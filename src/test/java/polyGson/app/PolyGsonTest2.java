package polyGson.app;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import polyGson.PolyGson;
import polyGson.PolyGsonBuilder;
//import org.sparkproject.guava.collect.Lists;
//import org.sparkproject.guava.collect.Maps;
//import org.sparkproject.guava.collect.Sets;

public class PolyGsonTest2 {

        PolyGson polyGson;
//    Gson polyGson;
//    KryoMarshaller polyGson;
    Gson gson;

    @Before
    public void setup() {
        gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .setPrettyPrinting()
                .create();

        polyGson = new PolyGsonBuilder()
            .create();

//        polyGson = DelegatePolyGson.CLASS_NAME_GSON;
//        polyGson = new KryoMarshaller(Object.class);

    }

    private void printTimeTakenStats(long before, long after, String message) {
        System.out.println(message);
        //        System.out.println("Time taken: millis: " + (after - before));
        System.out.println("Time taken: minutes: " + TimeUnit.MILLISECONDS.toMinutes(after - before));
        System.out.println();
    }

    @Test
    public void testNull() {
//        assertNull(polyGson.toJson(null));
        assertSerializeDeserialize(null, Object.class);
//        assertNull(polyGson.fromJson(null));
    }

    @Test
    public void testPrimitive() {
        assertSerializeDeserialize(true, boolean.class);
        assertSerializeDeserialize((short) 2, short.class);
        assertSerializeDeserialize(4, int.class);
        assertSerializeDeserialize(8L, long.class);
        assertSerializeDeserialize(1.23f, float.class);
        assertSerializeDeserialize(1.2213, double.class);
        assertSerializeDeserialize('s', char.class);
    }

    @Test
    public void testEnum() {
        assertSerializeDeserialize(SampleEnum.ENUM1, SampleEnum.class);
        assertSerializeDeserialize(SampleEnum.ENUM2, SampleEnum.class);

        SampleObject sampleObject = new SampleObject("1", "2");
        sampleObject.sampleEnum = SampleEnum.ENUM1;
        assertSerializeDeserialize(sampleObject, SampleObject.class);
        sampleObject.sampleEnum = SampleEnum.ENUM2;
        assertSerializeDeserialize(sampleObject, SampleObject.class);
    }

    @Test
    public void testObject() {
        assertSerializeDeserialize("String", String.class);
        assertSerializeDeserialize(new SampleObject("1", "2"), SampleObject.class);
        assertSerializeDeserialize(new SampleSubObject("1", "2"), SampleObject.class);

        SampleSubObject sampleSubObject = new SampleSubObject("1", "2");
        sampleSubObject.subField = "subField";
        assertSerializeDeserialize(sampleSubObject, SampleSubObject.class);
        assertSerializeDeserialize(sampleSubObject, SampleInterface.class); // testing as interface
    }

    @Test
    public void testArray() {

        int[] intArray = {1, 2, 3};
        assertArrayEquals(intArray, (int[]) polyGson.fromJson(polyGson.toJson(intArray), int[].class));

        char[] charArray = {'1', '2', '3'};
        assertArrayEquals(charArray, (char[]) polyGson.fromJson(polyGson.toJson(charArray), char[].class));

        SampleObject[] objArray = {new SampleObject("1", "2"), new SampleObject("3", "4")};
        assertArrayEquals(objArray, (Object[]) polyGson.fromJson(polyGson.toJson(objArray), SampleObject[].class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCollection() {
        assertSerializeDeserialize(Lists.newArrayList(), List.class);
        assertSerializeDeserialize(Lists.newArrayList(1, 2), List.class);
        assertSerializeDeserialize(Lists.newArrayList("a", "b"), List.class);
        assertSerializeDeserialize(Lists.newArrayList(new SampleObject("1", "2"), new SampleObject("3", "4")), List.class);

        assertSerializeDeserialize(Sets.newHashSet(), Set.class);
        assertSerializeDeserialize(Sets.newHashSet(1, 2), Set.class);
        assertSerializeDeserialize(Sets.newHashSet("a", "b"), Set.class);
        Set<SampleObject> input = Sets.newHashSet(new SampleObject("1", "2"));
        Set<SampleObject> output = (Set<SampleObject>) polyGson.fromJson(polyGson.toJson(input), Set.class);
        assertEquals(input.iterator().next(), output.iterator().next());
    }

    @Test
    public void testMap() {
        assertSerializeDeserialize(Maps.newHashMap(), Map.class);
        assertSerializeDeserialize(Maps.newHashMap(Collections.singletonMap(1, 2)), Map.class);
        assertSerializeDeserialize(Maps.newHashMap(Collections.singletonMap("1", "2")), Map.class);
        assertSerializeDeserialize(Maps.newHashMap(Collections.singletonMap("key", new SampleObject("3", "4"))), Map.class);

//        Map<SampleObject, SampleObject> map = new HashMap<>();
//        map.put(new SampleObject("key1.1", "key1.2"), new SampleObject("val1.1", "val1.2"));
//        map.put(new SampleObject("key2.1", "key2.2"), new SampleObject("val2.1", "val2.2"));
//        Map<SampleObject, SampleObject> mapNew = gson.fromJson(gson.toJson(map), new TypeToken<Map<SampleObject, SampleObject>>(){}.getType());
//        assertSerializeDeserialize(map, Map.class);
    }

    private <T> void assertSerializeDeserialize(T input, Class<T> klass) {
//        String opString = polyGson.toJson(input);
//        System.out.println(opString);
//        T op = polyGson.fromJson(opString, klass);
//        System.out.println(gson.toJson(op));
        assertEquals(polyGson.fromJson(polyGson.toJson(input), klass), input);
    }

    private enum SampleEnum {
        ENUM1("fieldEnum1"),
        ENUM2("fieldEnum2") {
            @Override
            public String getField() {
                return super.getField() + "inner";
            }
        };

        private final String field;

        SampleEnum(String field) {
            this.field = field;
        }

        public String getField() {
            return field;
        }
    }

    private interface SampleInterface {

    }

    public static class SampleObject {

        private final String var1;
        public String var2;
        SampleEnum sampleEnum;

        public SampleObject(String var1, String var2) {
            this.var1 = var1;
            this.var2 = var2;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj.getClass() == getClass()) {
                SampleObject sampleObject = (SampleObject) obj;
                return StringUtils.equals(var1, sampleObject.var1)
                        && StringUtils.equals(var2, sampleObject.var2)
                        && sampleEnum == sampleObject.sampleEnum;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return var1.hashCode() * var2.hashCode();
        }
    }

    private static class SampleSubObject extends SampleObject implements SampleInterface {

        public String var2;
        String subField = "default";
        private String var1;

        public SampleSubObject(String var1, String var2) {
            super(var1, var2);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SampleSubObject) {
                return super.equals(obj) && StringUtils.equals(subField, ((SampleSubObject) obj).subField);
            }
            return false;
        }
    }

}
