package polyGsonOld;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.internal.Primitives;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import javax.xml.transform.Transformer;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.*;
import java.util.*;

public class PolyGson {

    Gson gson;

    public PolyGson() {
        gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    }

    PolyGson(Gson gson) {
        this.gson = gson;
    }

    public Gson getGson() {
        return gson;
    }

    public String toJson(Object src) {
        JsonElement jsonElement = gson.toJsonTree(src);
        JsonElement jsonElement_updated = update_JsonElement(src, jsonElement);
        return gson.toJson(jsonElement_updated);
    }

    public JsonElement update_JsonElement(Object src, JsonElement jsonElement) {

        if (jsonElement.isJsonPrimitive()) {
            JsonObject jsonElement_updated = new JsonObject();
            jsonElement_updated.addProperty("_class", src.getClass().getName());
            jsonElement_updated.add("_value", jsonElement);
            return jsonElement_updated;
        } else if (jsonElement.isJsonObject()) {

            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonObject new_jsonObject = new JsonObject();
            new_jsonObject.addProperty("_class", src.getClass().getName());

            Type typeOfT = (Type) src.getClass();
            TypeToken<?> typeToken = (TypeToken<?>) TypeToken.get(typeOfT);
            Class<?> rawType = typeToken.getRawType();
            if (Map.class.isAssignableFrom(rawType)) {

                JsonArray new_jsonArray = new JsonArray();

                Map<?, ?> mp = (Map<?, ?>) src;
                for (Map.Entry<?, ?> entry : mp.entrySet()) {
                    JsonArray new_jsonArray1 = new JsonArray();
                    JsonElement jsonElement1 = gson.toJsonTree(entry.getKey());
                    JsonElement jsonElement2 = gson.toJsonTree(entry.getValue());
                    new_jsonArray1.add(update_JsonElement(entry.getKey(), jsonElement1));
                    new_jsonArray1.add(update_JsonElement(entry.getValue(), jsonElement2));
                    new_jsonArray.add(new_jsonArray1);
                }

                new_jsonObject.add("_map", new_jsonArray);
                return new_jsonObject;
            }

            // TODO: maintain cache for this map (Class vs This map) in case of performance issues.
            Map<String, Field> serializedNameVsField = createSerializedNameVsFieldMap(src.getClass());

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                try {
                    //                    Class<?> cl = src.getClass();
                    //                    Field field = null;
                    //                    for(Class<?> cl1 = cl; cl1 != null; cl1 = cl1.getSuperclass()) {
                    //                        try {
                    //                            field = cl1.getDeclaredField(entry.getKey());
                    //                            break;
                    //                        }
                    //                        catch (NoSuchFieldException ex) {
                    //                        }
                    //                    }
                    if (!serializedNameVsField.containsKey(entry.getKey())) {
                        throw new PolyGsonException("No instance variable in class " + src.getClass().getName() + " with serialized name: " + entry.getKey());
                    }
                    Field field = serializedNameVsField.get(entry.getKey());
                    field.setAccessible(true);
                    Object x = field.get(src);
                    JsonElement updated_JsonElement = update_JsonElement(x, entry.getValue());
                    new_jsonObject.add(entry.getKey(), updated_JsonElement);
                } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            return new_jsonObject;
        } else if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            JsonObject new_jsonObject = new JsonObject();
            new_jsonObject.addProperty("_class", src.getClass().getName());

            Type typeOfT = (Type) src.getClass();
            TypeToken<?> typeToken = (TypeToken<?>) TypeToken.get(typeOfT);
            Class<?> rawType = typeToken.getRawType();
            if (Collection.class.isAssignableFrom(rawType)) {
                JsonArray new_jsonArray = new JsonArray();
                Iterator<JsonElement> iterator = jsonArray.iterator();
                Collection<?> collec = (Collection<?>) src;
                Iterator<?> iterator1 = collec.iterator();

                while (iterator.hasNext()) {
                    JsonElement jsonElement_iterator = iterator.next();
                    new_jsonArray.add(update_JsonElement(iterator1.next(), jsonElement_iterator));
                }

                new_jsonObject.add("_array", new_jsonArray);
                return new_jsonObject;
            }

            if (typeOfT instanceof GenericArrayType || typeOfT instanceof Class && ((Class<?>) typeOfT).isArray()) {
                JsonArray new_jsonArray = new JsonArray();
                for (int i = 0; i < jsonArray.size(); ++i) {
                    new_jsonArray.add(update_JsonElement(Array.get(src, i), jsonArray.get(i)));
                }

                new_jsonObject.add("_array", new_jsonArray);
                return new_jsonObject;
            }

            if (Map.class.isAssignableFrom(rawType)) {

                JsonArray new_jsonArray = new JsonArray();

                Map<?, ?> mp = (Map<?, ?>) src;
                for (Map.Entry<?, ?> entry : mp.entrySet()) {
                    JsonArray new_jsonArray1 = new JsonArray();
                    JsonElement jsonElement1 = gson.toJsonTree(entry.getKey());
                    JsonElement jsonElement2 = gson.toJsonTree(entry.getValue());
                    new_jsonArray1.add(update_JsonElement(entry.getKey(), jsonElement1));
                    new_jsonArray1.add(update_JsonElement(entry.getValue(), jsonElement2));
                    new_jsonArray.add(new_jsonArray1);
                }

                new_jsonObject.add("_map", new_jsonArray);
                return new_jsonObject;
            }
        }
        return jsonElement;
    }

    public SourceClass variableName = () -> System.out.println("WASSUP");
    public SourceClass variableName2 = new SourceClass() {
        @Override
        public void spit() {
            System.out.println("WASSUP");
        }
    };
    public final SourceClass variableName3 = () -> System.out.println("WASSUP");

    public interface SourceClass {
        public abstract void spit();
    }

    private <T> Map<String, Field> createSerializedNameVsFieldMap(Class<T> klass) {
        List<Field> fields = getAllFields(klass);
        Map<String, Field> map = new HashMap<>();
        for (Field field : fields) {
            if ("this$0".equals(field.getName())) {
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

    // TODO: Force it to provide target class type - especially in serializer.
    @SuppressWarnings("unchecked")
    public <T> T fromJson(String json) throws IOException {
        StringReader reader = new StringReader(json);
        JsonReader jsonReader = new JsonReader(reader);
        JsonToken token = jsonReader.peek();
        if (token == JsonToken.BEGIN_OBJECT) {
            JsonObject jsonObject = new JsonObject();
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                String property = jsonReader.nextName();
                final TypeAdapter<JsonElement> adapter = gson.getAdapter(JsonElement.class);
                final JsonElement jsonElement = adapter.read(jsonReader);
                jsonObject.add(property, jsonElement);
            }
            jsonReader.endObject();
            try {
                return (T) JsonObject_to_object(jsonObject);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
        T object = null;
        return object;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T JsonObject_to_object(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            try {
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                String class_name = jsonObject.get("_class").getAsJsonPrimitive().getAsString();
                jsonObject.remove("_class");

                if (jsonObject.has("_value")) {
                    JsonElement jsonElement1 = jsonObject.get("_value");
                    return (T) gson.fromJson(jsonElement1, Class.forName(class_name));
                }

                if (jsonObject.has("_array")) {
                    Type typeOfT = (Type) Class.forName(class_name);
                    TypeToken<T> typeToken = (TypeToken<T>) TypeToken.get(typeOfT);
                    Map<Type, InstanceCreator<?>> instanceCreators = Collections.<Type, InstanceCreator<?>>emptyMap();
                    ConstructorConstructor constructorConstructor = new ConstructorConstructor(instanceCreators);
                    Class<? super T> rawType = typeToken.getRawType();

                    if (Collection.class.isAssignableFrom(rawType)) {
                        ObjectConstructor<? extends Collection<?>> constructor =
                            (ObjectConstructor<? extends Collection<?>>) constructorConstructor.get(typeToken);
                        Collection<?> collection = constructor.construct();

                        JsonArray jsonArray = jsonObject.get("_array").getAsJsonArray();
                        Iterator<JsonElement> iterator = jsonArray.iterator();
                        while (iterator.hasNext()) {
                            collection.add(JsonObject_to_object(iterator.next()));
                        }
                        return (T) collection;
                    }

                    if (typeOfT instanceof GenericArrayType || typeOfT instanceof Class && ((Class<?>) typeOfT).isArray()) {
                        JsonArray jsonArray = jsonObject.get("_array").getAsJsonArray();
                        Iterator<JsonElement> iterator = jsonArray.iterator();
                        Class<?> componentType = $Gson$Types.getRawType($Gson$Types.getArrayComponentType(typeOfT));
                        List list = new ArrayList<Object>();
                        while (iterator.hasNext()) {
                            JsonElement jsonElement_iterator = iterator.next();
                            list.add(JsonObject_to_object(jsonElement_iterator));
                        }
                        int size = list.size();
                        Object array = Array.newInstance(componentType, size);
                        for (int i = 0; i < size; i++) {
                            Array.set(array, i, list.get(i));
                        }
                        return (T) array;
                    }
                }

                if (jsonObject.has("_map")) {
                    Type typeOfT = (Type) Class.forName(class_name);
                    TypeToken<T> typeToken = (TypeToken<T>) TypeToken.get(typeOfT);
                    Map<Type, InstanceCreator<?>> instanceCreators = Collections.<Type, InstanceCreator<?>>emptyMap();
                    ConstructorConstructor constructorConstructor = new ConstructorConstructor(instanceCreators);
                    Class<? super T> rawType = typeToken.getRawType();

                    if (Map.class.isAssignableFrom(rawType)) {
                        ObjectConstructor<? extends Map<?, ?>> constructor =
                            (ObjectConstructor<? extends Map<?, ?>>) constructorConstructor.get(typeToken);
                        Map<?, ?> map = constructor.construct();
                        JsonArray jsonArray = jsonObject.get("_map").getAsJsonArray();
                        Iterator<JsonElement> iterator = jsonArray.iterator();
                        while (iterator.hasNext()) {
                            JsonArray jsonArray1 = iterator.next().getAsJsonArray();
                            map.put(JsonObject_to_object(jsonArray1.get(0)),
                                    JsonObject_to_object(jsonArray1.get(1)));
                        }
                        return (T) map;
                    }
                }


                Class<?> cls = (Class<?>) Class.forName(class_name);

                Constructor[] ctors = cls.getDeclaredConstructors();
                Constructor ctor = null;
                for (int i = 0; i < ctors.length; i++) {
                    ctor = ctors[i];
                    if (ctor.getGenericParameterTypes().length == 0)
                        break;
                }
                ctor.setAccessible(true);

                Type[] parameter_types = ctor.getGenericParameterTypes();
                Object[] parameters = new Object[parameter_types.length];
                for (int i = 0; i < parameter_types.length; ++i) {
                    parameters[i] = null;
                }

                Object obj = ctor.newInstance(parameters);


                Set<Map.Entry<String, JsonElement>> members = jsonObject.entrySet();
                for (Map.Entry<String, JsonElement> entry : members) {
                    Map<String, Field> serializedNameVsField = createSerializedNameVsFieldMap(cls);
                    //                    Field field = null;
                    //                    for(Class<?> cl1 = cls; cl1 != null; cl1 = cl1.getSuperclass()) {
                    //                        try {
                    //                            field = cl1.getDeclaredField(entry.getKey());
                    //                            break;
                    //                        } catch (NoSuchFieldException ex) {
                    //                        }
                    //                    }
                    if (!serializedNameVsField.containsKey(entry.getKey())) {
                        throw new PolyGsonException("No instance variable in class " + cls.getName() + " with serialized name: " + entry.getKey());
                    }
                    Field field = serializedNameVsField.get(entry.getKey());
                    field.setAccessible(true);
                    Object field_object = JsonObject_to_object(entry.getValue());
                    field.set(obj, field_object);
                }

                return (T) Primitives.wrap(cls).cast(obj);

            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        T object = null;
        return object;
    }
}
