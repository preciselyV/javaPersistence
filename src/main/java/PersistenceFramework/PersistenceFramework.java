package PersistenceFramework;

import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.json.*;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.function.Predicate;


//TODO нужно придумать что делать с вложенными классами
//TODO настройка приватных полей.
//TODO многопоточность для записи ?
//TODO если атрибут класса это тоже класс, но при этом поле помечено, а класс нет - что делать? (настроение - падать)
//TODO а что с суперклассами то делать?
//TODO что делать в случаях, когда указаны не все поля, которые нужны конструкору?
//TODO придумать что то для всяких контейнеров. Наверное мы хотим хранить не целиком эти классы, а просто содержимое
// TODO simple array support?
// TODO collection with predicate support
// TODO null fields support


public class PersistenceFramework {

    private static Stack<Object> alreadySerialized = new Stack<>();

    public PersistenceFramework() {}

    private static ArrayList<Field> getFields(Class<?> cls, Object obj)
    {

        if (cls.isAnnotationPresent(Serialize.class))
        {
            Serialize an = cls.getAnnotation(Serialize.class);
            ArrayList<Field> fields = new ArrayList<Field>();

            // in case we need some of the parent's fields
            if (an.requiresParent())
            {
                fields.addAll(getFields(cls.getSuperclass(), obj));
            }
            Field[] flds = cls.getDeclaredFields();

            // are we choosing some specific fields or all of them
            if (an.allFields())
            {
                fields.addAll(List.of(flds));
            }
            else
            {
                for (Field fld : flds)
                {
                    if (fld.isAnnotationPresent(SerializeField.class))
                    {
                        fields.add(fld);
                    }
                }
            }
            return fields;

        }
        else
        {
            System.out.println("not annotated");
            return null;
        }
    }

    public static String serialize(Object obj) {
        if (obj instanceof Collection)
            return serializeCollection((Collection<?>) obj).toString();
        return serializeInner(obj).toString();
    }

    private static JsonObject serializeInner(Object obj)
    {
        if (alreadySerialized.contains(obj))
            throw new PersistenceException("Cyclic object reference occurred");
        Class<?> cls = obj.getClass();
        String className = cls.getName();
        var objectFields = getFields(cls,obj);
        var json = Json.createObjectBuilder();
        json.add("ClassName", className);
        if (objectFields != null)
        {
            var jsonFields = Json.createObjectBuilder();
            for (Field field : objectFields)
            {
                try
                {
                    //TODO check whether we change visibility for every1 else
                    field.setAccessible(true);
                    if (isPrimitiveToSerializer(field.getType()))
                        jsonFields.add( field.getName(), field.get(obj).toString());
                    else if (Collection.class.isAssignableFrom(field.getType())) {
                        alreadySerialized.push(obj);
                        jsonFields.add(field.getName(), serializeCollection((Collection<?>) field.get(obj)));
                        alreadySerialized.pop();
                    }
                    else {
                        alreadySerialized.push(obj);
                        jsonFields.add(field.getName(), serializeInner(field.get(obj)));
                        alreadySerialized.pop();
                    }
                }
                catch (IllegalAccessException e)
                {
                    System.out.println("well, for some reason it was illegal");
                }
            }
            json.add("fields", jsonFields);
        }
        return json.build();
    }

    protected static JsonArray serializeCollection (Collection<?> collection) {
        JsonArrayBuilder arrBuilder = Json.createArrayBuilder();

        for (var element : collection) {
            if (element.getClass().isPrimitive() || element.getClass().equals(Integer.class) ||
                                                    element.getClass().equals(String.class)) {
                arrBuilder.add(element.toString());
            }
            else if (Collection.class.isAssignableFrom(element.getClass())) {
                alreadySerialized.push(collection);
                arrBuilder.add(serializeCollection((Collection<?>) element));
                alreadySerialized.pop();
            }
            else {
                alreadySerialized.push(collection);
                arrBuilder.add(serializeInner(element));
                alreadySerialized.pop();
            }
        }
        return arrBuilder.build();
    }

    //literally no idea how it does the trick... Magic, I guess
    public static Object convert(Class<?> targetType, String text) {
        PropertyEditor editor = PropertyEditorManager.findEditor(targetType);
        editor.setAsText(text);
        return editor.getValue();
    }

    @SuppressWarnings("unchecked") // haha
    public <T> T deserialize(String jsonString, Class<T> valueType) {
        if (jsonString == null) {
            throw new NullPointerException("Argument \"jsonString\" is null");
        }
        try {
            return (T) deserializeObject(jsonString, null);
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new PersistenceException("Deserialization error", e);
        }
    }

    @SuppressWarnings("unchecked") // haha
    public <T> T deserialize(String jsonString, Class<T> valueType, Predicate<JsonObject> predicate) {
        if (jsonString == null) {
            throw new NullPointerException("Argument \"jsonString\" is null");
        }
        try {
            return (T) deserializeObject(jsonString, predicate);
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new PersistenceException("Deserialization error", e);
        }
    }

    public static Object deserializeObject(String jsonString, Predicate<JsonObject> predicate) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();
        // TODO JsonArray on upper level support
        if (predicate != null) {
            JsonObject fields = object.getJsonObject("fields");
            if (predicate.test(fields))
                return deserializeInner(object);
            else return null;
        }
        return deserializeInner(object);
    }


    private static Object deserializeInner(JsonObject object) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        //Class initialization (find class and constructor)
        String name = object.getString("ClassName");
        Class<?> cls = Class.forName(name);
        Object[] constructors = Arrays.stream(cls.getConstructors()).filter(c -> c.isAnnotationPresent(JsonClassCreator.class)).toArray();
        // пока мы просто договариваемся, что есть только один аннотированный конструктор
        if (constructors.length == 1)
        {

            Constructor<?> constructor = (Constructor<?>)constructors[0];

            Annotation[][] annos = constructor.getParameterAnnotations();
            ArrayList<String> constructorParams = new ArrayList<>();

            for (Annotation[] ano : annos)
            {
                if (ano.length == 0)
                    continue;
                for (Annotation an : ano)
                {
                    if (an.annotationType().equals(CreatorField.class))
                    {
                        constructorParams.add(((CreatorField) an).value());
                    }
                }
            }

            //Fields initialization
            JsonObject fields = object.getJsonObject("fields");
            ArrayList<Object> params = new ArrayList<>();
            Set<?> keys = fields.keySet();
            for (var key : keys)
            {
                if (constructorParams.contains( (String) key))
                {
                    try {
                        var value = (Object) fields.getString( (String) key);
                        params.add(value);
                    }
                    catch (ClassCastException e) {
                        var complexValue = fields.getJsonObject((String) key);
                        params.add(deserializeInner(complexValue));
                    }
                }
            }
            // Parameters conversion to right types
            Class<?>[] required = constructor.getParameterTypes();
            for (int i = 0; i< required.length; i++)
            {
                try {
                    params.set(i, convert(required[i], (String) params.get(i)));
                }
                catch (ClassCastException e) { // complex types
                    params.set(i, params.get(i));
                }
            }

            try
            {
                return constructor.newInstance(params.toArray());
            }
            catch (IllegalArgumentException e )
            {
                e.printStackTrace();
                return null;
            }
        }
        else {
            constructors = Arrays.stream(cls.getConstructors()).filter(c -> c.getParameterCount() == 0).toArray();
            if (constructors.length == 1) {
                Constructor<?> constructor = (Constructor<?>)constructors[0];
                return constructor.newInstance();
            }
            else {
                throw new PersistenceException("Couldn't find constructor for the class");
            }
        }
        // TODO поля, которые не были затронуты конструктором
    }

    protected static boolean isPrimitiveToSerializer(Class<?> cls) {
        // java.beans.PropertyEditorManager documentation says it is supported
        // so i suppose that our convert() method will be able to process it
        return cls.isPrimitive() || cls.equals(Integer.class) ||
                cls.equals(String.class) || cls.equals(Double.class) ||
                cls.equals(Boolean.class) || cls.equals(Byte.class) ||
                cls.equals(Short.class) || cls.equals(Long.class) ||
                cls.equals(Float.class) /*|| cls.equals(java.awt.Color.class) ||
                cls.equals(java.awt.Font.class)*/;
    }
}
