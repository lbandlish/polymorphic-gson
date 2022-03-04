package polyGson.app;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.Kryo.DefaultInstantiatorStrategy;
import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import de.javakaffee.kryoserializers.FieldAnnotationAwareSerializer.Factory;
import de.javakaffee.kryoserializers.GregorianCalendarSerializer;
import de.javakaffee.kryoserializers.JdkProxySerializer;
import de.javakaffee.kryoserializers.SynchronizedCollectionsSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableListSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableMapSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableMultimapSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableSetSerializer;
import de.javakaffee.kryoserializers.guava.ReverseListSerializer;
import de.javakaffee.kryoserializers.guava.UnmodifiableNavigableSetSerializer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.util.Arrays;
import java.util.GregorianCalendar;
//import org.joda.time.DateTime;
//import org.joda.time.LocalDate;
//import org.joda.time.LocalDateTime;
import org.objenesis.strategy.StdInstantiatorStrategy;

public class KryoMarshaller<T> {

    private static final byte[] EMPTY = new byte[0];

    public static final String NAME = "KRYO";

    private static final ThreadLocal<Kryo> KRYO = ThreadLocal.withInitial(KryoMarshaller::createKryo);

    private final Class<T> type;

    public KryoMarshaller(Class<T> type) {
        this.type = type;
    }

    public InputStream stream(T value) {
        if (value == null) {
            return new ByteArrayInputStream(EMPTY);
        }
        byte[] bytes = toByteArray(value);
        return new ByteArrayInputStream(bytes);
    }

    public T parse(InputStream stream) {
        if (stream == null) {
            return null;
        }
        return fromInputStream(stream, type);
    }

    public static <T> Object fromJson(byte[] value, Class<T> klass) {
//        return fromInputStream(new ByteArrayInputStream(value), klass);
        return deserialize(new ByteArrayInputStream(value));
    }

    public static <T> byte[] toJson(T value) {
//        return new String(toByteArray(value));
        return toByteArrayClassName(value);
    }

    public static <T> byte[] toByteArrayClassName(T value) {
        try (Output output = new Output(4096, -1)) {
            KRYO.get().writeClassAndObject(output, value);
            output.flush();
            return output.toBytes();
        }
    }


    public static <T> T fromInputStream(InputStream stream, Class<T> clz) {
        return fromInputStream(KRYO.get(), stream, clz);
    }

    public static <T> T fromInputStream(Kryo kryo, InputStream stream, Class<T> clz) {
        try (Input input = new Input(stream)) {
            return kryo.readObjectOrNull(input, clz);
        }
    }

    public static Object deserialize(InputStream stream) {
        try (Input input = new Input(stream)) {
            return KRYO.get().readClassAndObject(input);
        }
    }

    public static <T> byte[] toByteArray(T value) {
        return toByteArray(value, KRYO.get());
    }

    public static <T> byte[] toByteArray(T value, Kryo kryo) {
        try (Output output = new Output(4096, -1)) {
            kryo.writeObject(output, value);
            output.flush();
            return output.toBytes();
        }
    }

    public static Kryo createKryo() {
        Kryo kryo = new Kryo();
        kryo.setReferences(true);
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));

        kryo.setDefaultSerializer(new Factory(Arrays.asList(java.beans.Transient.class),
                true));

        kryo.register(GregorianCalendar.class, new GregorianCalendarSerializer());
        kryo.register(InvocationHandler.class, new JdkProxySerializer());

        kryo.register(Arrays.asList().getClass(), new ArraysAsListSerializer());
        UnmodifiableCollectionsSerializer.registerSerializers(kryo);
        SynchronizedCollectionsSerializer.registerSerializers(kryo);

//        // joda DateTime, LocalDate, LocalDateTime and LocalTime
//        kryo.register(DateTime.class, new JodaDateTimeSerializer());
//        kryo.register(LocalDate.class, new JodaLocalDateSerializer());
//        kryo.register(LocalDateTime.class, new JodaLocalDateTimeSerializer());
//        kryo.register(LocalDateTime.class, new JodaLocalTimeSerializer());

        // guava ImmutableList, ImmutableSet, ImmutableMap, ImmutableMultimap, ImmutableTable, ReverseList, UnmodifiableNavigableSet
        ImmutableListSerializer.registerSerializers(kryo);
        ImmutableSetSerializer.registerSerializers(kryo);
        ImmutableMapSerializer.registerSerializers(kryo);
        ImmutableMultimapSerializer.registerSerializers(kryo);
        ReverseListSerializer.registerSerializers(kryo);
        UnmodifiableNavigableSetSerializer.registerSerializers(kryo);
        return kryo;
    }
}
