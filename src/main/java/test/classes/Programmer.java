package test.classes;


import PersistenceFramework.CreatorField;
import PersistenceFramework.JsonClassCreator;
import PersistenceFramework.Serialize;

@Serialize(allFields = true, requiresParent = true)
public class Programmer extends Employee
{
    private String heroinType;

    @JsonClassCreator
    public Programmer(@CreatorField("position") String position, @CreatorField("name") String name , @CreatorField("age") int age, @CreatorField("heroinType") String heroinType)
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
