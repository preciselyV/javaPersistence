package PersistenceFramework;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class JSONPredicate<T> implements Predicate<JsonObject> {
    private final Predicate<T> fieldPredicate;
    protected List<String> pathNames;

    // TODO find how to get class without storing it (maybe Reflection will help)
    Class<T> valueType;
    public JSONPredicate(String path, Predicate<T> fieldPredicate, Class<T> valueType) {
        if (path == null)
            throw new NullPointerException("Argument \"path\" is null");
        if (!(valueType.isPrimitive() || valueType.equals(Integer.class) ||
                valueType.equals(String.class))) {
            throw new PersistenceException("Predicates work only with primitive types");
        }
        this.fieldPredicate = fieldPredicate;
        this.valueType = valueType;
        this.pathNames = Arrays.stream(path.split("/")).toList();
        if (pathNames.size() < 1) {
            throw new IllegalArgumentException("Couldn`t extract names of fields in path, path should be like: \"complex_field_1/complex_field_2/simple_field_3\"");
        }
    }

    @SuppressWarnings("unchecked")
    public boolean test(JsonObject jsonObjectFields) {
        JsonObject currentObj = jsonObjectFields;
        for (int i = 0; i < pathNames.size(); i++) {
            String name = pathNames.get(i);
            if (i == pathNames.size() - 1){ // simple type, needs to be checked for predicate
                String strValue = currentObj.getString(name);
                T value = (T) PersistenceFramework.convert(valueType, strValue);
                return fieldPredicate.test(value);
            }
            else {
                currentObj = currentObj.getJsonObject(name).getJsonObject("fields");
            }
        }
        throw new PersistenceException("Couldn`t evaluate predicate");
    }
}
