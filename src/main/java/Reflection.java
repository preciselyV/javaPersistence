import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

//TODO нужно придумать что делать с вложенными классами
//TODO получать id объектов, чтобы разбираться с циклами
//TODO настройка приватных полей.
//TODO многопоточность для записи ?
//TODO ага, jsonы тоже нужно добавить
//TODO как подтягивать сам класс из json? Нужно понять, как найти конструктор
//TODO если атрибут класса это тоже класс, но при этом поле помечено, а класс нет - что делать? (настроение - падать)
//TODO а что с суперклассами то делать?
public class Reflection {

    private static  ArrayList<Field> getFields(Class<?> cls, Object obj)
    {

        if (cls.isAnnotationPresent(Serialize.class))
        {
            Serialize an = cls.getAnnotation(Serialize.class);
            ArrayList<Field> fields = new ArrayList<Field>();

            // in case we need some of the parent's fields
            if (an.requiersParent())
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
        var fields = getFields(cls,obj);
        if (fields != null)
            convertToJson(fields);
    }
}
