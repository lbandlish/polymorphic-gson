package polyGson.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.xstream.XStream;
import org.junit.Before;
import org.junit.Test;
import polyGson.PolyGson;
import polyGson.PolyGsonBuilder;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class PolyGsonTest {

    XStream xstream = new XStream();

    PolyGson polyGson;
    Gson gson;

    @Before
    public void setup() throws IOException {
        gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .setPrettyPrinting()
                .create();

        polyGson = new PolyGsonBuilder()
//                .enableComplexMapKeySerialization()
                .setPrettyPrinting()
                .create();

    }

    @Test
    public void testPolyGson2() throws IOException, ClassNotFoundException {

        Map<String, Integer> map = new HashMap<>();
        System.out.println(map.getClass());
        TypeToken<Map<String, Integer>> typeToken = new TypeToken<Map<String, Integer>>(){};

        Person person = new Person();
        System.out.println(gson.toJson(person));
        System.out.println("yo");
        System.out.println(polyGson.toJson(person));

        Person personGson = gson.fromJson(gson.toJson(person), Person.class);
        System.out.println("yow");
        Person personPolyGson = (Person) polyGson.fromJson(polyGson.toJson(person));

//        System.out.println(Class.forName(int.class.getName()).getName());
//        int x = 91;
//        System.out.println(gson.toJson(x));
//        System.out.println(polyGson.toJson(x));
//
////        System.out.println(gson.toJson(gson.fromJson(gson.toJson(x))));
//        System.out.println(gson.toJson((Object)polyGson.fromJson(polyGson.toJson(x))));
////        System.out.println(polyGson.toJson(x));

    }

    @Test
    public void testPolyJson() throws IOException {
        Person person = new Person(new Computer());
        person.dontSerialize = "dontSerializeText";
        String person_string = gson.toJson(person);
        System.out.println("GSON SERIALIZE");
        System.out.println(person_string);

        String poly_person_string = polyGson.toJson(person);
        System.out.println("POLYGSON SERIALIZE");
        System.out.println(poly_person_string);

        Person person_ret = gson.fromJson(person_string, person.getClass());
        person_ret.print();

//		Person person = new Person(new Computer());
//		String person_string = polyGson.toJson(person);
//		System.out.println(person_string);
//		Person person_ret = polyGson.fromJson(person_string);
//		person_ret.print();


        Set<String> set = new HashSet<String>();
        set.add("hi");
        String set_json = polyGson.toJson(set);
        System.out.println(set_json);
        Set<String> set_ret = (Set<String>) polyGson.fromJson(set_json);
        System.out.println(set_ret);


//		String[] string = new String[1];
//		string[0] = "hi";
//		String string_json = polyGson.toJson(string);
//		System.out.println(string_json);
//		String[] x = polyGson.fromJson(string_json);
//		System.out.println(x[0]);


        Map<Integer, String> mp = new HashMap<Integer, String>();
        mp.put(1, "hwlp");
        mp.put(2, "ap");
        String x = polyGson.toJson(mp);
        System.out.println(x);
        Map<Integer, String> mp_ret = (Map<Integer, String>) polyGson.fromJson(x);
        for (Map.Entry<Integer, String> entry : mp_ret.entrySet()) {
            System.out.println(entry.getKey()+1);
        }
    }

    @Test
    public void testEnum() {

        String json = polyGson.toJson(SampleEnum.ENUM1);
        SampleEnum sampleEnum = polyGson.fromJson(json, SampleEnum.class);
        System.out.println(json);

        json = polyGson.toJson(SampleEnum.ENUM2);
        sampleEnum = polyGson.fromJson(json, SampleEnum.class);
        System.out.println(json);

        SampleSubObject subObject = new SampleSubObject("var1", "var2", "var3");
        json = polyGson.toJson(subObject);
        subObject = polyGson.fromJson(json, SampleSubObject.class);
        System.out.println(json);

        subObject.sampleEnum = SampleEnum.ENUM2;
        json = polyGson.toJson(subObject);
        subObject = polyGson.fromJson(json, SampleSubObject.class);
        System.out.println(json);
    }

    @Test
    public void testSelfReferencingObj() {
        SelfReferencing obj = new SelfReferencing();
        obj.id = "topLevel";
        obj.selfReferencer = obj;

        String json = polyGson.toJson(obj);
        System.out.println(json);
//        SelfReferencing obj = new SelfReferencing();
//        SelfReferencer referencer = new SelfReferencer();
//        referencer.innerId = "InnerIDValue";
//        obj.id = "outerIDValue";
//        referencer.reference = obj;
//        obj.selfReferencer = referencer;
//
//        String xml = xstream.toXML(obj);
//        System.out.println(xml);
    }

    @Test
    public void testStatic() {
        SampleObject obj = new SampleObject("1", "2", "3");
        String json = polyGson.toJson(obj);
        System.out.println(json);

        SampleObject polyObj = polyGson.fromJson(json, SampleObject.class);
        System.out.println(gson.toJson(polyObj));
    }

    @Test
    public void testToJson() throws IOException {
        TargetInterface target = new TargetInterfaceImpl("heya");
        Object origTarget = polyGson.fromJson(polyGson.toJson(target));

        Person person = polyGson.fromJson(polyGson.toJson(new Person()), Person.class);
    }

    @Test
    public void testShit() {
        TestClass obj = new TestClass();
        obj.getClass().getFields();

        SampleSubObject sampleSubObject = new SampleSubObject("supvar1", "supvar2", "supvar3");
        sampleSubObject.var1 = "subvar1";
        sampleSubObject.var2 = "subvar2";


        String xml = xstream.toXML(sampleSubObject);
        System.out.println("XML:");
        System.out.println(xml);
        SampleSubObject subObject = (SampleSubObject) xstream.fromXML(xml);
        String json = polyGson.toJson(sampleSubObject);
        System.out.println("POLYGSON:");
        System.out.println(json);
        SampleSubObject subObjectPoly = polyGson.fromJson(json, SampleSubObject.class);
    }

    @Test
    public void testComplexKey() {
        ComplexMap complexMap = new ComplexMap();
        complexMap.mapInternal = new HashMap<>();
        complexMap.mapInternal.put(new Machine("customMachine"), new Person());

        String gsonDeserialize = gson.toJson(complexMap);
        String polyGsonDeserialize = polyGson.toJson(complexMap);

        ComplexMap gsonMap = gson.fromJson(gsonDeserialize, ComplexMap.class);
        ComplexMap polyGsonMap = polyGson.fromJson(polyGsonDeserialize, ComplexMap.class);
    }

    static class Machine {
        public String type;

        public Machine() {
            type = "machine type";
        }

        public Machine(String x) {
            type = x;
        }

        public void print() {
            System.out.println(type);
        }
    }

    class Computer extends Machine {
        public String processor;
        public void print() {
            System.out.println(processor);
        }

        public Computer() {
            processor = "i3";
        }

        public Computer(String x) {
            processor = x;
        }
    }

    static class Person {
        @SerializedName("serializedNameMachine")
        Machine machine;

        int peopleCount = 5;

        transient String dontSerialize;

        public Person(){
            machine = new Machine();
        }

        public Person(Machine m){
            machine = m;
        }

        public void print() {
            machine.print();
        }
    }

    class TestClass extends TestClassSuper {
        public String publicString;
        private String privateString;
    }

    private class TestClassSuper {
        public String publicStringSuper;
        private String privateStringSuper;
    }

    class ComplexMap {
        public Map<Machine, Person> mapInternal;
    }


    private static class SampleObject {
        private String var1;
        public String var2;
        public String var3;

        public static String GAME = "GAMEVAL";

        int[] arr;

        public SampleObject(String var1, String var2, String var3) {
            this.var1 = var1;
            this.var2 = var2;
            this.var3 = var3;
        }


//        @Override
//        public boolean equals(Object obj) {
//            if (obj instanceof SampleObject) {
//                SampleObject sampleObject = (SampleObject) obj;
//                return StringUtils.equals(var1, sampleObject.var1) && StringUtils.equals(var2, sampleObject.var2);
//            }
//            return false;
//        }

    }


    private static class SampleSubObject extends SampleObject {

        String subField = "default";
        private String var1;
        public String var2;
        SampleEnum sampleEnum = SampleEnum.ENUM2;

        public SampleSubObject(String var1, String var2, String var3) {
            super(var1, var2, var3);
        }

//        @Override
//        public boolean equals(Object obj) {
//            if (obj instanceof SampleSubObject) {
//                return super.equals(obj) && StringUtils.equals(subField, ((SampleSubObject) obj).subField);
//            }
//            return false;
//        }
    }

    private enum SampleEnum {
        ENUM1("fieldEnum1"),
        ENUM2("fieldEnum2") {
            @Override
            public String toString() {
                return super.toString() + "inner";
            }
        };

        private final String field;

        private String getField() {
            return field;
        };

        SampleEnum(String field) {
            this.field = field;
        }
    }

    private class SelfReferencing {
        SelfReferencing selfReferencer;
        String id;
    }

    private class SelfReferencer {
        SelfReferencing reference;
        String innerId;
    }

}