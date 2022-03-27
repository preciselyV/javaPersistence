package test.classes;

import PersistenceFramework.Serialize;

import java.util.ArrayList;
import java.util.List;

@Serialize(allFields = true)
public class User {
    public int id;
    public String name;
    public List<Item> userItems = new ArrayList<>();

    public User (int id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addItem (Item i) {
        userItems.add(i);
    }

    @Serialize(allFields = true)
    public static class Item {
        public int id;
        public String itemName;
        public User owner;

        public Item(int id, String itemName, User owner){
            this.id = id;
            this.itemName = itemName;
            this.owner = owner;
        }
    }
}
