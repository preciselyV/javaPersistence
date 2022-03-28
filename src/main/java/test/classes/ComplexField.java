package test.classes;

import PersistenceFramework.CreatorField;
import PersistenceFramework.JsonClassCreator;
import PersistenceFramework.Serialize;
import PersistenceFramework.SerializeField;

import java.util.ArrayList;

@Serialize(allFields = true)
public class ComplexField {
    public int i = 0;
    public String str = "Some string";
    public ArrayList<Integer> coll = new ArrayList<>();

    public ComplexField(){};

    public ComplexField( int i, String str){
        this.i = i;
        this.str = str;
    }

    public ComplexField(int i, String str, ArrayList<Integer> collection){
        this.i = i;
        this.str = str;
        this.coll = collection;
    }

    @Override
    public String toString() {
        return "ComplexField{" +
                "i=" + i +
                ", str='" + str + '\'' +
                ", coll=" + coll +
                '}';
    }
}
