package test.classes;

import PersistenceFramework.PersistenceFramework;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

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
        //test.classes.Programmer ex = new test.classes.Programmer("asshole","Rofl Olegovich", 23, "original");
        ArrayList<Integer> arr = new ArrayList<Integer>();
        System.out.println(arr.getClass().toGenericString());
    }
}
