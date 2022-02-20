
@Serialize(allFields = false)
public class myClass {
    //@SerializeField
    private String name;
    @SerializeField
    private int age;

    public myClass (String name , int age)
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
