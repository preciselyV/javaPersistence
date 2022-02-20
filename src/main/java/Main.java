
public class Main {
    public static void main(String[] args){
        Programmer ex = new Programmer("asshole","Rofl Olegovich", 23, "original");
        Reflection.serialize(ex);
    }
}
