package polyGson;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

public class PolyGson {

    public static final String CLASS = "_class";
    public static final String VALUE = "_value";
    public static final String ARRAY = "_array";
    public static final String MAP = "_map";

    private final ConstructorConstructor constructorConstructor = new ConstructorConstructor(Collections.emptyMap());
    private final Gson gson;

    public PolyGson() {
        gson = new GsonBuilder()
                .create();

        // TODO: SHOULD WE ADD .enableComplexMapKeySerialization(): com.google.gson.internal.bind.MapTypeAdapterFactory.Adapter.write
    }

    PolyGson(Gson gson) {
        this.gson = gson;
    }

    public String toJson(Object src) {
        if (src == null) {
            return null;
        }
        JsonElement jsonElement = gson.toJsonTree(src);
        JsonElement withClassInfo = createElementWithClassInfo(src, jsonElement);
        return gson.toJson(withClassInfo);
    }

    @SuppressWarnings("unchecked")
    public <T> T fromJson(String json, Class<T> klass) {
        return (T) fromJson(json);
    }

    public Object fromJson(String json) {
        StringReader reader = new StringReader(json);
        JsonReader jsonReader = new JsonReader(reader);
        return fromJson(jsonReader);
    }

    private Object fromJson(JsonReader jsonReader) {
        TypeAdapter<JsonElement> adapter = gson.getAdapter(JsonElement.class);
        try {
            return fromJson(jsonReader, adapter);
        } catch (JsonSyntaxException e) {
            throw new PolyGsonException(e);
        }
    }

    private Object fromJson(JsonReader jsonReader, TypeAdapter<JsonElement> adapter) {
        try {
            JsonElement jsonElement = adapter.read(jsonReader);
            return fromJsonElement(jsonElement);
        } catch (IOException e) {
            throw new JsonSyntaxException(e);
        }
    }

    private JsonElement createElementWithClassInfo(Object src, JsonElement element) {

        if (element == null || element.isJsonNull()) {
            return null;
        }

        JsonObject outputObject = new JsonObject();
        outputObject.addProperty(CLASS, src.getClass().getName());

        if (element.isJsonPrimitive()) {
            return createElementFromPrimitive(element.getAsJsonPrimitive(), outputObject);
        }
        if (element.isJsonObject()) {
            return createElementFromObject(src, element.getAsJsonObject(), outputObject);
        }
        if (element.isJsonArray()) {
            return createElementFromJsonArray(src, element.getAsJsonArray(), outputObject);
        }

        throw new PolyGsonException("JsonElement not belonging to permitted types: " + element);
        // TODO: Remove this scenario
    }

    private JsonElement createElementFromPrimitive(JsonPrimitive primitive, JsonObject outputObject) {
        outputObject.add(VALUE, primitive);
        return outputObject;
    }

    private JsonElement createElementFromObject(Object src, JsonObject inputJsonObject, JsonObject outputObject) {
        if (Map.class.isAssignableFrom(src.getClass())) {
            return createElementFromMap((Map<?, ?>) src, outputObject);
        }
        return createElementFromObjectWithReflection(src, inputJsonObject, outputObject);
    }

    private JsonElement createElementFromJsonArray(Object src, JsonArray jsonArray, JsonObject outputObject) {
        Class<?> srcClass = src.getClass();

        if (Map.class.isAssignableFrom(srcClass)) {
            return createElementFromMap((Map<?, ?>) src, outputObject);
        }
        if (Collection.class.isAssignableFrom(srcClass)) {
            return createElementFromCollection((Collection<?>) src, jsonArray, outputObject);
        }
        if (srcClass.isArray()) {
            return createElementFromSimpleArray(src, jsonArray, outputObject);
        }

        throw new PolyGsonException("input Object class did not match (Collection, Array) for JsonArray element, class name: " + srcClass.getName());
    }

    // Either of JsonObject and JsonArray might be used to represent Maps (Depending on key complexity)
    private JsonElement createElementFromMap(Map<?, ?> src, JsonObject outputObject) {
        JsonArray jsonMapAsArray = new JsonArray();

        for (Map.Entry<?, ?> srcEntry : src.entrySet()) {

            JsonArray jsonMapAsArrayEntry = new JsonArray();
            JsonElement keyJsonElement = gson.toJsonTree(srcEntry.getKey());
            JsonElement valueJsonElement = gson.toJsonTree(srcEntry.getValue());
            jsonMapAsArrayEntry.add(createElementWithClassInfo(srcEntry.getKey(), keyJsonElement));          // key of target Map element
            jsonMapAsArrayEntry.add(createElementWithClassInfo(srcEntry.getValue(), valueJsonElement));      // value of target Map element

            jsonMapAsArray.add(jsonMapAsArrayEntry);
        }

        outputObject.add(MAP, jsonMapAsArray);
        return outputObject;
    }

    private JsonObject createElementFromCollection(Collection<?> src, JsonArray inputJsonArray, JsonObject outputObject) {

        if (src.size() != inputJsonArray.size()) {
            throw new PolyGsonException("Collection Object vs jsonArray size mismatch: object size = " + src.size()
                                        + ", jsonArray size = " + inputJsonArray.size());
        }

        JsonArray outputJsonArray = new JsonArray();
        Iterator<?> srcIterator = src.iterator();

        for (JsonElement inputElement : inputJsonArray) {
            outputJsonArray.add(createElementWithClassInfo(srcIterator.next(), inputElement));
        }

        outputObject.add(ARRAY, outputJsonArray);
        return outputObject;
    }

    private JsonObject createElementFromSimpleArray(Object src, JsonArray inputJsonArray, JsonObject outputObject) {
        if (Array.getLength(src) != inputJsonArray.size()) {
            throw new PolyGsonException("Array Object vs jsonArray size mismatch: object size = "
                                        + Array.getLength(src) + ", jsonArray size = " + inputJsonArray.size());
        }

        JsonArray outputJsonArray = new JsonArray();
        for (int i = 0; i < inputJsonArray.size(); ++i) {
            outputJsonArray.add(createElementWithClassInfo(Array.get(src, i), inputJsonArray.get(i)));
        }

        outputObject.add(ARRAY, outputJsonArray);
        return outputObject;
    }

    private JsonObject createElementFromObjectWithReflection(Object src, JsonObject inputJsonObject, JsonObject outputObject) {
        // TODO: maintain cache for this map (Class vs This map) in case of performance issues.
        Map<String, Field> serializedNameVsField = createSerializedNameVsFieldMap(src.getClass());

        for (Map.Entry<String, JsonElement> entry : inputJsonObject.entrySet()) {
            if (!serializedNameVsField.containsKey(entry.getKey())) {
                throw new PolyGsonException("No instance variable in class " + src.getClass().getName() + " with serialized name: " + entry.getKey());
            }

            try {
                Field field = serializedNameVsField.get(entry.getKey());
                field.setAccessible(true);
                Object instanceVariable = field.get(src);
                JsonElement withClassInfo = createElementWithClassInfo(instanceVariable, entry.getValue());
                outputObject.add(entry.getKey(), withClassInfo);

            } catch (IllegalAccessException e) {
                throw new PolyGsonException(e);
            }
        }

        return outputObject;
    }

    private Object fromJsonElement(JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return null;
        }

        if (jsonElement.isJsonObject()) {
            try {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                String className = jsonObject.get(CLASS).getAsJsonPrimitive().getAsString();
                Class<?> klass = Class.forName(className);
                jsonObject.remove(CLASS);

                if (jsonObject.has(VALUE)) {
                    return createObjectFromPrimitive(jsonObject, klass);
                }
                if (jsonObject.has(ARRAY)) {
                    return createObjectFromJsonArray(jsonObject, klass);
                }
                if (jsonObject.has(MAP)) {
                    return createObjectFromMap(jsonObject, klass);
                }
                return createObjectFromObject(jsonObject, klass);

            } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException e) {
                throw new PolyGsonException(e);
            }
        }

        throw new PolyGsonException("input element is not jsonObject or null, input element: " + jsonElement);
        // TODO: REMOVE THIS SCENARIO, preconditions?
    }

    private Object createObjectFromPrimitive(JsonObject jsonObject, Class<?> klass) {
        JsonElement value = jsonObject.get(VALUE);
        return gson.fromJson(value, klass);
    }

    private Object createObjectFromJsonArray(JsonObject jsonObject, Class<?> klass) {
        if (Collection.class.isAssignableFrom(klass)) {
            return createObjectFromCollection(jsonObject, klass);
        }

        if (klass.isArray()) {
            return createObjectFromSimpleArray(jsonObject, klass);
        }

        throw new PolyGsonException("input Object class did not match (Collection, Array) for _ARRAY element, class name: " + klass.getName());
    }

    private Map<Object, Object> createObjectFromMap(JsonObject jsonObject, Class<?> klass) {
        if (Map.class.isAssignableFrom(klass)) {
            @SuppressWarnings("unchecked")
            ObjectConstructor<Map<Object, Object>> constructor =
                    (ObjectConstructor<Map<Object, Object>>) constructorConstructor.get(TypeToken.get(klass));
            Map<Object, Object> map = constructor.construct();
            JsonArray jsonArray = jsonObject.get(MAP).getAsJsonArray();

            jsonArray.forEach(element -> {
                JsonArray subArray = element.getAsJsonArray();
                assert subArray.size() == 2; // TODO: REPLACE WITH PRECONDITION
                map.put(fromJsonElement(subArray.get(0)), fromJsonElement(subArray.get(1)));
            });

            return map;
        }

        throw new PolyGsonException("input Object class did not match (Map) for _MAP element, class name: " + klass.getName());
        // TODO: REMOVE AFTER CHECKING
    }

    private Object createObjectFromObject(JsonObject jsonObject, Class<?> klass) throws IllegalAccessException {

        @SuppressWarnings("unchecked")
        ObjectConstructor<Object> constructor = (ObjectConstructor<Object>) constructorConstructor.get(TypeToken.get(klass));
        Object object = constructor.construct();
        Map<String, Field> serializedNameVsField = createSerializedNameVsFieldMap(klass);

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            if (!serializedNameVsField.containsKey(entry.getKey())) {
                throw new PolyGsonException("No instance variable in class " + klass.getName() + " with serialized name: " + entry.getKey());
            }
            Field field = serializedNameVsField.get(entry.getKey());
            Object instanceVariable = fromJsonElement(entry.getValue());
            field.setAccessible(true);
            field.set(object, instanceVariable);
        }

        return object;
    }

    private Collection<Object> createObjectFromCollection(JsonObject jsonObject, Class<?> klass) {
        @SuppressWarnings("unchecked")
        ObjectConstructor<Collection<Object>> constructor =
                (ObjectConstructor<Collection<Object>>) constructorConstructor.get(TypeToken.get(klass));
        Collection<Object> collection = constructor.construct();
        JsonArray jsonArray = jsonObject.get(ARRAY).getAsJsonArray();
        jsonArray.forEach(element -> collection.add(fromJsonElement(element)));
        return collection;
    }

    private Object createObjectFromSimpleArray(JsonObject jsonObject, Class<?> klass) {
        JsonArray jsonArray = jsonObject.get(ARRAY).getAsJsonArray();
        int size = jsonArray.size();
        Object array = Array.newInstance(klass.getComponentType(), size);

        for (int i = 0; i < size; i++) {
            Array.set(array, i, fromJsonElement(jsonArray.get(i)));
        }
        return array;
    }

    private <T> Map<String, Field> createSerializedNameVsFieldMap(Class<T> klass) {
        List<Field> fields = getAllFields(klass);
        Map<String, Field> map = new HashMap<>();
        for (Field field : fields) {
            if (field.isSynthetic()) {
                continue;
            }
            String key = getFieldSerializedName(field);
            if (map.containsKey(key)) {
                throw new PolyGsonException("Serialized field already exists with same name: " + key);
            }
            map.put(key, field);
        }
        return map;
    }

    private String getFieldSerializedName(Field field) {
        SerializedName serializedName = field.getAnnotation(SerializedName.class);
        if (serializedName != null) {
            return serializedName.value();
        }
        return field.getName();
    }

    private <T> List<Field> getAllFields(Class<T> klass) {
        List<Field> fields = new ArrayList<>(Arrays.asList(klass.getDeclaredFields()));
        if (klass.getSuperclass() != null) {
            fields.addAll(getAllFields(klass.getSuperclass()));
        }
        return fields;
    }
}
