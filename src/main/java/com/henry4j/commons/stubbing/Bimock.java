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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.experimental.Accessors;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

import pp.commons.test.base.PojoMapper;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/*
 * Bimock introduction --- excerpted from http://w.amazon.com?-/bimock
 * How about automating stubbing arbitrary calls on public methods?
 * Let's use a bimock (bidirectional mock) which has a factory method `of` 
 *   that takes a real object, a mode of record, or replay, and a resource file.
 * When in Record mode, it records method invocations with return values or exceptions
 *   into the resource file in the JSON format.
 * When in Replay mode, it sets up method invocations and answers out of the resource file
 *   when it starts up, and replays answers of returns or throws.
 *   also, it throws up a runtime exception to indicate a potential bug, 
 *   as soon as unexpected, or additional method invocations happen on the bimock.
 */
public class Bimock {
    private final PojoMapper pojoMapper;

    // Bimock.BimockModule is required to be auto-wired to PojoMapper's constructor.
    public Bimock(PojoMapper pojoMapper) {
        this.pojoMapper = pojoMapper;
    }

    public <T> T of(T object, Mode mode, final File resource) {
        if (Mode.Record == mode && resource.exists()) {
            if (!resource.delete()) {
                throw new IllegalStateException("UNCHECKED: this bug should go unhandled.");
            }
        }
        val depth = new int[1];
        val recordDown = new Answer<Object>() {
            public Object answer(InvocationOnMock iom) throws Throwable {
                depth[0]++;
                Object success = null;
                Throwable failure = null;
                try {
                    return (success = iom.callRealMethod());
                } catch (Throwable t) {
                    throw (failure = t);
                } finally {
                    if (0 == --depth[0]) { // only records out-most invocation.
                        Files.append(toJson(iom.getMethod(), success, failure) + "\n", resource, Charsets.UTF_8);
                    }
                }
            }
        };
        val throwUp = new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                throw new IllegalStateException("UNCHECKED: this bug should go unhandled, as there are unexpected invocation(s).");
            }
        };
        @SuppressWarnings("unchecked")
        val clazz = (Class<T>)object.getClass();
        T mock = mock(clazz, withSettings()
                .spiedInstance(Mode.Record == mode ? object : null)
                .defaultAnswer(Mode.Record == mode ? recordDown : throwUp));
        return Mode.Replay == mode ? doStub(mock, resource) : mock;
    }

    @SneakyThrows({ IOException.class })
    private <T> T doStub(T mock, File resource) {
        val invocationsByMethodSignature = new LinkedHashMap<Integer, Queue<Invocation>>();
        for (val json : Files.readLines(resource, Charsets.UTF_8)) {
            Invocation i = pojoMapper.fromJson(json, Invocation.class);
            int c = methodSignatureCode(i);
            if (!invocationsByMethodSignature.containsKey(c)) {
                invocationsByMethodSignature.put(c, new LinkedList<Invocation>());
            }
            invocationsByMethodSignature.get(c).add(i);
        }
        for (val invocations : invocationsByMethodSignature.values()) {
            Stubber s = null;
            for (val i: invocations) {
                if (null != i.failure()) {
                    s = (null == s ? doThrow(i.failure()) : s.doThrow(i.failure()));
                } else if (Void.TYPE.equals(i.method().getReturnType())) {
                    s = (null == s ? doNothing() : s.doNothing());
                } else {
                    s = (null == s ? doReturn(i.success()) : s.doReturn(i.success()));
                }
            }
            s = s.doThrow(new IllegalStateException("UNCHECKED: this bug should go unhandled, as there are unexpected invocation(s)."));
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

    private static int methodSignatureCode(Invocation i) {
        // method.hashCode() hashes the declaring class' fully qualified name and the method name.
        return i.method.hashCode() ^ Arrays.hashCode(i.method.getParameterTypes());
    }

    private String toJson(Method method, Object success, Throwable failure) {
        return pojoMapper.toJson(Invocation.of(method, success, failure));
    }

    public static enum Mode {
        Record, Replay
    }

    @Getter @Accessors(fluent = true)
    @NoArgsConstructor @AllArgsConstructor(staticName = "of")
    public static class Invocation {
        private Method method;
        private Object success;
        private Throwable failure;
    }
}
