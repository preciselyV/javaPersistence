
@Serialize(allFields = false)
public class Person {
    @SerializeField
    private String name;
    @SerializeField
    private int age;

    public Person (String name , int age)
    {
        this.name = name;
        this.age = age;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getAge() {
        return age;
    }
    public String getName() {
        return name;
    }
}

@Serialize(allFields = true, requiersParent = true)
class Employee extends Person {
    private String position;

    public Employee(String position,String name , int age)
    {
        super(name,age);
        this.position = position;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}