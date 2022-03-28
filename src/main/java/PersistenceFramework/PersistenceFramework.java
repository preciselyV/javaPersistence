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


//TODO настройка приватных полей.
//TODO многопоточность для записи ?
//TODO если атрибут класса это тоже класс, но при этом поле помечено, а класс нет - что делать? (настроение - падать)
//TODO а что с суперклассами то делать?
//TODO что делать в случаях, когда указаны не все поля, которые нужны конструкору?
//TODO придумать что то для всяких контейнеров. Наверное мы хотим хранить не целиком эти классы, а просто содержимое
// TODO simple array support?
// TODO collection with predicate support
// TODO collection deserialization


public class PersistenceFramework {

    private static Stack<Object> alreadySerialized = new Stack<>();

    public PersistenceFramework() {}

    private static HashMap<String, Field> getFields(Class<?> cls, Object obj)
    {

        if (cls.isAnnotationPresent(Serialize.class))
        {
            Serialize an = cls.getAnnotation(Serialize.class);
            HashMap<String, Field>fields = new HashMap<>();

            // in case we need some of the parent's fields
            if (an.requiresParent())
            {
                // if names intersect then someone's an idiot and I don't want to deal with it
                fields.putAll(getFields(cls.getSuperclass(), obj));
            }
            Field[] flds = cls.getDeclaredFields();

            // are we choosing some specific fields or all of them
            if (an.allFields())
            {
                for (Field fld : flds)
                {
                    fields.put(fld.getName(),fld);
                }
            }
            else
            {
                for (Field fld : flds)
                {
                    if (fld.isAnnotationPresent(SerializeField.class))
                    {
                        SerializeField serAno = fld.getAnnotation(SerializeField.class);
                        if (!Objects.equals(serAno.Name(), ""))
                        {
                            fields.put(serAno.Name(), fld);
                        }
                        else
                        {
                            fields.put(fld.getName(), fld);
                        }
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

    private static JsonValue serializeInner(Object obj)
    {
        if (obj == null) {
            return JsonValue.NULL;
        }
        if (alreadySerialized.contains(obj))
            throw new PersistenceException("Cyclic object reference occurred");
        Class<?> cls = obj.getClass();
        String className = cls.getName();
        var objectFields = getFields(cls,obj);
        var json = Json.createObjectBuilder();
        json.add("ClassName", className);
        if (objectFields != null)
        {
            var keys = objectFields.keySet();
            var jsonFields = Json.createObjectBuilder();
            for (String key : keys)
            {
                try
                {
                    //TODO check whether we change visibility for every1 else
                    Field field = objectFields.get(key);
                    field.setAccessible(true);
                    if (isPrimitiveToSerializer(field.getType())) {
                        if (field.get(obj) != null){
                            jsonFields.add(key, field.get(obj).toString());
                        }
                        else {
                            jsonFields.add(key, JsonValue.NULL);
                        }
                    }
                    else if (Collection.class.isAssignableFrom(field.getType())) {
                        alreadySerialized.push(obj);
                        jsonFields.add(key, serializeCollection((Collection<?>) field.get(obj)));
                        alreadySerialized.pop();
                    }
                    else {
                        alreadySerialized.push(obj);
                        jsonFields.add(key, serializeInner(field.get(obj)));
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

    protected static JsonValue serializeCollection (Collection<?> collection) {
        if (collection == null) {
            return JsonValue.NULL;
        }
        JsonObjectBuilder collectionBuilder = Json.createObjectBuilder();
        collectionBuilder.add("ClassName",collection.getClass().getName());
        if (collection.size() != 0)
            collectionBuilder.add("genericType", collection.iterator().next().getClass().getName());
        else collectionBuilder.add("genericType", JsonValue.NULL);
        JsonArrayBuilder arrBuilder = Json.createArrayBuilder();

        for (var element : collection) {
            if (element == null) {
                arrBuilder.add(JsonValue.NULL);
            }
            else if (element.getClass().isPrimitive() || element.getClass().equals(Integer.class) ||
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
        collectionBuilder.add("array",arrBuilder.build());
        return collectionBuilder.build();
    }

    //literally no idea how it does the trick... Magic, I guess
    public static Object convert(Class<?> targetType, String text) {
        PropertyEditor editor = PropertyEditorManager.findEditor(targetType);
        editor.setAsText(text);
        return editor.getValue();
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialize(String jsonString) {
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
    public <T> T deserialize(String jsonString, Predicate<JsonObject> predicate) {
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
        if (object.containsKey("fields")){
            if (predicate != null) {
                JsonObject fields = object.getJsonObject("fields");
                if (predicate.test(fields))
                    return deserializeInner(object);
                else return null;
            }
            return deserializeInner(object);
        }
        // collection on upper level support
        else if (object.containsKey("array")) {
            if (predicate != null) {
                JsonObjectBuilder filteredCollection = Json.createObjectBuilder();
                filteredCollection.add("ClassName", object.getString("ClassName"));
                try {
                    filteredCollection.add("genericType", object.getString("genericType"));
                }
                catch (ClassCastException e) {
                    return deserializeCollection(object);
                }

                JsonArrayBuilder filteredArray = Json.createArrayBuilder();
                JsonArray elements = object.getJsonArray("array");
                for(int i = 0; i < elements.size(); i++){
                    JsonObject obj = elements.getJsonObject(i);
                    if (predicate.test(obj.getJsonObject("fields")))
                        filteredArray.add(obj);
                }
                filteredCollection.add("array", filteredArray.build());
                return deserializeCollection(filteredCollection.build());
            }
            return deserializeCollection(object);
        }
        return null;
    }

    // Обёртка над десериализаторами, чтобы не нужно было постоянно делать эту классификацию
    private static Object deserializeJsonObject(JsonObject object) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (object.containsKey("fields")){
            return deserializeInner(object);
        }
        else if (object.containsKey("array")) {
            return deserializeCollection(object);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Collection<?> deserializeCollection(JsonObject jsonObject) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String name = jsonObject.getString("ClassName");
        Class<?> cls = Class.forName(name);
        Collection<Object> collection = (Collection<Object>) Arrays.stream(cls.getConstructors()).filter(c ->  c.getParameterCount() == 0).toList().get(0).newInstance();
        String genericClass;
        try{
            genericClass = jsonObject.getString("genericType");
        }
        catch (ClassCastException e) {
            return collection;
        }

        Class<?> genCls = Class.forName(genericClass);

        JsonArray jsonArray = jsonObject.getJsonArray("array");
        for (int i = 0; i < jsonArray.size(); i++) {
            if (jsonArray.isNull(i)) {
                collection.add(null);
            }
            else if (isPrimitiveToSerializer(genCls)) {
                collection.add(convert(genCls, jsonArray.getString(i)));
            }
            else {
                collection.add(deserializeJsonObject(jsonArray.getJsonObject(i)));
            }
        }
        return collection;
    }

    private static Field findField(Class<?> cls, String fieldName)
    {
        Field res = null;
        var fields = cls.getDeclaredFields();
        Serialize an = cls.getAnnotation(Serialize.class);
        if (an == null)
            throw new PersistenceException("deserializing unannotated class");
        for (Field fld : fields)
        {
            //fld.setAccessible(true);
            if (fld.getName().equals(fieldName))
            {
                res = fld;
                break;
            }
            else if (fld.isAnnotationPresent(SerializeField.class))
            {
                SerializeField serAno = fld.getAnnotation(SerializeField.class);
                if (serAno.Name().equals(fieldName))
                {
                    res = fld;
                    break;
                }
            }
        }

        if (res == null && an.requiresParent())
            res = findField(cls.getSuperclass(),fieldName);
        return res ;
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
            HashMap<String, Object> toSet = new HashMap<>();
            Set<?> keys = fields.keySet();
            //getting constructor params
            for (String fieldName: constructorParams)
            {
                if (keys.contains(fieldName))
                {
                    try {
                        var value = (Object) fields.getString(fieldName);
                        params.add(value);
                    }
                    catch (ClassCastException e) {
                        try {
                            var complexValue = fields.getJsonObject(fieldName);
                            params.add(complexValue);
                        }
                        catch (ClassCastException e2) {
                            params.add(null);
                        }
                    }
                }
                else
                {
                    System.out.println("ALERT COULDN'T FIND A KEY");
                }
            }
            //getting other fields
            for (var key : keys)
            {
                if (!constructorParams.contains( (String) key))
                {
                    try {
                        var value = (Object) fields.getString( (String) key);
                        toSet.put((String) key,value);
                    }
                    catch (ClassCastException e) {
                        try {
                            var complexValue = fields.getJsonObject((String) key);
                            toSet.put((String)key, complexValue);
                        }
                        catch (ClassCastException e2) {
                            toSet.put((String) key, null);
                        }

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
                    params.set(i, deserializeJsonObject((JsonObject) params.get(i)));
                }
                catch (NullPointerException e2) {
                    params.set(i, null);
                }
            }
            Set<String> fieldKeys = toSet.keySet();
            for (String key : fieldKeys)
            {
                Object value = toSet.get(key);
                Field field = findField(cls, key);
                field.setAccessible(true);
                Class<?> fieldClass = field.getType();
                try {
                    toSet.put(key, convert(fieldClass, (String) toSet.get(key)));
                }
                catch (ClassCastException e) {
                    toSet.put(key, deserializeJsonObject( (JsonObject) toSet.get(key)));
                }
                catch (NullPointerException e) {
                    toSet.put(key, null);
                }
            }

            Object res;
            try
            {
                res = constructor.newInstance(params.toArray());

            }
            catch (IllegalArgumentException e )
            {
                e.printStackTrace();
                return null;
            }
            for(String key : fieldKeys)
            {
                Field fld = findField(cls,key);
                fld.setAccessible(true);
                fld.set(res, toSet.get(key));
            }
            return res;
        }
        // случай, когда конструктор не объявлен явно
        else {

            constructors = Arrays.stream(cls.getConstructors()).filter(c -> c.getParameterCount() == 0).toArray();
            Object res;
            if (constructors.length == 1) {
                Constructor<?> constructor = (Constructor<?>)constructors[0];
                res =  constructor.newInstance();
            }
            else {
                throw new PersistenceException("Couldn't find constructor for the class");
            }
            JsonObject fields = object.getJsonObject("fields");
            Set<?> keys = fields.keySet();
            for (var key : keys)
            {

                Field field = findField(cls, (String) key);
                field.setAccessible(true);
                try {
                    String value = fields.getString( (String) key);
                    Object updatedValue = convert(field.getType(), value);
                    field.set(res, updatedValue);
                }
                catch (ClassCastException e) {
                    JsonObject complexValue = fields.getJsonObject((String) key);
                    Object updatedValue = deserializeJsonObject(complexValue);
                    field.set(res, updatedValue);
                }

            }
            return res;
        }
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
