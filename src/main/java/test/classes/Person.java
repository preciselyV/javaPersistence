package test.classes;

import PersistenceFramework.CreatorField;
import PersistenceFramework.JsonClassCreator;
import PersistenceFramework.Serialize;
import PersistenceFramework.SerializeField;

@Serialize(allFields = false)
public class Person {
    @SerializeField()
    private String name;
    @SerializeField()
    private int age;

    @SerializeField()
    private ComplexField prikol;

    @SerializeField()
    public ComplexField complexField;

    @JsonClassCreator
    public Person (@CreatorField("name") String name , @CreatorField("age") int age, @CreatorField("complexField") ComplexField complexField)
    {
        this.name = name;
        this.age = age;
        this.complexField = complexField;
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

    public void setPrikol(ComplexField mama) {this.prikol = mama;}
    public ComplexField getPrikol(){return this.prikol;}

    @Override
    public String toString() {
        return "test.classes.Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", complexField=" + complexField +
                '}';
    }
}

@Serialize(allFields = true, requiresParent = true)
class Employee extends Person {
    private String position;

    public Employee(String position,String name , int age)
    {
        super(name,age, null);
        this.position = position;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}

@Serialize(allFields = true, requiresParent = true)
class Programmer extends Employee
{
    private String heroinType;

    public Programmer(String position,String name , int age, String heroinType)
    {
        super(position, name, age);
        this.heroinType = heroinType;
    }

    public String getHeroinType() {
        return heroinType;
    }

    public void setHeroinType(String heroinType) {
        this.heroinType = heroinType;
    }
}

