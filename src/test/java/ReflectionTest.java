import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionTest {
    @Test
    public void testComplexInnerFieldSerialize() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        Person person = new Person("Author", 12);
        // serialization test
        String res = Reflection.serialize(person);
        System.out.println("Serialized JSON: " + res);
        assertTrue(res.contains("Author"));
        assertTrue(res.contains("12"));
        // deserialization test
        Reflection framework = new Reflection();
        Person p = framework.deserialize(res, Person.class);
        assertNotNull(p);
        System.out.println("Deserialized JSON: " + p);
        assertEquals(p.getAge(), 12);
        assertEquals(p.getName(), "Author");
        assertNotNull(p.complexField);
    }
}