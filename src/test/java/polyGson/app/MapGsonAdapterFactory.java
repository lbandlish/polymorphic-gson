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
import java.util.Collections;
import java.util.Map;

/**
 * @author lakshay
 * @since 19/10/21
 */
public class MapGsonAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!Map.class.isAssignableFrom(type.getRawType())) {
            return null;
        }
        return (TypeAdapter<T>) new MapAdapter(gson, type.getRawType());
    }


    class MapAdapter extends TypeAdapter<Map> {

        private final Gson gson;
        private final Class klass;

        private final ConstructorConstructor constructorConstructor = new ConstructorConstructor(Collections.emptyMap());

        public MapAdapter(Gson gson, Class klass) {
            this.gson = gson;
            this.klass = klass;
        }

        @Override
        public Map read(JsonReader in) throws IOException {
            ObjectConstructor<Map<Object, Object>> constructor =
                    (ObjectConstructor<Map<Object, Object>>) constructorConstructor.get(TypeToken.get(klass));
            Map<Object, Object> outputMap = constructor.construct();
            JsonObject jsonObject = Streams.parse(in).getAsJsonObject();
            JsonArray jsonArray = jsonObject.get("map").getAsJsonArray();
            jsonArray.forEach(element -> {
                JsonArray subArray = element.getAsJsonArray();
                assert subArray.size() == 2; // TODO: REPLACE WITH PRECONDITION
                JsonElement key = subArray.get(0);
                JsonElement value = subArray.get(1);
                outputMap.put(delegateRead(key), delegateRead(value));
            });
            return outputMap;
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
        public void write(JsonWriter out, Map map) throws IOException {
            JsonObject outputObject = new JsonObject();
            JsonArray jsonMapAsArray = new JsonArray();

            for (Object entry : map.entrySet()) {

                Object key = ((Map.Entry) entry).getKey();
                Object value = ((Map.Entry) entry).getValue();

                JsonArray jsonMapAsArrayEntry = new JsonArray();
                jsonMapAsArrayEntry.add(delegateWrite(key.getClass(), key));           // key of target Map element
                jsonMapAsArrayEntry.add(delegateWrite(value.getClass(), value));       // value of target Map element

                jsonMapAsArray.add(jsonMapAsArrayEntry);
            }

            outputObject.add("map", jsonMapAsArray);
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
