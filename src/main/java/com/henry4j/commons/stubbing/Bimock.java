package com.henry4j.commons.stubbing;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.experimental.Accessors;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.henry4j.commons.base.PojoMapper;

public class Bimock {
    private static final String LINE_BREAK = System.getProperty("line.separator");
    private final PojoMapper pojoMapper;

    // Bimock.BimockModule is required to be auto-wired to PojoMapper's constructor.
    public Bimock(PojoMapper pojoMapper) {
        this.pojoMapper = pojoMapper;
    }

    public <T> T of(T object, Mode mode, final File resource) {
        if (Mode.Record == mode && resource.exists()) {
            resource.delete();
        }
        val recordDown = new Answer<Object>() {
            public Object answer(InvocationOnMock iom) throws Throwable {
                Object success = null;
                Throwable failure = null;
                try {
                    return (success = iom.callRealMethod());
                } catch (Throwable t) {
                    throw (failure = t);
                } finally {
                    if (Modifier.isPublic(iom.getMethod().getModifiers())) {
                        Files.append(toJson(iom.getMethod(), success, failure) + LINE_BREAK, resource, Charsets.UTF_8);
                    }
                }
            }
        };
        val throwUp = new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                throw new RuntimeException("UNCHECKED: this bug should go unhandled, as there are unexpected invocation(s).");
            }
        };
        @SuppressWarnings("unchecked")
        T mock = mock((Class<T>)object.getClass(), withSettings()
                .spiedInstance(Mode.Record == mode ? object : null)
                .defaultAnswer(Mode.Record == mode ? recordDown : throwUp));
        return Mode.Replay == mode ? doStub(mock, resource) : mock;
    }

    @SneakyThrows({ IOException.class })
    private <T> T doStub(T mock, File resource) {
        val invocationsByHashCode = new LinkedHashMap<Integer, Queue<Invocation>>();
        for (val json : Files.readLines(resource, Charsets.UTF_8)) {
            val i = pojoMapper.fromJson(json, Invocation.class);
            int c = i.hashCode();
            if (!invocationsByHashCode.containsKey(c)) {
                invocationsByHashCode.put(c, new LinkedList<Invocation>());
            }
            invocationsByHashCode.get(c).offer(i);
        }
        for (val invocations : invocationsByHashCode.values()) {
            Stubber s = null;
            for (val i: invocations) {
                if (null != i.failure()) {
                    s = null == s ? doThrow(i.failure()) : s.doThrow(i.failure());
                } else if (Void.TYPE.equals(i.method().getReturnType())) {
                    s = null == s ? doNothing() : s.doNothing();
                } else {
                    s = null == s ? doReturn(i.success()) : s.doReturn(i.success());
                }
            }
            s = s.doThrow(new RuntimeException("UNCHECKED: this bug should go unhandled, as there are unexpected invocation(s)."));
            doStub(invocations.peek().method(), s.when(mock));
        }
        return mock;
    }

    @SneakyThrows({ IllegalAccessException.class, InvocationTargetException.class, NoSuchMethodException.class })
    private static void doStub(Method m, Object o) {
        m = m.getDeclaringClass().getMethod(m.getName(), m.getParameterTypes()); // to be compatible across JVM instances.
        Class<?>[] argTypes = m.getParameterTypes();
        Object[] args = new Object[argTypes.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = any(argTypes[i]);
        }
        m.invoke(o, args);
    }

    private String toJson(Method method, Object success, Throwable failure) {
        return pojoMapper.toJson(Invocation.of(method, success, failure));
    }

    public static enum Mode {
        Record, Replay
    }

    @Data
    @Accessors(fluent = true)
    @NoArgsConstructor @AllArgsConstructor(staticName = "of")
    public static class Invocation {
        private Method method;
        private Object success;
        private Throwable failure;

        @Override
        public int hashCode() {
            return method.hashCode() ^ Arrays.hashCode(method.getParameterTypes());
        }
    }

    public static class BimockModule extends SimpleModule {
        private static final long serialVersionUID = -2479398644334238459L;

        // NOTE: http://wiki.fasterxml.com/JacksonMixInAnnotations
        @SneakyThrows({ ClassNotFoundException.class })
        public BimockModule() {
            super("Bimock.MixIns", new Version(1, 0, 0, "", "", ""));
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
}
