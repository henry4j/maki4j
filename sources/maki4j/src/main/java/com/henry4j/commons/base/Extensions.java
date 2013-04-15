package com.henry4j.commons.base;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

import lombok.SneakyThrows;
import lombok.val;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.henry4j.commons.base.Actions.Action1;
import com.henry4j.commons.base.Actions.Action2;
import com.henry4j.commons.base.Functions.Function1;
import com.henry4j.commons.base.Predicates.Predicate1;

public class Extensions {
    public static String hexString(int i) {
        return Integer.toHexString(i);
    }

    public static String hexString(long l) {
        return Long.toHexString(l);
    }

    public static String string(int i) {
        return String.valueOf(i);
    }

    public static String string(long l) {
        return String.valueOf(l);
    }

    public static <E> void each(Iterable<E> from, Action1<E> process) {
        for (E e : from) {
            process.apply(e);
        }
    }

    public static <E> void eachWithPosition(Iterable<E> from, Action2<E, Integer> process) {
        int position = 0;
        for (E e : from) {
            process.apply(e, position++);
        }
    }

    public static <E> boolean isEmpty(Iterable<E> iterable) {
        return Iterables.isEmpty(iterable);
    }

    @SuppressWarnings("unchecked")
    public static <E> E[] array(Iterable<E> from, Class<E> clazz) {
        val list = list(from);
        return list.toArray((E[])Array.newInstance(clazz, list.size()));
    }

    public static String[] strings(Iterable<String> from) {
        return array(from, String.class);
    }

    public static Integer[] ints(Iterable<Integer> from) {
        return array(from, Integer.class);
    }

    public static Long[] longs(Iterable<Long> from) {
        return array(from, Long.class);
    }

    @SuppressWarnings("unchecked")
    public static <E> E[] array(Collection<E> from, Class<E> clazz) {
        return from.toArray((E[])Array.newInstance(clazz, from.size()));
    }

    public static String[] strings(Collection<String> from) {
        return array(from, String.class);
    }

    public static Integer[] ints(Collection<Integer> from) {
        return array(from, Integer.class);
    }

    public static Long[] longs(Collection<Long> from) {
        return array(from, Long.class);
    }

    public static <E> ImmutableList<E> list() {
        return ImmutableList.of();
    }

    public static <E> ImmutableList<E> list(Iterable<E> from) {
        return ImmutableList.copyOf(from);
    }

    public static <E> ImmutableList<E> list(Iterator<E> from) {
        return ImmutableList.copyOf(from);
    }

    public static <E> ImmutableList<E> list(E... from) {
        return ImmutableList.copyOf(from);
    }

    @SneakyThrows({ InterruptedException.class })
    public static <E> ImmutableList<E> listAsleep(long millis, Iterable<E> from) {
        val list = ImmutableList.<E> builder();
        for (E e : from) {
            Thread.sleep(millis);
            list.add(e);
        }
        return list.build();
    }

    public static <E> ImmutableSet<E> set() {
        return ImmutableSet.of();
    }

    public static <E> ImmutableSet<E> set(Iterable<E> from) {
        return ImmutableSet.copyOf(from);
    }

    public static <E> ImmutableSet<E> set(Iterator<E> from) {
        return ImmutableSet.copyOf(from);
    }

    public static <E> ImmutableSet<E> set(E... from) {
        return ImmutableSet.copyOf(from);
    }

    public static <V> void callback(ListenableFuture<V> future, FutureCallback<? super V> callback) {
        Futures.addCallback(future, callback);
    }

    public static <V> void callback(ListenableFuture<V> future, FutureCallback<? super V> callback, Executor executor) {
        Futures.addCallback(future, callback, executor);
    }

    public static <I, O> ListenableFuture<O> reduce(ListenableFuture<I> input, AsyncFunction<? super I, ? extends O> function) {
        return Futures.transform(input, function);
    }

    public static <I, O> ListenableFuture<O> reduce(ListenableFuture<I> input, AsyncFunction<? super I, ? extends O> function, Executor executor) {
        return Futures.transform(input, function, executor);
    }

    public static <I, O> ListenableFuture<O> reduce(ListenableFuture<I> input, Function<? super I, ? extends O> function) {
        return Futures.transform(input, function);
    }

    public static <I, O> ListenableFuture<O> reduce(ListenableFuture<I> input, Function<? super I, ? extends O> function, Executor executor) {
        return Futures.transform(input, function, executor);
    }

    public static <U, V> Iterable<V> map(final Iterable<U> from, final Function1<U, V> function) {
        return Iterables.transform(from, new Function<U, V>() {
            @Override
            public V apply(U arg) {
                return function.apply(arg);
            }
        });
    }

    public static <U, V> Iterable<V> map(final Iterable<U> from, final Function<U, V> map) {
        return Iterables.transform(from, map);
    }

    public static <E> Iterable<E> select(final Iterable<E> from, final Function1<E, Boolean> predicate) {
        return FluentIterable.from(from).filter(new Predicate<E>() {
            @Override
            public boolean apply(E arg) {
                return predicate.apply(arg);
            }
        });
    }

    public static <E> Iterable<E> select(final Iterable<E> from, final Predicate1<E> predicate) {
        return FluentIterable.from(from).filter(new Predicate<E>() {
            @Override
            public boolean apply(E arg) {
                return predicate.apply(arg);
            }
        });
    }

    public static <E> Iterable<E> select(final Iterable<E> from, final Predicate<E> predicate) {
        return Iterables.filter(from, predicate);
    }

    public static <E> Iterable<E> concat(final Iterable<E> ones, final Iterable<E> others) {
        return Iterables.concat(ones, others);
    }

    public static <E> Iterable<List<E>> partition(final Iterable<E> from, final int size) {
        return Iterables.partition(from, size);
    }

    public static <E> E at(final Iterable<E> from, final int position) {
        return Iterables.get(from, position);
    }

    public static <K, V> ImmutableMap<K, V> hash() {
        return ImmutableMap.of();
    }

    public static <K, V> ImmutableMap<K, V> hash(K k1, V v1) {
        return ImmutableMap.of(k1, v1);
    }

    public static <K, V> ImmutableMap<K, V> hash(K k1, V v1, K k2, V v2) {
        return ImmutableMap.of(k1, v1, k2, v2);
    }

    public static <K, V> ImmutableMap<K, V> hash(K k1, V v1, K k2, V v2, K k3, V v3) {
        return ImmutableMap.of(k1, v1, k2, v2, k3, v3);
    }

    public static <K, V> ImmutableMap<K, V> hash(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4);
    }

    public static <K, V> ImmutableMap<K, V> hash(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return ImmutableMap.<K, V> builder()
                .put(k1, v1)
                .put(k2, v2)
                .put(k3, v3)
                .put(k4, v4)
                .put(k5, v5)
                .build();
    }

    public static <K, V> ImmutableMap<K, V> hash(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        return ImmutableMap.<K, V> builder()
                .put(k1, v1)
                .put(k2, v2)
                .put(k3, v3)
                .put(k4, v4)
                .put(k5, v5)
                .put(k6, v6)
                .build();
    }

    @Deprecated
    public static <K, V> ImmutableMap<K, V> mapOf() {
        return ImmutableMap.of();
    }

    @Deprecated
    public static <K, V> ImmutableMap<K, V> mapOf(K k1, V v1) {
        return ImmutableMap.of(k1, v1);
    }

    @Deprecated
    public static <K, V> ImmutableMap<K, V> mapOf(K k1, V v1, K k2, V v2) {
        return ImmutableMap.of(k1, v1, k2, v2);
    }

    @Deprecated
    public static <K, V> ImmutableMap<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        return ImmutableMap.of(k1, v1, k2, v2, k3, v3);
    }

    @Deprecated
    public static <K, V> ImmutableMap<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4);
    }

    @Deprecated
    public static <K, V> ImmutableMap<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return ImmutableMap.<K, V> builder()
                .put(k1, v1)
                .put(k2, v2)
                .put(k3, v3)
                .put(k4, v4)
                .put(k5, v5)
                .build();
    }

    @Deprecated
    public static <K, V> ImmutableMap<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        return ImmutableMap.<K, V> builder()
                .put(k1, v1)
                .put(k2, v2)
                .put(k3, v3)
                .put(k4, v4)
                .put(k5, v5)
                .put(k6, v6)
                .build();
    }
}
