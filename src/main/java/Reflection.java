import java.lang.reflect.Field;

public class Reflection {
    public static void getFields(Object ccls)
    {
        Class<?> cls = ccls.getClass();
        if (cls.isAnnotationPresent(Serialize.class))
        {
            Serialize an = cls.getAnnotation(Serialize.class);
            if (an.all())
            {
                Field[] flds = cls.getDeclaredFields();
                for (Field fld : flds)
                {
                    String name = fld.getName();
                    System.out.println(name);
                }
            }
        }
        else
        {
            System.out.println("not annotated");
        }
    }
}
