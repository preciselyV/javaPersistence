import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

class tmp {
    public String s1;
    public String s2;
    public String s3;

    public tmp (String s1, String s2,String s3)
    {
        this.s1 = s1;
        this.s2 = s2;
        this.s3 = s3;
    }
}


public class Main {
    public static void main(String[] args){
        //Programmer ex = new Programmer("asshole","Rofl Olegovich", 23, "original");
        /*
        Person ex = new Person("pomogiti", 228);
        String res = Reflection.serialize(ex);
        System.out.println(res);
        try
        {
            Reflection.deserialize(res);
        }
        catch (ClassNotFoundException e )
        {
            System.out.println("class was not found");
        }
        catch (NoSuchMethodException e )
        {
            System.out.println("apparently there is no constructor for the class");
        }
        */

        Class<?>[] types = {String.class, String. class,String.class};
        try {
            Constructor<?> constructor = Class.forName("tmp").getConstructor(types);

            Object[] ar = {"3","2","1"};
            Object res = constructor.newInstance(ar);

            tmp finallyRes = (tmp) res;
            System.out.println(finallyRes.s1);
        }
        catch (NoSuchMethodException e)
        {
            System.out.println("no such method");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
