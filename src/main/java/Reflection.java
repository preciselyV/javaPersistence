import com.google.gson.Gson;

import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;



//TODO нужно придумать что делать с вложенными классами
//TODO получать id объектов, чтобы разбираться с циклами
//TODO настройка приватных полей.
//TODO многопоточность для записи ?
//TODO если атрибут класса это тоже класс, но при этом поле помечено, а класс нет - что делать? (настроение - падать)
//TODO а что с суперклассами то делать?
//TODO что делать в случаях, когда указаны не все поля, которые нужны конструкору?
//TODO придумать что то для всяких контейнеров. Наверное мы хотим хранить не целиком эти классы, а просто содержимое

public class Reflection {

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

    public static String serialize(Object obj)
    {
        Class<?> cls = obj.getClass();
        String className = cls.getName();
        var fields = getFields(cls,obj);
        var json = Json.createObjectBuilder();
        json.add("ClassName", className);
        if (fields != null)
        {
            var flds = Json.createArrayBuilder();
            for (Field field : fields)
            {
                try
                {
                    //TODO check whether we change visibility for every1 else
                    field.setAccessible(true);
                    flds.add( Json.createObjectBuilder().add(field.getName(), field.get(obj).toString()));
                }
                catch (IllegalAccessException e)
                {
                    System.out.println("well, for some reason it was illegal");
                }
            }
            json.add("fields", flds);
        }
        return json.build().toString();
    }

    //literally no idea how it does the trick... Magic, I guess
    private static Object convert(Class<?> targetType, String text) {
        PropertyEditor editor = PropertyEditorManager.findEditor(targetType);
        editor.setAsText(text);
        return editor.getValue();
    }

    public static Object deserialize(String jsonString) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();

        String name = object.getString("ClassName");
        Class<?> cls = Class.forName(name);
        Object[] constructors = Arrays.stream(cls.getConstructors()).filter(c -> c.isAnnotationPresent(JsonClassCreator.class)).toArray();
        // пока мы просто договариваемся, что есть только один аннотированный конструктор
        if (constructors.length != 1)
        {
            // кажется пора писать кастомные исключения
            throw new NoSuchMethodException("couldn't find constructor for the class");
        }
        Constructor<?> constructor = (Constructor<?>)constructors[0];

        Annotation[][] annos = constructor.getParameterAnnotations();
        ArrayList<String> consParams = new ArrayList<>();

        for (Annotation[] ano : annos)
        {
            if (ano.length == 0)
                continue;
            for (Annotation an : ano)
            {
                if (an.annotationType().equals(CreatorField.class))
                {
                    consParams.add(((CreatorField) an).value());
                }
            }
        }
        JsonArray ar = object.getJsonArray("fields");

        ArrayList<Object> params = new ArrayList<>();
        for (var stuff : ar)
        {
            JsonObject jsonObj = stuff.asJsonObject();
            Set<?> keys = jsonObj.keySet();
            for (var key : keys)
            {
                if (consParams.contains( (String) key))
                {
                    var rofl = (Object) jsonObj.getString( (String) key);
                    params.add(rofl);
                }
            }
        }


        Class<?>[] required = constructor.getParameterTypes();
        for (int i = 0; i< required.length; i++)
        {
            params.set(i, convert(required[i], (String) params.get(i)));
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

}
