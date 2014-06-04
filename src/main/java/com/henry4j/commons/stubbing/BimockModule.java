package com.henry4j.commons.stubbing;

import static com.google.common.io.BaseEncoding.base64;
import static com.google.common.io.ByteStreams.toByteArray;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.SequenceInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import lombok.SneakyThrows;
import lombok.val;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class BimockModule extends SimpleModule {
    private static final long serialVersionUID = -2479398644334238459L;

    // [Jackson Mix-in Annotations](http://github.com/FasterXML/jackson-docs/wiki/JacksonMixInAnnotations)
    public BimockModule() {
        super("BimockModule", new Version(1, 0, 0, "", "", ""));
        setMixInAnnotation(Method.class, MethodMixIn.class);
        setMixInAnnotation(Throwable.class, ThrowableMixIn.class);

        addSerializer(ByteBuffer.class, new BBSerializer());
        addDeserializer(ByteBuffer.class, new BBDeserializer());
        addSerializer(InputStream.class, new IsSerializer());
        addDeserializer(InputStream.class, new IsDeserializer());
    }

    static class BBDeserializer extends JsonDeserializer<ByteBuffer> {
        private static final Constructor<?> HBB_CTOR = heapByteBufferCtor();

        @Override
        @SneakyThrows({ IllegalAccessException.class, InstantiationException.class, InvocationTargetException.class })
        public ByteBuffer deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            String clazz = null;
            byte[] bytes = null;
            int mark = 0, position = 0, limit = 0, capacity = 0, offset = 0;
            while (JsonToken.END_OBJECT != jp.nextToken()) {
                val fieldName = jp.getCurrentName();
                jp.nextToken();
                switch (fieldName) {
                case "@class": clazz = jp.getText(); break;
                case "base64": bytes = base64().decode(jp.getText()); break;
                case "mark": mark = jp.getIntValue(); break;
                case "position": position = jp.getIntValue(); break;
                case "limit": limit = jp.getIntValue(); break;
                case "capacity": capacity = jp.getIntValue(); break;
                case "offset": offset = jp.getIntValue(); break;
                }
            }
            assert null != clazz;
            return (ByteBuffer)HBB_CTOR.newInstance(bytes, mark, position, limit, capacity, offset);
        }

        @SneakyThrows({ ClassNotFoundException.class, NoSuchMethodException.class, SecurityException.class })
        private static Constructor<?> heapByteBufferCtor() {
            val clazz = Class.forName("java.nio.HeapByteBuffer");
            val ctor = clazz.getDeclaredConstructor(byte[].class, int.class, int.class, int.class, int.class, int.class);
            ctor.setAccessible(true);
            return ctor;
        }
    }

    static class BBSerializer extends JsonSerializer<ByteBuffer> {
        private static final Field BB_MARK_FIELD = bb_mark_field();

        @Override
        @SneakyThrows({ IllegalAccessException.class })
        public void serialize(ByteBuffer bb, JsonGenerator jgen, SerializerProvider arg2) throws IOException,
                JsonProcessingException {
            jgen.writeStartObject();
            jgen.writeStringField("@class", bb.getClass().getName());
            jgen.writeStringField("base64", base64().encode(bb.array()));
            jgen.writeNumberField("mark", BB_MARK_FIELD.getInt(bb));
            jgen.writeNumberField("position", bb.position());
            jgen.writeNumberField("limit", bb.limit());
            jgen.writeNumberField("capacity", bb.capacity());
            jgen.writeNumberField("offset", bb.arrayOffset());
            jgen.writeEndObject();
        }

        @SneakyThrows
        private static Field bb_mark_field() {
            val mark = Buffer.class.getDeclaredField("mark");
            mark.setAccessible(true);
            return mark;
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    static abstract class ThrowableMixIn {
    }

    @JsonIgnoreProperties({ "accessible", "annotations", "bridge", "clazz", "declaredAnnotations", "genericExceptionTypes", "genericParameterTypes", "genericReturnType", "override", "parameterAnnotations", "root", "slot", "synthetic", "typeParameters", "varArgs" })
    static abstract class MethodMixIn {
        @JsonCreator MethodMixIn(
                @JsonProperty("declaringClass") Class<?> declaringClass,
                @JsonProperty("name") String name,
                @JsonProperty("parameterTypes") Class<?>[] parameterTypes,
                @JsonProperty("returnType") Class<?> returnType,
                @JsonProperty("checkedExceptions") Class<?>[] checkedExceptions,
                @JsonProperty("modifiers") int modifiers,
                @JsonProperty("slot") int slot,
                @JsonProperty("signature") String signature,
                @JsonProperty("annotations") byte[] annotations,
                @JsonProperty("parameterAnnotations") byte[] parameterAnnotations,
                @JsonProperty("annotationDefault") byte[] annotationDefault) {
         }
    }

    static class IsDeserializer extends JsonDeserializer<InputStream> {
        @Override
        public InputStream deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            String clazz = null;
            byte[] bytes = null;
            while (JsonToken.END_OBJECT != jp.nextToken()) {
                val fieldName = jp.getCurrentName();
                jp.nextToken();
                switch (fieldName) {
                case "@class": clazz = jp.getText(); break;
                case "base64": bytes = base64().decode(jp.getText()); break;
                }
            }
            assert null != clazz;
            return new ByteArrayInputStream(bytes);
        }
    }

    static class IsSerializer extends JsonSerializer<InputStream> {
        private static final Field FIS_IN_FIELD = fisInField();
        private static final Field SIS_IN_FIELD = sisInField();
        private static final Field OIS_IN_FIELD = oisInField();
        private static final Constructor<?> BDIS_CTOR = bdisCtor();

        @Override
        public void serialize(InputStream is, JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonProcessingException {
            byte[] bytes = null;
            if (!is.markSupported() && FilterInputStream.class.isAssignableFrom(is.getClass())) {
                set(FIS_IN_FIELD, is, new BufferedInputStream(get(FIS_IN_FIELD, is)));
            }
            if (is.markSupported()) {
                is.mark(Integer.MAX_VALUE);
                bytes = toByteArray(is);
                is.reset();
            } else if (SequenceInputStream.class.isAssignableFrom(is.getClass())) {
                set(SIS_IN_FIELD, is, new ByteArrayInputStream(bytes = toByteArray(is)));
            } else if (ObjectInputStream.class.isAssignableFrom(is.getClass())) {
                set(OIS_IN_FIELD, is, bdis(is, bytes = toByteArray(is)));
            } else {
                bytes = toByteArray(is);
            }
            jgen.writeStartObject();
            jgen.writeStringField("@class", is.getClass().getName());
            jgen.writeStringField("base64", base64().encode(bytes));
            jgen.writeEndObject();
        }

        @SneakyThrows
        private static Field fisInField() {
            val in = FilterInputStream.class.getDeclaredField("in");
            in.setAccessible(true);
            return in;
        }

        @SneakyThrows
        private static Field sisInField() {
            val in = SequenceInputStream.class.getDeclaredField("in");
            in.setAccessible(true);
            return in;
        }

        @SneakyThrows
        private static Field oisInField() {
            val in = ObjectInputStream.class.getDeclaredField("bin");
            in.setAccessible(true);
            return in;
        }

        @SneakyThrows({ ClassNotFoundException.class, NoSuchMethodException.class, SecurityException.class })
        private static Constructor<?> bdisCtor() {
            val clazz = Class.forName("java.io.ObjectInputStream$BlockDataInputStream");
            val ctor = clazz.getDeclaredConstructor(ObjectInputStream.class, InputStream.class);
            ctor.setAccessible(true);
            return ctor;
        }

        @SneakyThrows({ InstantiationException.class, IllegalAccessException.class, IllegalArgumentException.class, InvocationTargetException.class })
        private static InputStream bdis(InputStream outer, byte[] bytes) {
            return (InputStream)BDIS_CTOR.newInstance(outer, new ByteArrayInputStream(bytes));
        }

        @SneakyThrows({ IllegalAccessException.class })
        private static void set(Field f, InputStream outer, InputStream inner) {
            f.set(outer, inner);
        }

        @SneakyThrows({ IllegalAccessException.class })
        private static InputStream get(Field f, InputStream outer) {
            return (InputStream)f.get(outer);
        }
    }
}
