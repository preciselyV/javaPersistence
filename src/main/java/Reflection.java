import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.util.List;


//TODO нужно придумать что делать с вложенными классами
//TODO получать id объектов, чтобы разбираться с циклами
//TODO настройка приватных полей.
//TODO многопоточность для записи ?
//TODO ага, jsonы тоже нужно добавить
//TODO как подтягивать сам класс из json? Нужно понять, как найти конструктор
//TODO если атрибут класса это тоже класс, но при этом поле помечено, а класс нет - что делать? (настроение - падать)
//TODO а что с суперклассами то делать?
//TODO что делать в случаях, когда указаны не все поля, которые нужны конструкору?
//TODO придумать что то для всяких конструкторов. Наверное мы хотим хранить не целиком эти классы, а просто содержимое
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

    private static void convertToJson(ArrayList<Field> flds)
    {
        for (Field fld : flds)
        {
            String name = fld.getName();
            System.out.println(name);
        }
    }


    public static void serialize(Object obj)
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
                    field.setAccessible(true);
                    flds.add( Json.createObjectBuilder().add(field.getName(), field.get(obj).toString()));
                }
                catch (IllegalAccessException e)
                {
                    System.out.println("well, it was illegal");
                }
            }
            json.add("fields", flds);
        }
        System.out.println(json.build().toString());
    }
}
