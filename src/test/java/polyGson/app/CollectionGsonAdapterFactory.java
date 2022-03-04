package polyGson.app;

import com.google.gson.*;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.internal.Streams;
import com.google.gson.internal.bind.ObjectTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * @author lakshay
 * @since 19/10/21
 */
public class CollectionGsonAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!Collection.class.isAssignableFrom(type.getRawType())) {
            return null;
        }
        return (TypeAdapter<T>) new CollectionAdapter(gson, type.getRawType());
    }


    class CollectionAdapter extends TypeAdapter<Collection> {

        private final Gson gson;
        private final Class klass;

        private final ConstructorConstructor constructorConstructor = new ConstructorConstructor(Collections.emptyMap());

        public CollectionAdapter(Gson gson, Class klass) {
            this.gson = gson;
            this.klass = klass;
        }

        @Override
        public Collection read(JsonReader in) throws IOException {
            ObjectConstructor<Collection<Object>> constructor =
                    (ObjectConstructor<Collection<Object>>) constructorConstructor.get(TypeToken.get(klass));
            Collection<Object> outputColl = constructor.construct();
            JsonObject jsonObject = Streams.parse(in).getAsJsonObject();
            JsonArray jsonArray = jsonObject.get("coll").getAsJsonArray();
            jsonArray.forEach(element -> outputColl.add(delegateRead(element)));
            return outputColl;
        }

        private Object delegateRead(JsonElement jsonElement) {
            TypeAdapter<Object> delegate;

            if (jsonElement.isJsonObject()) {
                delegate = gson.getDelegateAdapter(ObjectTypeAdapter.FACTORY, TypeToken.get(Object.class));
            } else {
                delegate = gson.getDelegateAdapter(null, TypeToken.get(Object.class));
            }
            if (delegate == null) {
                throw new JsonParseException("cannot deserialize " + klass.getClass().getName());
            }
            return delegate.fromJsonTree(jsonElement);
        }

        @Override
        public void write(JsonWriter out, Collection coll) throws IOException {
            JsonObject outputObject = new JsonObject();
            JsonArray colArray = new JsonArray();

            for (Object item : coll) {
                colArray.add(delegateWrite(item.getClass(), item));
            }

            outputObject.add("coll", colArray);
            Streams.write(outputObject, out);
        }

        private <R> JsonElement delegateWrite(Class<R> klass, Object value) {
            TypeAdapter<R> delegate = gson.getDelegateAdapter(null, TypeToken.get(klass));
            if (delegate == null) {
                throw new JsonParseException("cannot serialize " + klass.getName());
            }
            return delegate.toJsonTree((R) value);
        }
    }
}
