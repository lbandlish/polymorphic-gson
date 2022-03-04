package polyGson.app;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Map.Entry;

public final class ClassNameTypeAdapterFactory implements TypeAdapterFactory {
    private final String typeFieldName;

    private ClassNameTypeAdapterFactory(String typeFieldName) {
        if (typeFieldName == null) {
            throw new NullPointerException();
        }
        this.typeFieldName = typeFieldName;
    }

    /**
     * Creates a new runtime type adapter for {@code baseType} using {@code "type"} as
     * the type field name.
     */
    public static  ClassNameTypeAdapterFactory of() {
        return new ClassNameTypeAdapterFactory("_class");
    }

    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {

        return new TypeAdapter<T>() {
            @Override
            public T read(JsonReader in) throws IOException {
                JsonElement jsonElement = Streams.parse(in);
                if (!jsonElement.isJsonObject()) {
                    return delegateRead(type.getRawType(), jsonElement);
                }
                JsonElement labelJsonElement = jsonElement.getAsJsonObject().remove(typeFieldName);
                if (labelJsonElement == null) {
                    throw new JsonParseException("cannot deserialize " + type.getClass().getName()
                            + " because it does not define a field named " + typeFieldName);
                }
                String label = labelJsonElement.getAsString();

                try {
                    return delegateRead(Class.forName(label), jsonElement);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

            }

            private <R> T delegateRead(Class<R> klass, JsonElement jsonElement) {
                TypeAdapter<R> delegate = gson.getDelegateAdapter(ClassNameTypeAdapterFactory.this, TypeToken.get(klass));
                if (delegate == null) {
                    throw new JsonParseException("cannot deserialize " + type.getClass().getName());
                }
                return (T) delegate.fromJsonTree(jsonElement);
            }

            @Override public void write(JsonWriter out, T value) throws IOException {
                Class<?> klass = value.getClass();
                JsonElement jsonElement = delegateWrite(klass, value);
                if (!jsonElement.isJsonObject()) {
                    Streams.write(jsonElement, out);
                    return;
                }

                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (jsonObject.has(typeFieldName)) {
                    throw new JsonParseException("cannot serialize " + klass.getName()
                            + " because it already defines a field named " + typeFieldName);
                }
                JsonObject clone = new JsonObject();
                clone.add(typeFieldName, new JsonPrimitive(klass.getName()));
                for (Entry<String, JsonElement> e : jsonObject.entrySet()) {
                    clone.add(e.getKey(), e.getValue());
                }
                Streams.write(clone, out);
            }

                private <R> JsonElement delegateWrite(Class<R> klass, T value) {
                    TypeAdapter<R> delegate = gson.getDelegateAdapter(ClassNameTypeAdapterFactory.this, TypeToken.get(klass));
                    if (delegate == null) {
                        throw new JsonParseException("cannot serialize " + klass.getName());
                    }
                    return delegate.toJsonTree((R) value);
                }
        }.nullSafe();
    }
}
