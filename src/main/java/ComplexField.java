import PersistenceFramework.JsonClassCreator;
import PersistenceFramework.Serialize;
import PersistenceFramework.SerializeField;

@Serialize(allFields = true)
public class ComplexField {
    @SerializeField
    public int i = 0;
    @SerializeField
    public String str = "Some string";


    public ComplexField(){};

    ComplexField(int i, String str){
        this.i = i;
        this.str = str;
    }

    @Override
    public String toString() {
        return "ComplexField{" +
                "i=" + i +
                ", str='" + str + '\'' +
                '}';
    }
}
