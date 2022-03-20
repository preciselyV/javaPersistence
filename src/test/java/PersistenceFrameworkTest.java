import PersistenceFramework.*;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;

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
        Person p = framework.deserialize(res, Person.class);
        assertNotNull(p);
        System.out.println("  Deserialized JSON: " + p);
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
        Person p = framework.deserialize(res, Person.class, jsonPredicate);
        assertNull(p);
        System.out.println("  Object wasn`t deserialized");
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

        JsonReader jsonReader = Json.createReader(new StringReader(res));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();
        JsonObject fields = object.getJsonObject("fields");
        System.out.println("  Complex predicate result: " + j.test(fields));
        assertTrue(j.test(fields));

        PersistenceFramework framework = new PersistenceFramework();
        // complex predicates can be used in deserialization
        Person p = framework.deserialize(res, Person.class, j);
        assertNotNull(p);
    }
}