import PersistenceFramework.*;

import org.junit.jupiter.api.Test;
import test.classes.ComplexField;
import test.classes.Person;
import test.classes.User;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class PersistenceFrameworkTest {
    @Test
    public void testComplexInnerFieldSerialization() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        System.out.println("Base functionality test:");
        Person person = new Person("Author", 12, new ComplexField(145, "Intresting String"));
        // serialization test
        String res = PersistenceFramework.serialize(person);
        System.out.println("  Serialized JSON: " + res);
        assertTrue(res.contains("Author"));
        assertTrue(res.contains("12"));
        // deserialization test
        PersistenceFramework framework = new PersistenceFramework();
        Person p = framework.deserialize(res);
        assertNotNull(p);
        System.out.println("  Deserialized JSON: " + p + "\n");
        assertEquals(p.getAge(), 12);
        assertEquals(p.getName(), "Author");
        assertNotNull(p.complexField);
    }

    @Test
    public void testSimplePredicateAppliedToSingleObject() {
        System.out.println("Predicate test:");
        Person person = new Person("Author", 12, new ComplexField());
        String res = PersistenceFramework.serialize(person);
        System.out.println("  Serialized JSON: " + res);
        // deserialization test
        PersistenceFramework framework = new PersistenceFramework();
        JSONPredicate<Integer> jsonPredicate = new JSONPredicate<Integer>("complexField/i", (Integer i) -> i != 0, Integer.class);
        // simple predicates can be used in deserialization
        Person p = framework.deserialize(res, jsonPredicate);
        assertNull(p);
        System.out.println("  Object wasn`t deserialized because of predicate" + "\n");
    }

    @Test
    public void testPredicates() {
        System.out.println("Predicates functionality test:");
        Person person = new Person("Author", 12, new ComplexField());
        String res = PersistenceFramework.serialize(person);
        System.out.println("  Serialized JSON: " + res);
        JSONPredicate<Integer> jsonPredicate1 = new JSONPredicate<>("complexField/i", (Integer i) -> i == 0, Integer.class);
        JSONPredicate<String> jsonPredicate2 = new JSONPredicate<>("complexField/str", (String s) -> s.length()<5, String.class);
        var j = jsonPredicate1.and(jsonPredicate2.negate());

        // test predicates test() method
        JsonReader jsonReader = Json.createReader(new StringReader(res));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();
        JsonObject fields = object.getJsonObject("fields");
        System.out.println("  Complex predicate result: " + j.test(fields) + "\n");
        assertTrue(j.test(fields));

        PersistenceFramework framework = new PersistenceFramework();
        // complex predicates can be used in deserialization
        Person p = framework.deserialize(res, j);
        assertNotNull(p);
    }

    @Test
    public void testSerializeObjectsWithSimpleCollections() {
        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(1321); ids.add(1322); ids.add(1233);
        ComplexField cf = new ComplexField(19214, "group", ids);

        System.out.println("Serializing objects with simple collections: ");
        String result = PersistenceFramework.serialize(cf);
        System.out.println("  Serialized JSON: " + result  + "\n");
        assertTrue(result.contains("[\"1321\",\"1322\",\"1233\"]"));
    }

    @Test
    public void testSerializeSimpleCollections() {
        ArrayList<String> names = new ArrayList<>();
        names.add("Maksim"); names.add("Vladimir"); names.add("Arseniy"); names.add("Denis");

        System.out.println("Serializing simple collections: ");
        String result = PersistenceFramework.serialize(names);
        System.out.println("  Serialized JSON: " + result + "\n");
        assertTrue(result.contains("Maksim"));
        assertTrue(result.contains("Vladimir"));
        assertTrue(result.contains("Arseniy"));
        assertTrue(result.contains("Denis"));
    }

    @Test
    public void testSerializeCollectionsOfCollections() {
        ArrayList<ArrayList<Integer>> coll = new ArrayList<>();
        ArrayList<Integer> ints1 = new ArrayList<>();
        ints1.add(1321); ints1.add(1322); ints1.add(1233);
        ArrayList<Integer> ints2 = new ArrayList<>();
        ints2.add(1); ints2.add(2); ints2.add(3); ints2.add(4);
        ArrayList<Integer> ints3 = new ArrayList<>();
        ints3.add(0); ints3.add(0);
        coll.add(ints1); coll.add(ints2); coll.add(ints3);

        System.out.println("Serializing collections of simple collections: ");
        String result = PersistenceFramework.serialize(coll);
        System.out.println("  Serialized JSON: " + result + "\n");
        assertTrue(result.contains("[\"1321\",\"1322\",\"1233\"]"));
        assertTrue(result.contains("[\"1\",\"2\",\"3\",\"4\"]"));
        assertTrue(result.contains("[\"0\",\"0\"]"));
    }


    @Test
    public void testBidirectionalRelationshipsSerializationWithException() {
        User user = new User(1, "John");
        User.Item item = new User.Item(2, "book", user);
        user.addItem(item);

        assertThrows(PersistenceException.class, () -> {
            PersistenceFramework.serialize(user);
        });
    }

    @Test
    public void testEqualObjectSerialization() {
        User user = new User(1, "John");
        ArrayList<User> users = new ArrayList<>();
        users.add(user);
        users.add(user);
        System.out.println("Test equal object serialization:");
        String res = PersistenceFramework.serialize(users);
        System.out.println("  Serialized JSON: " + res + "\n");
    }
}