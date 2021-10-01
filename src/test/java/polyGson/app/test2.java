//package polyGson.app;
//
///**
// * @author lakshay
// * @since 01/10/21
// */
//
//import static org.junit.Assert.assertArrayEquals;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNull;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.List;
//import java.util.Set;
//import java.util.concurrent.TimeUnit;
//import org.junit.Before;
//import org.junit.Test;
//import polyGson.PolyGson;
//import polyGson.PolyGsonBuilder;
//import org.sparkproject.guava.collect.Lists;
//import org.sparkproject.guava.collect.Maps;
//import org.sparkproject.guava.collect.Sets;
//
//public class test2 {
//
//    PolyGson polyGson;
//    Gson gson;
//
//    @Before
//    public void setup() {
//        gson = new GsonBuilder()
//                .enableComplexMapKeySerialization()
//                .create();
//
//        polyGson = new PolyGsonBuilder()
//                .create();
//
//    }
//
//    @Test
//    public void testNull() {
//        assertNull(polyGson.toJson(null));
//        assertNull(polyGson.fromJson(null));
//    }
//
//    @Test
//    public void testPrimitive() {
//        assertSerializeDeserialize(true, boolean.class);
//        assertSerializeDeserialize((short) 2, short.class);
//        assertSerializeDeserialize(4, int.class);
//        assertSerializeDeserialize(8L, long.class);
//        assertSerializeDeserialize(1.23f, float.class);
//        assertSerializeDeserialize(1.2213, double.class);
//        assertSerializeDeserialize('s', char.class);
//    }
//
//    @Test
//    public void testArray() {
//
//        int[] intArray = {1, 2, 3};
//        assertArrayEquals(intArray, polyGson.fromJson(polyGson.toJson(intArray), int[].class));
//
//        char[] charArray = {'1', '2', '3'};
//        assertArrayEquals(charArray, polyGson.fromJson(polyGson.toJson(charArray), char[].class));
//
//        SampleObject[] objArray = {new SampleObject("1", "2"), new SampleObject("3","4")};
//        assertArrayEquals(objArray, polyGson.fromJson(polyGson.toJson(objArray), SampleObject[].class));
//    }
//
//    @Test
//    public void testCollection() {
//        assertSerializeDeserialize(Lists.newArrayList(1, 2), List.class);
//        assertSerializeDeserialize(Lists.newArrayList("a", "b"), List.class);
//        assertSerializeDeserialize(Lists.newArrayList(new SampleObject("1", "2"), new SampleObject("3","4")), List.class);
//
//        assertSerializeDeserialize(Sets.newHashSet(1, 2), Set.class);
//        assertSerializeDeserialize(Sets.newHashSet("a", "b"), Set.class);
//        assertSerializeDeserialize(Sets.newHashSet(new SampleObject("1", "2"), new SampleObject("3","4")), Set.class);
//    }
//
//    @Test
//    public void testObject() {
//        assertSerializeDeserialize(new SampleObject("1", "2"), SampleObject.class);
//    }
//
//    @Test
//    public void testMap() {
//        assertSerializeDeserialize(Maps.newHashMap());
//
//    }
//
//    @Test
//    public void testObjects() {
//        assertSerializeDeserialize("String", String.class);
//
//    }
//
//    private void assertSerializeDeserialize(Object input) {
//        assertEquals(polyGson.fromJson(polyGson.toJson(input)), input);
//    }
//
//    private <T> void assertSerializeDeserialize(T input, Class<T> klass) {
//        assertEquals(polyGson.fromJson(polyGson.toJson(input), klass), input);
//    }
//
////    @Test
////    public void testLakshay() throws IOException {
////
////        String tweetString = new String(Files.readAllBytes(Paths.get("/Users/lakshay/testCurl/deserialize/resultprod0")));
////        SprMicroServiceSerializer serializer = new SprMicroServiceXStreamSerializer();
////
////        long before = System.currentTimeMillis();
////        Response xstreamOp = (Response) serializer.unpackResult(tweetString, Response.class);
////        long after = System.currentTimeMillis();
////        printTimeTakenStats(before, after, "XSTREAM UNPACK");
////
////        //        serializer = new SprMicroServiceGsonSerializer();
////
////        before = System.currentTimeMillis();
////        //        String packOp = serializer.packResult(op, Response.class);
////        String packOp =
////                gson.toJson(xstreamOp);
////        after = System.currentTimeMillis();
////        printTimeTakenStats(before, after, "SIMPLE_GSON PACK");
////
////        before = System.currentTimeMillis();
////        //        op = (Response) serializer.unpackResult(packOp, Response.class);
////        Response gsonOp =
////                gson.fromJson(packOp, Response.class);
////        after = System.currentTimeMillis();
////        printTimeTakenStats(before, after, "SIMPLE_GSON UNPACK");
////
////        before = System.currentTimeMillis();
////        //        String packOp = serializer.packResult(op, Response.class);
////        packOp =
////                polyGson.toJson(xstreamOp);
////        after = System.currentTimeMillis();
////        printTimeTakenStats(before, after, "POLY_GSON PACK");
////
////        before = System.currentTimeMillis();
////        //        op = (Response) serializer.unpackResult(packOp, Response.class);
////        gsonOp =
////                polyGson.fromJson(packOp, Response.class);
////        after = System.currentTimeMillis();
////        printTimeTakenStats(before, after, "POLY_GSON UNPACK");
////
////    }
////
////    @Test
////    public void testResponse() throws IOException {
////        String responseString = new String(Files.readAllBytes(Paths.get("/Users/lakshay/testCurl/deserialize/responseqa4")));
////        SprMicroServiceSerializer serializer = new SprMicroServiceXStreamSerializer();
////
////        long before = System.currentTimeMillis();
////        SprMicroServiceResponse xstreamResponse = serializer.unpackResponse(responseString, new SprMicroServiceRequest());
////        long after = System.currentTimeMillis();
////        printTimeTakenStats(before, after, "XSTREAM UNPACK RESPONSE");
////
////        before = System.currentTimeMillis();
////        Response xstreamResult = (Response) serializer.unpackResult(xstreamResponse.getResult(), Response.class);
////        after = System.currentTimeMillis();
////        printTimeTakenStats(before, after, "XSTREAM UNPACK RESULT");
////
////        serializer = new SprMicroServiceGsonSerializer();
////
////        before = System.currentTimeMillis();
////        String gsonResultString = serializer.packResult(xstreamResult, Response.class);
////        after = System.currentTimeMillis();
////        printTimeTakenStats(before, after, "SIMPLE_GSON PACK RESULT");
////
////        before = System.currentTimeMillis();
////        String gsonResponseString = serializer.packResponse(xstreamResponse.result(gsonResultString));
////        after = System.currentTimeMillis();
////        printTimeTakenStats(before, after, "SIMPLE_GSON PACK RESPONSE");
////
////        before = System.currentTimeMillis();
////        SprMicroServiceResponse gsonResponse = serializer.unpackResponse(gsonResponseString, new SprMicroServiceRequest());
////        after = System.currentTimeMillis();
////        printTimeTakenStats(before, after, "SIMPLE_GSON UNPACK RESPONSE");
////
////        before = System.currentTimeMillis();
////        Response gsonResult = (Response) serializer.unpackResult(gsonResponse.getResult(), Response.class);
////        after = System.currentTimeMillis();
////        printTimeTakenStats(before, after, "SIMPLE_GSON UNPACK RESULT");
////
////        before = System.currentTimeMillis();
////        String polyGsonResultString = polyGson.toJson(gsonResult);
////        after = System.currentTimeMillis();
////        printTimeTakenStats(before, after, "SIMPLE_GSON PACK RESULT");
////
////        before = System.currentTimeMillis();
////        String polyGsonResponseString = polyGson.toJson(xstreamResponse.result(polyGsonResultString));
////        after = System.currentTimeMillis();
////        printTimeTakenStats(before, after, "SIMPLE_GSON PACK RESPONSE");
////
////        before = System.currentTimeMillis();
////        SprMicroServiceResponse polyGsonResponse = polyGson.fromJson(polyGsonResponseString, SprMicroServiceResponse.class);
////        after = System.currentTimeMillis();
////        printTimeTakenStats(before, after, "SIMPLE_GSON UNPACK RESPONSE");
////
////        before = System.currentTimeMillis();
////        Response polyGsonResult = (Response) polyGson.fromJson(polyGsonResponse.getResult());
////        after = System.currentTimeMillis();
////        printTimeTakenStats(before, after, "SIMPLE_GSON UNPACK RESULT");
////    }
////
////    private void printTimeTakenStats(long before, long after, String message) {
////        System.out.println(message);
////        System.out.println("Time taken: millis: " + (after - before));
////        System.out.println("Time taken: minutes: " + TimeUnit.MILLISECONDS.toMinutes(after - before));
////    }
//
//    private class SampleObject {
//        String var1;
//        String var2;
//
//        public SampleObject(String var1, String var2) {
//            this.var1 = var1;
//            this.var2 = var2;
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (obj instanceof SampleObject) {
//                SampleObject sampleObject = (SampleObject) obj;
//                return StringUtils.equals(var1, sampleObject.var1) && StringUtils.equals(var2, sampleObject.var2);
//            }
//            return false;
//        }
//    }
//}
