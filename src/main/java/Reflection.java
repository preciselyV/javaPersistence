import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


//TODO нужно придумать что делать с вложенными классами
//TODO получать id объектов, чтобы разбираться с циклами
//TODO настройка приватных полей.
//TODO многопоточность для записи ?
//TODO ага, jsonы тоже нужно добавить
//TODO как подтягивать сам класс из json? Нужно понять, как найти конструктор
public class Reflection {
    public static void getFields(Object ccls)
    {
        Class<?> cls = ccls.getClass();
        if (cls.isAnnotationPresent(Serialize.class))
        {
            Serialize an = cls.getAnnotation(Serialize.class);
            Field[] flds = cls.getDeclaredFields();
            if (an.allFields())
            {
                convertToJson(new ArrayList<Field>(List.of(flds)));
            }
            else
            {
                ArrayList<Field> serializable = new ArrayList<Field>();
                for (Field fld : flds)
                {
                    if (fld.isAnnotationPresent(SerializeField.class))
                    {
                        serializable.add(fld);
                    }
                }
                convertToJson(serializable);
            }
        }
        else
        {
            System.out.println("not annotated");
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
}
