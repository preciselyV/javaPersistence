import PersistenceFramework.*;
import org.junit.jupiter.api.Test;

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
    public void testPredicateAppliedToSingleObject() {
        System.out.println("Predicate test:");
        Person person = new Person("Author", 12, new ComplexField());
        String res = PersistenceFramework.serialize(person);
        System.out.println("  Serialized JSON: " + res);
        // deserialization test
        PersistenceFramework framework = new PersistenceFramework();
        JSONPredicate<Integer> jsonPredicate = new JSONPredicate<Integer>("complexField/i", (Integer i) -> i != 0, Integer.class);
        Person p = framework.deserialize(res, Person.class, jsonPredicate);
        assertNull(p);
        System.out.println("  Object wasn`t deserialized");
    }
}