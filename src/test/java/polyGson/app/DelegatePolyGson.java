package polyGson.app;

import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lakshay
 * @since 06/10/21
 */
public class DelegatePolyGson {

    public static Gson CLASS_NAME_GSON =  new GsonBuilder()
            .registerTypeAdapterFactory(new MapGsonAdapterFactory())
            .registerTypeAdapterFactory(new CollectionGsonAdapterFactory())
            .registerTypeAdapterFactory(ClassNameTypeAdapterFactory.of())
            .setPrettyPrinting()
            .setFieldNamingStrategy((field) -> field.getDeclaringClass().getSimpleName() + "." + field.getName()).create();


    private static GsonBuilder createDeserializeAsLongGsonBuilder() {
        return new GsonBuilder()
                .registerTypeAdapter(new TypeToken<Map<String, Object>>() {
                        }.getType(),
                        new JsonDeserializer<Map<String, Object>>() {
                            @Override
                            @SuppressWarnings("unchecked")
                            public Map<String, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                                    throws JsonParseException {
                                return (Map<String, Object>) read(json);
                            }

                            public Object read(JsonElement in) {
                                if (in.isJsonArray()) {
                                    List<Object> list = new ArrayList<>();
                                    JsonArray arr = in.getAsJsonArray();
                                    for (JsonElement anArr : arr) {
                                        list.add(read(anArr));
                                    }
                                    return list;
                                } else if (in.isJsonObject()) {
                                    Map<String, Object> map = new LinkedTreeMap<>();
                                    JsonObject obj = in.getAsJsonObject();
                                    Set<Map.Entry<String, JsonElement>> objectMap = obj.entrySet();
                                    for (Map.Entry<String, JsonElement> entry : objectMap) {
                                        map.put(entry.getKey(), read(entry.getValue()));
                                    }
                                    return map;
                                } else if (in.isJsonPrimitive()) {
                                    JsonPrimitive primitive = in.getAsJsonPrimitive();
                                    if (primitive.isBoolean()) {
                                        return primitive.getAsBoolean();
                                    } else if (primitive.isString()) {
                                        return primitive.getAsString();
                                    } else if (primitive.isNumber() && primitive.getAsLong() != primitive.getAsFloat()) {
                                        return primitive.getAsFloat();
                                    } else if (primitive.isNumber()) {
                                        return primitive.getAsLong();
                                    }
                                }
                                return null;
                            }
                        });
    }

    public static Gson polyGsonDelegate = createDelegatePolyGsonBuilder().setPrettyPrinting().create();

    public static GsonBuilder createDelegatePolyGsonBuilder() {
//        Gson gson = new Gson();
        return new GsonBuilder()
                .registerTypeAdapterFactory(new TypeAdapterFactory() {

//                    class PrimitiveAdapter extends TypeAdapter {
//                        @Override
//                        public void write(JsonWriter out, Object value) throws IOException {
//                            if (value == null) {
//                                out.nullValue();
//                                return;
//                            }
//                            out.beginObject();
//                            out.name("__class");
//                            Class<?> klass = value.getClass();
//                            out.value("__PRIMITIVE");
//                            out.name("__value");
//                            writeViaDelegate(out, value, klass);
//                            out.endObject();
//
//                        }
//
//                        private <T> void writeViaDelegate(JsonWriter out, Object value, Class<T> klass) throws IOException {
//                            T valTypeConverted = (T) value;
//                            delegate(klass).write(out, valTypeConverted);
//                        }
//
//                        @Override
//                        public Object read(JsonReader in) throws IOException {
//                            return null;
//                        }
//                    }

                    @Override
                    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
//                        if (Object.class.isAssignableFrom(type.getRawType())) {
                            return (TypeAdapter<T>) new ObjectAdapter();
//                        }
                    }

                    class ObjectAdapter extends TypeAdapter<Object> {

                        @Override
                        public void write(JsonWriter out, Object value) throws IOException {
                            if (value == null) {
                                out.nullValue();
                                return;
                            }
                            out.beginObject();
                            out.name("__class");
                            Class<?> klass = value.getClass();
                            out.value(value.getClass().getName());
                            out.name("__object");
                            writeViaDelegate(out, value, klass);
                            out.endObject();
                        }

                        private <T> void writeViaDelegate(JsonWriter out, Object value, Class<T> klass) throws IOException {
                            T valTypeConverted = (T) value;
                            delegate(klass).write(out, valTypeConverted);
                        }

                        @Override
                        public Object read(JsonReader in) throws IOException {
                            Object retVal;
                            if (in.peek() == JsonToken.NULL) {
                                in.nextNull();
                                return null;
                            }
                            try {
                                in.beginObject();
                                String classTag = in.nextName();
                                assert "__class".equals(classTag);
                                Class<?> klass = Class.forName(in.nextString());

                                String objectTag = in.nextName();
                                assert "__object".equals(objectTag);

                                retVal = delegate(klass).read(in);

                            } catch (IllegalStateException e) {
                                throw new JsonSyntaxException(e);
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                            in.endObject();
                            return retVal;
                        }
                    }

                    private <T> TypeAdapter<T> delegate(Class<T> klass) {
                       return polyGsonDelegate.getDelegateAdapter(this, TypeToken.get(klass));
                    }
                }).registerTypeAdapterFactory(new TypeAdapterFactory() {
                    @Override
                    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                        if (!Map.class.isAssignableFrom(type.getRawType())) {
                            return null;
                        }
                        return (TypeAdapter<T>) new MapAdapter();
                    }

                    class MapAdapter extends TypeAdapter<Map> {
                        @Override
                        public void write(JsonWriter out, Map value) throws IOException {

                        }

                        @Override
                        public Map read(JsonReader in) throws IOException {
                            return null;
                        }
                    }

                }).enableComplexMapKeySerialization().setFieldNamingStrategy((field) -> field.getDeclaringClass().getSimpleName() + "." + field.getName());

    }

}
