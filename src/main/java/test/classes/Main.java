package test.classes;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import static PersistenceFramework.PersistenceFramework.deserializeObject;
import static PersistenceFramework.PersistenceFramework.serialize;




public class Main {
    public static void main(String[] args) {
        //Programmer ex = new Programmer("asshole","Rofl Olegovich", 23, "original");

        Person ex = new Person("pomogiti", 228, new ComplexField());
        ex.setPrikol(new ComplexField(1,"tmp"));
        String res = serialize(ex);
        System.out.println(res);
        try
        {
            Person p = (Person) deserializeObject(res, null);
            if (p != null)
            {
                System.out.println(p.getAge());
                System.out.println(p.getName());
                System.out.println(p.getPrikol());
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
