package com.henry4j.commons.collect;

import static com.google.common.collect.Ordering.natural;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.val;

import com.google.common.collect.Ordering;

// References:
// http://commons.apache.org/collections/apidocs/org/apache/commons/collections/buffer/PriorityBuffer.html
// http://docs.oracle.com/javase/7/docs/api/java/util/PriorityQueue.html
public class PriorityHeap<K, V extends Comparable<V>> {
    private final List<Pair<K, V>> list = new ArrayList<Pair<K, V>>();
    private final Map<K, Integer> map = new HashMap<K, Integer>();
    private final Ordering<V> ordering;

    public PriorityHeap() {
        ordering = natural();
    }

    public PriorityHeap(Ordering<V> ordering) {
        this.ordering = ordering;
    }

    public PriorityHeap<K, V> offer(K element, V priority) {
        val prioritized = Pair.of(element, priority);
        if (map.containsKey(element)) {
            int n = map.get(element);
            list.set(n, prioritized);
            if (n == bubbleUp(n)) {
                bubbleDown(n);
            }
        } else {
            list.add(prioritized);
            bubbleUp(list.size() - 1);
        }
        return this;
    }

    public Pair<K, V> peek() {
        return !list.isEmpty() ? list.get(0) : null;
    }

    public Pair<K, V> poll() {
        if (!list.isEmpty()) {
            val poll = list.get(0);
            list.set(0, list.get(list.size() - 1));
            list.remove(list.size() - 1);
            map.remove(poll.first());
            if (!list.isEmpty()) {
                bubbleDown(0);
            }
            return poll;
        } else {
            return null;
        }
    }

    public int bubbleUp(int n) {
        val p = (n - 1)/2;
        if (n > 0 && compare(list, p, n, ordering) > 0) {
            swap(list, p, n);
            map.put(list.get(n).first(), n);
            return bubbleUp(p);
        } else {
            map.put(list.get(n).first(), n);
            return n;
        }
    }

    public int bubbleDown(int n) {
        int c = n;
        if (2*n + 1 < list.size() && compare(list, c, 2*n + 1, ordering) > 0) {
            c = 2*n + 1;
        }
        if (2*n + 2 < list.size() && compare(list, c, 2*n + 2, ordering) > 0) {
            c = 2*n + 2;
        }
        if (c != n) { // c is the index of the smallest of list[n], list[2*n +1], and list[2*n + 2].
            swap(list, c, n);
            map.put(list.get(n).first(), n);
            return bubbleDown(c);
        } else {
            map.put(list.get(n).first(), n);
            return n;
        }
    }

    public int size() {
        return map.size();
    }

    private static <E, V> int compare(List<Pair<E, V>> list, int i, int j, Ordering<V> ordering) {
        return ordering.compare(list.get(i).second(), list.get(j).second());
    }

    private static <E> void swap(List<E> list, int i, int j) {
        E e = list.get(i);
        list.set(i, list.get(j));
        list.set(j, e);
    }
}
