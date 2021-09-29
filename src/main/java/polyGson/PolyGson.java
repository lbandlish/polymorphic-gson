package polyGson;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.internal.Primitives;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.*;
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

    public Gson getGson() {
        return gson;
    }

    // TODO: Can have target type and use it to call gson.toJsonTree(src, type)
    // (as current implementation uses .getClass() which skips generic information)
    public String toJson(Object src) {
        if (src == null) {
            return null;
        }
        JsonElement jsonElement = gson.toJsonTree(src);
        JsonElement withClassInfo = createObjectWithClassInfo(src, jsonElement);
        return gson.toJson(withClassInfo);
    }

    // TODO: Force it to provide target class type - especially in serializer Factory.
    public Object fromJson(String json) throws IOException {
        StringReader reader = new StringReader(json);
        JsonReader jsonReader = new JsonReader(reader);
        return fromJson(jsonReader);
    }

    private Object fromJson(JsonReader jsonReader) throws IOException {
//        JsonToken token = jsonReader.peek();
//        if (token == JsonToken.BEGIN_OBJECT) {
//            JsonObject jsonObject = new JsonObject();
//            jsonReader.beginObject();
//            while (jsonReader.hasNext()) {
//                String property = jsonReader.nextName();
//                final TypeAdapter<JsonElement> adapter = gson.getAdapter(JsonElement.class);
//                final JsonElement jsonElement = adapter.read(jsonReader);
//                jsonObject.add(property, jsonElement);
//            }
//            jsonReader.endObject();
//            try {
//                return JsonObject_to_object(jsonObject);
//            } catch (JsonSyntaxException e) {
//                e.printStackTrace();
//            }
//        }
        final TypeAdapter<JsonElement> adapter = gson.getAdapter(JsonElement.class);
        final JsonElement jsonElement = adapter.read(jsonReader);
        try {
            return fromJsonElement(jsonElement);
        } catch (JsonSyntaxException e) {
            throw new PolyGsonException(e);
        }
    }

    private JsonElement createObjectWithClassInfo(Object src, JsonElement element) {

//        JsonObject outputObject = new JsonObject();

        if (element == null) {
            return null;
        } else if (element.isJsonPrimitive()) {
            return createElementForPrimitive(src, element.getAsJsonPrimitive());
        } else if (element.isJsonObject()) {
            return createElementForObject(src, element.getAsJsonObject());
        } else if (element.isJsonArray()) {
            return createElementForJsonArray(src, element.getAsJsonArray());
        } else if (element.isJsonNull()) {
            return null;
        }

        throw new PolyGsonException("JsonElement not belonging to permitted types: " + element);
        // TODO: Remove this scenario
    }

    private JsonElement createElementForPrimitive(Object src, JsonPrimitive primitive) {
        JsonObject primitiveObject = new JsonObject();
        primitiveObject.addProperty(CLASS, src.getClass().getName());
        primitiveObject.add(VALUE, primitive);
        return primitiveObject;
    }

    private JsonElement createElementForObject(Object src, JsonObject inputJsonObject) {
        if (Map.class.isAssignableFrom(src.getClass())) {
            return createElementForMap((Map<?, ?>) src);
        }
        return createElementForObjectWithReflection(src, inputJsonObject);
    }

    private JsonElement createElementForJsonArray(Object src, JsonArray jsonArray) {
        Class<?> srcClass = src.getClass();

        if (Map.class.isAssignableFrom(srcClass)) {
            return createElementForMap((Map<?, ?>) src);
        } else if (Collection.class.isAssignableFrom(srcClass)) {
            return createElementForCollection((Collection<?>) src, jsonArray);
        } else if (srcClass.isArray()) {
            return createElementForSimpleArray(src, jsonArray);
        }

        throw new PolyGsonException("input Object class did not match (Map, Collection, Array) for JsonArray element");
    }

    private JsonObject createElementForSimpleArray(Object src, JsonArray inputJsonArray) {
        if (Array.getLength(src) != inputJsonArray.size()) {
            throw new PolyGsonException("Array Object vs jsonArray size mismatch: object size = " + Array.getLength(src) + ", jsonArray size = " + inputJsonArray.size());
        }

        JsonArray outputJsonArray = new JsonArray();
        for (int i = 0; i < inputJsonArray.size(); ++i) {
            outputJsonArray.add(createObjectWithClassInfo(Array.get(src, i), inputJsonArray.get(i)));
        }

        JsonObject outputObject = new JsonObject();
        outputObject.addProperty(CLASS, src.getClass().getName());
        outputObject.add(ARRAY, outputJsonArray);
        return outputObject;
    }

    private JsonObject createElementForCollection(Collection<?> src, JsonArray inputJsonArray) {

        if (src.size() != inputJsonArray.size()) {
            throw new PolyGsonException("Collection Object vs jsonArray size mismatch: object size = " + src.size() + ", jsonArray size = " + inputJsonArray.size());
        }

        JsonArray outputJsonArray = new JsonArray();
        Iterator<?> srcIterator = src.iterator();

        for (JsonElement jsonElement : inputJsonArray) {
            outputJsonArray.add(createObjectWithClassInfo(srcIterator.next(), jsonElement));
        }

        JsonObject outputObject = new JsonObject();
        outputObject.addProperty(CLASS, src.getClass().getName());
        outputObject.add(ARRAY, outputJsonArray);
        return outputObject;
    }

    private JsonElement createElementForMap(Map<?, ?> src) {
        JsonObject outputObject = new JsonObject();
        outputObject.addProperty(CLASS, src.getClass().getName());

        JsonArray jsonMapAsArray = new JsonArray();

        for (Map.Entry<?, ?> srcEntry : src.entrySet()) {

            JsonArray jsonMapAsArrayEntry = new JsonArray();
            JsonElement key = gson.toJsonTree(srcEntry.getKey());
            JsonElement value = gson.toJsonTree(srcEntry.getValue());
            jsonMapAsArrayEntry.add(createObjectWithClassInfo(srcEntry.getKey(), key));          // key of target Map element
            jsonMapAsArrayEntry.add(createObjectWithClassInfo(srcEntry.getValue(), value));      // value of target Map element

            jsonMapAsArray.add(jsonMapAsArrayEntry);
        }

        outputObject.add(MAP, jsonMapAsArray);
        return outputObject;
    }

    private JsonObject createElementForObjectWithReflection(Object src, JsonObject inputJsonObject) {
        JsonObject outputObject = new JsonObject();
        outputObject.addProperty(CLASS, src.getClass().getName());

        // TODO: maintain cache for this map (Class vs This map) in case of performance issues.
        Map<String, Field> serializedNameVsField = createSerializedNameVsFieldMap(src.getClass());

        for (Map.Entry<String, JsonElement> entry : inputJsonObject.entrySet()) {
//            try {
            if (!serializedNameVsField.containsKey(entry.getKey())) {
                throw new PolyGsonException("No instance variable in class " + src.getClass().getName() + " with serialized name: " + entry.getKey());
            }

            try {
                Field field = serializedNameVsField.get(entry.getKey());
                field.setAccessible(true);
                Object instanceVariable = field.get(src);
                JsonElement withClassInfo = createObjectWithClassInfo(instanceVariable, entry.getValue());
                outputObject.add(entry.getKey(), withClassInfo);

            } catch (IllegalAccessException e) {
                throw new PolyGsonException(e);
            }

//            } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
//                e.printStackTrace();
//            }
        }

        return outputObject;
    }

    // TODO: Force it to provide target class type - especially in serializer.
//    @SuppressWarnings({"unchecked", "rawtypes"})
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
                    JsonElement jsonElement1 = jsonObject.get(VALUE);
                    return gson.fromJson(jsonElement1, klass);
                }

                if (jsonObject.has(ARRAY)) {
                    if (Collection.class.isAssignableFrom(klass)) {
                        ObjectConstructor<Collection<Object>> constructor =
                                (ObjectConstructor<Collection<Object>>) constructorConstructor.get(TypeToken.get(klass));
                        Collection<Object> collection = constructor.construct();
                        JsonArray jsonArray = jsonObject.get(ARRAY).getAsJsonArray();
                        jsonArray.forEach(element -> collection.add(fromJsonElement(element)));
                        return collection;
                    }

                    if (klass.isArray()) {
                        JsonArray jsonArray = jsonObject.get(ARRAY).getAsJsonArray();
                        int size = jsonArray.size();
                        Object array = Array.newInstance(klass.getComponentType(), size);

                        Iterator<JsonElement> iterator = jsonArray.iterator();
                        for (int i = 0; i < size; i++) {
                            Array.set(array, i, fromJsonElement(iterator.next()));
                        }
                        return array;
                    }

                    throw new PolyGsonException(klass.getName());
                }

                if (jsonObject.has(MAP)) {
                    if (Map.class.isAssignableFrom(klass)) {
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

                    throw new PolyGsonException(klass.getName());
                }

                // JsonObject

                Object obj = createObjectForClass(klass);
                Map<String, Field> serializedNameVsField = createSerializedNameVsFieldMap(klass);

                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    if (!serializedNameVsField.containsKey(entry.getKey())) {
                        throw new PolyGsonException("No instance variable in class " + klass.getName() + " with serialized name: " + entry.getKey());
                    }
                    Field field = serializedNameVsField.get(entry.getKey());
                    Object instanceVariable = fromJsonElement(entry.getValue());
                    field.setAccessible(true);
                    field.set(obj, instanceVariable);
                }

                return obj;

            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new PolyGsonException(e);
            }
        }

        throw new PolyGsonException("input element is not jsonObject or null, input element: " + jsonElement);
    }

    private Object createObjectForClass(Class<?> klass) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor<?>[] declaredConstructors = klass.getDeclaredConstructors();
        Constructor<?> constructor = null;
        for (Constructor<?> declaredConstructor : declaredConstructors) {
            constructor = declaredConstructor;
            if (constructor.getGenericParameterTypes().length == 0) {
                break;
            }
        }
        constructor.setAccessible(true);
        int numArgs = constructor.getGenericParameterTypes().length;
        return constructor.newInstance(new Object[numArgs]);
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
