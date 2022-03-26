package test.classes;

import java.util.List;

public class User {
    public int id;
    public String name;
    public List<Item> userItems;

    public User (int id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addItem (Item i) {
        userItems.add(i);
    }

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
