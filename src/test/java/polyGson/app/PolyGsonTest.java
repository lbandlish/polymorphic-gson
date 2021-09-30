package polyGson.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import org.junit.Before;
import org.junit.Test;
import polyGson.PolyGson;
import polyGson.PolyGsonBuilder;

import javax.crypto.Mac;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PolyGsonTest {

    PolyGson polyGson;
    Gson gson;

    @Before
    public void setup() throws IOException {
        gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .setPrettyPrinting()
                .create();

        polyGson = new PolyGsonBuilder()
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
    public void testToJson() throws IOException {
        TargetInterface target = new TargetInterfaceImpl("heya");
        Object origTarget = polyGson.fromJson(polyGson.toJson(target));

        Person person = polyGson.fromJson(polyGson.toJson(new Person()), Person.class);
    }

    @Test
    public void testShit() {
        TestClass obj = new TestClass();
        obj.getClass().getFields();
    }

    class Machine {
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

    class Person {
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

}
