package polyGson;

import java.lang.reflect.Type;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.TypeAdapterFactory;

public final class PolyGsonBuilder {

    GsonBuilder gsonBuilder;
    public PolyGsonBuilder() {
        gsonBuilder = new GsonBuilder();
    }

    public PolyGsonBuilder setVersion(double ignoreVersionsAfter) {
        gsonBuilder.setVersion(ignoreVersionsAfter);
        return this;
    }

    public PolyGsonBuilder excludeFieldsWithModifiers(int... modifiers) {
        gsonBuilder.excludeFieldsWithModifiers(modifiers);
        return this;
    }

    public PolyGsonBuilder generateNonExecutableJson() {
        gsonBuilder.generateNonExecutableJson();
        return this;
    }

    public PolyGsonBuilder excludeFieldsWithoutExposeAnnotation() {
        gsonBuilder.excludeFieldsWithoutExposeAnnotation();
        return this;
    }

    public PolyGsonBuilder serializeNulls() {
        gsonBuilder.serializeNulls();
        return this;
    }

    public PolyGsonBuilder enableComplexMapKeySerialization() {
        gsonBuilder.enableComplexMapKeySerialization();
        return this;
    }

    public PolyGsonBuilder disableInnerClassSerialization() {
        gsonBuilder.disableInnerClassSerialization();
        return this;
    }

    public PolyGsonBuilder setLongSerializationPolicy(LongSerializationPolicy serializationPolicy) {
        gsonBuilder.setLongSerializationPolicy(serializationPolicy);
        return this;
    }

    public PolyGsonBuilder setFieldNamingPolicy(FieldNamingPolicy namingConvention) {
        gsonBuilder.setFieldNamingPolicy(namingConvention);
        return this;
    }

    public PolyGsonBuilder setFieldNamingStrategy(FieldNamingStrategy fieldNamingStrategy) {
        gsonBuilder.setFieldNamingStrategy(fieldNamingStrategy);
        return this;
    }

    public PolyGsonBuilder setExclusionStrategies(ExclusionStrategy... strategies) {
        gsonBuilder.setExclusionStrategies(strategies);
        return this;
    }

    public PolyGsonBuilder addSerializationExclusionStrategy(ExclusionStrategy strategy) {
        gsonBuilder.addSerializationExclusionStrategy(strategy);
        return this;
    }

    public PolyGsonBuilder addDeserializationExclusionStrategy(ExclusionStrategy strategy) {
        gsonBuilder.addDeserializationExclusionStrategy(strategy);
        return this;
    }

    public PolyGsonBuilder setPrettyPrinting() {
        gsonBuilder.setPrettyPrinting();
        return this;
    }

    public PolyGsonBuilder setLenient() {
        gsonBuilder.setLenient();
        return this;
    }

    public PolyGsonBuilder disableHtmlEscaping() {
        gsonBuilder.disableHtmlEscaping();
        return this;
    }

    public PolyGsonBuilder setDateFormat(String pattern) {
        gsonBuilder.setDateFormat(pattern);
        return this;
    }
    public PolyGsonBuilder setDateFormat(int style) {
        gsonBuilder.setDateFormat(style);
        return this;
    }

    public PolyGsonBuilder setDateFormat(int dateStyle, int timeStyle) {
        gsonBuilder.setDateFormat(dateStyle, timeStyle);
        return this;
    }

    public PolyGsonBuilder registerTypeAdapter(Type type, Object typeAdapter) {
        gsonBuilder.registerTypeAdapter(type, typeAdapter);
        return this;
    }

    public PolyGsonBuilder registerTypeAdapterFactory(TypeAdapterFactory factory) {
        gsonBuilder.registerTypeAdapterFactory(factory);
        return this;
    }

    public PolyGsonBuilder registerTypeHierarchyAdapter(Class<?> baseType, Object typeAdapter) {
        gsonBuilder.registerTypeHierarchyAdapter(baseType, typeAdapter);
        return this;
    }

    public PolyGsonBuilder serializeSpecialFloatingPointValues() {
        gsonBuilder.serializeSpecialFloatingPointValues();
        return this;
    }

    public PolyGson create() {
        return new PolyGson(gsonBuilder.create());
    }

}
