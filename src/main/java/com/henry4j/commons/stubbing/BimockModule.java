package com.henry4j.commons.stubbing;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import lombok.SneakyThrows;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class BimockModule extends SimpleModule {
    private static final long serialVersionUID = -2479398644334238459L;

    // NOTE: http://wiki.fasterxml.com/JacksonMixInAnnotations
    @SneakyThrows({ ClassNotFoundException.class })
    public BimockModule() {
        super("BimockModule", new Version(1, 0, 0, "", "", ""));
        setMixInAnnotation(Method.class, MethodMixIn.class);
        setMixInAnnotation(Throwable.class, ThrowableMixIn.class);
        setMixInAnnotation(ByteBuffer.class, ByteBufferMixin.class);
        setMixInAnnotation(Class.forName("java.nio.HeapByteBuffer"), HeapByteBufferMixin.class);
    }

    @JsonIgnoreProperties({ "long", "double", "int", "float", "address", "hb", "isReadOnly", "bigEndian", "nativeByteOrder", "short", "char" })
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    static abstract class ByteBufferMixin {
        @JsonProperty("bytes")
        byte[] array() { return null; }
    }

    static abstract class HeapByteBufferMixin {
        @JsonCreator HeapByteBufferMixin(
                @JsonProperty("bytes") byte[] bytes,
                @JsonProperty("mark") int mark,
                @JsonProperty("position") int position,
                @JsonProperty("limit") int limit,
                @JsonProperty("capacity") int capacity,
                @JsonProperty("offset") int offset) {}
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    static abstract class ThrowableMixIn {
    }

    @JsonIgnoreProperties({ "override", "clazz", "annotations", "parameterAnnotations", "root", "synthetic", "typeParameters", "declaredAnnotations", "genericReturnType", "genericParameterTypes", "genericExceptionTypes", "bridge", "varArgs", "accessible" })
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
}
