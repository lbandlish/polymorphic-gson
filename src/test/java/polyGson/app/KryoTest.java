package polyGson.app;

/**
 * @author lakshay
 * @since 20/10/21
 */

import com.esotericsoftware.kryo.Kryo;
        import com.esotericsoftware.kryo.io.Input;
        import com.esotericsoftware.kryo.io.Output;
        import java.io.*;
import java.util.HashMap;

import static com.google.common.base.Charsets.UTF_16;

public class KryoTest {
    static public void main (String[] args) throws Exception {
//        Kryo kryo = new Kryo();
//        kryo.register(SomeClass.class);

//        SomeClass object = new SomeClass();
//        object.value = "Hello Kryo!";
//        object.value2 = "noo!";

//        Output output = new Output(new FileOutputStream("file.bin"));
//        kryo.writeObject(output, object);
//        output.close();

//        Input input = new Input(new FileInputStream("file.bin"));
//        SomeClass object2 = kryo.readObject(input, SomeClass.class);
//        System.out.println(object2.value);
//        input.close();


//        Machine machine = new Computer("v3");

        Person person = new Person(new Computer("v3"));
        person.map = new HashMap<>();
        person.map.put("key1", "val1");

        byte[] str = KryoMarshaller.toJson(person);
        String strin = new String(str, UTF_16);
//        String s = new String(str, UTF_16);
//        Person obj = KryoMarshaller.fromInputStream(new ByteArrayInputStream(str), Person.class);
        Person obj = (Person) KryoMarshaller.fromJson(strin.getBytes(UTF_16), Person.class);

//        String str = KryoMarshaller.toJson(object);
//        SomeClass obj = KryoMarshaller.fromJson(str, SomeClass.class);
        System.out.println(new String(str));
    }

    static public class SomeClass {
        String value;
        String value2;
        Machine machine;
    }

    public static class Person {
        private final Machine machine;

        Person (Machine machine) {
            this.machine = machine;
        }

        public HashMap<String, String> map;
    }

    public static class Machine {
        String version;

        Machine(String version) {
            this.version = version;
        }
    }

    public static class Computer extends Machine {
        Computer(String version) {
            super(version);
        }
        String type = "TypeComputer";
    }
}
