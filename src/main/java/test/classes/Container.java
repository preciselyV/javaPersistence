package test.classes;

import PersistenceFramework.JsonClassCreator;
import PersistenceFramework.Serialize;
import PersistenceFramework.SerializeField;

import java.util.ArrayList;

@Serialize(allFields = true)
public class Container {
    @SerializeField
    public ArrayList<ArrayList<String>> coll = new ArrayList<>();

    public Container(){};

    public Container(ArrayList<ArrayList<String>> collection){
        this.coll = collection;
    }

}
