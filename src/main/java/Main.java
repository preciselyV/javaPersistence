import PersistenceFramework.PersistenceFramework;

import java.lang.reflect.InvocationTargetException;

class tmp {
    public String s1;
    public String s2;
    public int s3;

    public tmp (String s1, String s2, int s3)
    {
        this.s1 = s1;
        this.s2 = s2;
        this.s3 = s3;
    }
}


public class Main {
    public static void main(String[] args) {
        //Programmer ex = new Programmer("asshole","Rofl Olegovich", 23, "original");

        tmp t = new tmp("1","2",3);

        Person ex = new Person("pomogiti", 228, null);
        String res = PersistenceFramework.serialize(ex);
        System.out.println(res);
        try
        {
            Person p = (Person) PersistenceFramework.deserializeObject(res, null);
            if (p != null)
            {
                System.out.println(p.getAge());
                System.out.println(p.getName());
            }
        }
        catch (ClassNotFoundException e )
        {
            System.out.println("class was not found");
        }
        catch (InvocationTargetException | InstantiationException | IllegalAccessException e )
        {
            System.out.println("huinya");
            System.out.println("apparently there is no constructor for the class");
        }
    }
}
