package test.classes;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import static PersistenceFramework.PersistenceFramework.deserializeObject;
import static PersistenceFramework.PersistenceFramework.serialize;




public class Main {
    public static void main(String[] args) {
        //test.classes.Programmer ex = new test.classes.Programmer("asshole","Rofl Olegovich", 23, "original");

        tmp t = new tmp();
        t.s1 = "1";
        t.s2 = "1";
        t.s3 = 1;


        //Person ex = new Person("pomogiti", 228, new ComplexField());
        //ex.setPrikol(new ComplexField(1,"tmp"));
        String res = serialize(t);
        System.out.println(res);
        try
        {
            tmp p = (tmp) deserializeObject(res, null);
            if (p != null)
            {
                System.out.println(p.s1);
                System.out.println(p.s2);
                System.out.println(p.s3);
            }
        }
        catch (ClassNotFoundException e )
        {
            System.out.println("class was not found");
        }
        catch (InvocationTargetException | InstantiationException | IllegalAccessException e )
        {
            e.printStackTrace();
        }
    }
}
