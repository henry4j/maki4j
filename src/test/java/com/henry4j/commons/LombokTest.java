package com.henry4j.commons;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.Value;
import lombok.val;
import lombok.experimental.Accessors;
import lombok.experimental.Wither;
import lombok.extern.log4j.Log4j;

import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Maps;

public class LombokTest {
    public class Trie<V> {
        @Getter(AccessLevel.PROTECTED)
        private final Map<Character, Trie<V>> children = Maps.newHashMap();
        @Getter @Setter @Accessors(fluent = true)
        private V value;

        public Trie<V> map(CharSequence keys) { throw new UnsupportedOperationException("No implementation yet."); }
    }

    @Test
    public void testTrie() {
        val trie = new Trie<List<Integer>>();
        val list = trie.map("bananas").value(new ArrayList<Integer>()).value();
        list.add(10);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter // for all non-static fields
    public class ClassicEntry<K, V> {
        private K key;
        @Setter @NonNull // for null check in the setter; throws NPE.
        private V value;
        @Getter(AccessLevel.NONE) // not to generate a getter specified on the class.
        private int olv; // optimistic lock version
    }

	@Test(expected = NullPointerException.class)
    public void testClassicEntry() {
        val entry = new ClassicEntry<String, Integer>();
        entry.setValue(null); // will see an NPE by @NonNull.
    }

    @RequiredArgsConstructor
    @Getter @Accessors(fluent = true) // http://codemonkeyism.com/generation-java-programming-style/
    public static class Point {
        final private int x;
        final private int y;
    }

    @RequiredArgsConstructor(staticName="of") // a public factory method besides a private constructor.
    @Getter @Accessors(fluent = true)
    public static class Pair<U, V> { // design patterns: immutable data structure, and fluent interface.
        final private U first;
        final private V second;
    }

    @Test
    public void testFluentPair() {
        val duet = new Pair<String, BitSet>("anana", new BitSet()); // a private constructor
        val pair = Pair.of("anana", new BitSet()); // a public static factory method.
        duet.second().set(0);
        assertThat(pair.first(), equalTo("anana"));
    }

    @Getter @Setter @Accessors(fluent = true)
    public static class DList<E> {
        private DList<E> prev;
        private DList<E> next;
        private E value;

        public static <E> DList<E> of(E e) {
            return new DList<E>().value(e);
        }
    }

    public String mostBeautifulUnique(String s) {
        DList<Character> head = null, tail = null;
        val map = new HashMap<Character, DList<Character>>();
        for (int i = s.length() - 1; i >= 0; i--) {
            char c = s.charAt(i);
            if (null == head) {
                map.put(c, head = tail = DList.of(c));
            } else if (!map.containsKey(c)) {
                map.put(c, head = head.prev(DList.of(c).next(head)).prev());
            } else {
                if (c > head.value()) {
                    val node = map.get(c);
                    map.put(c, head = head.prev(DList.of(c)).prev());
                    node.prev().next(node.next());
                    if (tail != node)
                       node.next().prev(node.prev());
                    else
                       (tail = tail.prev()).next(null);
                }
            }
        }
        val sb = new StringBuilder();
        for (DList<Character> node = head; null != node; node = node.next()) {
            sb.append(node.value());
        }
        return sb.toString();
    }

    @Test
    public void testMostBeautifulUnique() {
        assertThat(mostBeautifulUnique("acbdba"), equalTo("cdba"));
        assertThat(mostBeautifulUnique("acdcab"), equalTo("dcab"));
        assertThat(mostBeautifulUnique("bcdaca"), equalTo("bdca"));
    }

    @Data // a shortcut for @Getter, @Setter, @ToString, @EqualsAndHashCode
    public class DataEntry {
        private boolean weighted;
        private boolean isDirected;
        private Boolean reachable;
        @Setter(AccessLevel.NONE) @Accessors(fluent = true)
        private boolean hasCycle; // will see getHasCycle unless fluent specified.
    }

    @Test
    public void testBooleanProperties() {
        val entry = new DataEntry();
        entry.isWeighted();
        entry.isDirected();
        entry.getReachable(); // 'null' means it is unknown until now.
        entry.hasCycle(); // should not be 'getHasCycle'
    }

    public class LazyEntry {
        @Getter(lazy = true) // DCL-based lazy getter http://projectlombok.org/features/GetterLazy.html
        private final double[] cached = expensive();

        private double[] expensive() {
            double[] result = new double[1000000];
            for (int i = 0; i < result.length; i++) {
                result[i] = Math.asin(i);
            }
            return result;
        }
    }

    @Test
    public void testCleanup() throws IOException {
        @Cleanup InputStream in = new FileInputStream("");
        @Cleanup OutputStream out = new FileOutputStream("");
        byte[] b = new byte[10000];
        while (true) {
            int r = in.read(b);
            if (r == -1) break;
            out.write(b, 0, r);
        }
    }

    // Get synchronized on fields $lock = new Object[0] and $Locks = new Object[0] for instance & static methods.
    public static class SynchronizedRight {
        private final Object readLock = new Object[0];

        @Synchronized
        public static void hello() {
            System.out.println("world");
        }

        @Synchronized
        public int answerToLife() {
            return 42;
        }

        @Synchronized("readLock")
        public void foo() {
            System.out.println("bar");
        }
    }

    @Log4j
    public static class SneakyThrowsOutOfReasoning implements Runnable {
        @SneakyThrows(UnsupportedEncodingException.class) // b/c there is no reason throw & catch impossible exceptions
        public String cannotThrowUpAsUTF8IsAlwaysAvailable(byte[] bytes) {
            return new String(bytes, "UTF-8");
        }

        // b/c throwing an unchecked exception from a needlessly strict interface only obscures the real cause of the issue.
        // @SneakyThrows({ IOException.class })
        public void run() { // this rigid interface only allows unchecked exceptions to be thrown.
            try {
                // Apache Fluent Http Client http://hc.apache.org/httpcomponents-client-ga/fluent-hc/index.html
                String result1 = org.apache.http.client.fluent.Request.Get("http://somehost/")
                        .version(HttpVersion.HTTP_1_1)
                        .connectTimeout(1000)
                        .socketTimeout(1000)
                        .viaProxy(new HttpHost("myproxy", 8080))
                        .execute().returnContent().asString();
                log.debug(result1);
            } catch (IOException e) {
                throw new RuntimeException("UNCHECKED: this bug should go unhandled.", e);
            }
        }
    }

    @Value // immutable as all fields are made private and final by default.
    public static class DTO {
        String key;
        @Wither(AccessLevel.PACKAGE) int value;
    }

    @Test
    public void testDTO() {
        val dto1 = new DTO("key", 1);
        val dto2 = dto1.withValue(2);
        Assert.assertNotSame(dto1, dto2);
    }
}