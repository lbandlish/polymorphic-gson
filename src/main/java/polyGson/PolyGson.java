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
import java.lang.reflect.Modifier;
import java.util.*;

public class PolyGson {

    public static final String CLASS = "__class";
    public static final String VALUE = "__value";
    public static final String ENUM = "__enum";
    public static final String ARRAY = "__array";
    public static final String COLLECTION = "__coll";
    public static final String MAP = "__map";

    private final ConstructorConstructor constructorConstructor = new ConstructorConstructor(Collections.emptyMap());
    private final Gson gson;

    public PolyGson() {
        gson = new GsonBuilder().create();
    }

    PolyGson(Gson gson) {
        this.gson = gson;
    }

    public String toJson(Object src) {
        if (src == null) {
            return null;
        }
        JsonElement jsonElement = createElementWithClassInfo(src);
        return gson.toJson(jsonElement);
    }

    @SuppressWarnings("unchecked")
    public <T> T fromJson(String json, Class<T> klass) {
        return (T) fromJson(json);
    }

    public Object fromJson(String json) {
        if (json == null) {
            return null;
        }
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

    private JsonElement createElementWithClassInfo(Object src) {

        // null
        if (src == null) {
            return JsonNull.INSTANCE;
        }

        // primitive
        if (isBoxedPrimitive(src)) {
            return createElementFromPrimitive(src);
        }

        // enum
        if (Enum.class.isAssignableFrom(src.getClass())) {
            return createElementFromEnum(src);
        }

        // arrays
        if (src.getClass().isArray()) {
            return createElementFromArray(src);
        }

        // collections
        if (Collection.class.isAssignableFrom(src.getClass())) {
            return createElementFromCollection((Collection<?>) src);
        }

        // maps
        if (Map.class.isAssignableFrom(src.getClass())) {
            return createElementFromMap((Map<?, ?>) src);
        }

        // other objects
        return createElementFromObject(src);
    }

    private boolean isBoxedPrimitive(Object src) {
        return (src instanceof Boolean) ||
                (src instanceof Number) ||
                (src instanceof String) ||
                (src instanceof Character);
    }

    private JsonElement createElementFromPrimitive(Object src) {

        JsonObject outputObject = new JsonObject();
        outputObject.addProperty(CLASS, src.getClass().getName());

        if (src instanceof Boolean) {
            outputObject.add(VALUE, new JsonPrimitive((Boolean) src));
        } else if (src instanceof Number) {
            outputObject.add(VALUE, new JsonPrimitive((Number) src));
        } else if (src instanceof String) {
            outputObject.add(VALUE, new JsonPrimitive((String) src));
        } else {
            outputObject.add(VALUE, new JsonPrimitive((Character) src));
        }
        return outputObject;
    }

    @SuppressWarnings("rawtypes")
    private JsonElement createElementFromEnum(Object src) {
        JsonObject outputObject = new JsonObject();
        outputObject.addProperty(CLASS, ((Enum) src).getDeclaringClass().getName());
        outputObject.add(ENUM, new JsonPrimitive(((Enum) src).name()));
        return outputObject;
    }

    private JsonObject createElementFromArray(Object src) {
        JsonObject outputObject = new JsonObject();
        JsonArray outputJsonArray = new JsonArray();
        for (int i = 0; i < Array.getLength(src); ++i) {
            outputJsonArray.add(createElementWithClassInfo(Array.get(src, i)));
        }
        outputObject.addProperty(CLASS, src.getClass().getName());
        outputObject.add(ARRAY, outputJsonArray);
        return outputObject;
    }

    private JsonObject createElementFromCollection(Collection<?> src) {
        JsonObject outputObject = new JsonObject();
        JsonArray outputJsonArray = new JsonArray();
        src.stream().map(this::createElementWithClassInfo).forEach(outputJsonArray::add);
        outputObject.addProperty(CLASS, src.getClass().getName());
        outputObject.add(COLLECTION, outputJsonArray);
        return outputObject;
    }

    private JsonElement createElementFromMap(Map<?, ?> src) {
        JsonObject outputObject = new JsonObject();
        JsonArray jsonMapAsArray = new JsonArray();

        for (Map.Entry<?, ?> srcEntry : src.entrySet()) {
            JsonArray jsonMapAsArrayEntry = new JsonArray();
            jsonMapAsArrayEntry.add(createElementWithClassInfo(srcEntry.getKey()));           // key of target Map element
            jsonMapAsArrayEntry.add(createElementWithClassInfo(srcEntry.getValue()));         // value of target Map element

            jsonMapAsArray.add(jsonMapAsArrayEntry);
        }

        outputObject.addProperty(CLASS, src.getClass().getName());
        outputObject.add(MAP, jsonMapAsArray);
        return outputObject;
    }

    private JsonObject createElementFromObject(Object src) {
        JsonObject outputObject = new JsonObject();
        outputObject.addProperty(CLASS, src.getClass().getName());

        // TODO: maintain cache for this map (Class vs This map) in case of performance issues.
        Map<String, Field> serializedNameVsField = new HashMap<>();
        populateSerializedNameVsFieldMap(src.getClass(), serializedNameVsField);

        serializedNameVsField.forEach((variableName, field) -> {
            try {
                field.setAccessible(true);
                Object variableValueObject = field.get(src);
                if (variableValueObject != null && variableValueObject != src) {    // variableValueObject == src implies recursive reference to same object
                    JsonElement variableValue = createElementWithClassInfo(variableValueObject);
                    outputObject.add(variableName, variableValue);
                }
            } catch (IllegalAccessException e) {
                throw new PolyGsonException(e);
            }
        });

        return outputObject;
    }

    private Object fromJsonElement(JsonElement jsonElement) {
        // null
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return null;
        }

        assert(jsonElement.isJsonObject());

        try {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String className = jsonObject.get(CLASS).getAsJsonPrimitive().getAsString();
            Class<?> klass = Class.forName(className);
            jsonObject.remove(CLASS);

            // primitive
            if (jsonObject.has(VALUE)) {
                return createPrimitiveFromJson(jsonObject, klass);
            }
            // enums
            if (jsonObject.has(ENUM)) {
                return createEnumFromJson(jsonObject, klass);
            }
            // arrays
            if (jsonObject.has(ARRAY)) {
                return createArrayFromJson(jsonObject, klass);
            }
            // collections
            if (jsonObject.has(COLLECTION)) {
                return createCollectionFromJson(jsonObject, klass);
            }
            // maps
            if (jsonObject.has(MAP)) {
                return createMapFromJson(jsonObject, klass);
            }
            // other objects
            return createObjectFromJson(jsonObject, klass);

        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException e) {
            throw new PolyGsonException(e);
        }
    }

    private Object createPrimitiveFromJson(JsonObject jsonObject, Class<?> klass) {
        JsonPrimitive value = jsonObject.getAsJsonPrimitive(VALUE);
        return gson.fromJson(value, klass);
    }

    @SuppressWarnings("rawtypes")
    private Object createEnumFromJson(JsonObject jsonObject, Class<?> klass) {
        assert (klass.isEnum());
        String enumString = jsonObject.getAsJsonPrimitive(ENUM).getAsString();
        for (Object constant : klass.getEnumConstants()) {
            if (((Enum) constant).name().equals(enumString)) {
                return constant;
            }
        }
        throw new PolyGsonException("Cannot find enum: " + enumString + " in class: " + klass.getName());
    }

    private Object createArrayFromJson(JsonObject jsonObject, Class<?> klass) {
        assert (klass.isArray());
        JsonArray jsonArray = jsonObject.get(ARRAY).getAsJsonArray();
        int size = jsonArray.size();
        Object outputArray = Array.newInstance(klass.getComponentType(), size);

        for (int i = 0; i < size; i++) {
            Array.set(outputArray, i, fromJsonElement(jsonArray.get(i)));
        }
        return outputArray;
    }

    @SuppressWarnings("unchecked")
    private Collection<Object> createCollectionFromJson(JsonObject jsonObject, Class<?> klass) {
        assert (Collection.class.isAssignableFrom(klass));
        ObjectConstructor<Collection<Object>> constructor =
                (ObjectConstructor<Collection<Object>>) constructorConstructor.get(TypeToken.get(klass));
        Collection<Object> outputCollection = constructor.construct();
        JsonArray jsonArray = jsonObject.get(COLLECTION).getAsJsonArray();
        jsonArray.forEach(element -> outputCollection.add(fromJsonElement(element)));
        return outputCollection;
    }

    @SuppressWarnings("unchecked")
    private Map<Object, Object> createMapFromJson(JsonObject jsonObject, Class<?> klass) {
        assert(Map.class.isAssignableFrom(klass));
        ObjectConstructor<Map<Object, Object>> constructor =
                (ObjectConstructor<Map<Object, Object>>) constructorConstructor.get(TypeToken.get(klass));
        Map<Object, Object> outputMap = constructor.construct();
        JsonArray jsonArray = jsonObject.get(MAP).getAsJsonArray();

        jsonArray.forEach(element -> {
            JsonArray subArray = element.getAsJsonArray();
            assert subArray.size() == 2; // TODO: REPLACE WITH PRECONDITION
            outputMap.put(fromJsonElement(subArray.get(0)), fromJsonElement(subArray.get(1)));
        });
        return outputMap;
    }

    private Object createObjectFromJson(JsonObject jsonObject, Class<?> klass) throws IllegalAccessException {

        @SuppressWarnings("unchecked")
        ObjectConstructor<Object> constructor = (ObjectConstructor<Object>) constructorConstructor.get(TypeToken.get(klass));
        Object outputObject = constructor.construct();
        Map<String, Field> serializedNameVsField = new HashMap<>();
        populateSerializedNameVsFieldMap(klass, serializedNameVsField);

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String fieldName = entry.getKey();
            if (!serializedNameVsField.containsKey(fieldName)) {
                throw new PolyGsonException("No instance variable in class " + klass.getName() + " with polyGson serialized name: " + fieldName);
            }
            Field field = serializedNameVsField.get(fieldName);
            Object instanceVariable = fromJsonElement(entry.getValue());
            field.setAccessible(true);
            field.set(outputObject, instanceVariable);
        }

        return outputObject;
    }

    private void populateSerializedNameVsFieldMap(Class<?> klass, Map<String, Field> map) {
        for (Field field : klass.getDeclaredFields()) {
            if (skipFieldSerialization(field)) {
                continue;
            }
            String key = getFieldSerializedName(field);
            if (map.containsKey(key)) {
                key = klass.getName() + "." + key;
            }
            map.put(key, field);
        }
        if (klass.getSuperclass() != null) {
            populateSerializedNameVsFieldMap(klass.getSuperclass(), map);
        }
    }

    private boolean skipFieldSerialization(Field field) {
        Class<?> klass = field.getClass();

        if (klass.isAnonymousClass() || klass.isLocalClass()) {
            return true;
        }

        if (field.isSynthetic()) {
            return true;
        }

        int mod = field.getModifiers();
        return Modifier.isStatic(mod) || Modifier.isTransient(mod);
    }

    private String getFieldSerializedName(Field field) {
        SerializedName serializedName = field.getAnnotation(SerializedName.class);
        if (serializedName != null) {
            return serializedName.value();
        }
        return field.getName();
    }
}
