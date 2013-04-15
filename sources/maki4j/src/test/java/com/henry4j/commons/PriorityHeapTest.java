package com.henry4j.commons;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import lombok.val;

import org.junit.Test;

import com.google.common.collect.Ordering;
import com.henry4j.commons.collect.Pair;
import com.henry4j.commons.collect.PriorityHeap;

public class PriorityHeapTest {
    @Test
    public void test() {
        val ph = new PriorityHeap<String, Integer>(Ordering.<Integer> natural().reverse());
        ph.offer("d", 10).offer("e", 30).offer("h", 50)
                .offer("f", 20).offer("b", 40).offer("c", 60)
                .offer("a", 80).offer("i", 90).offer("g", 70);
        ph.offer("a", 92).offer("b", 98).offer("h", 120);
        ph.offer("i", 45).offer("c", 25);
        assertThat(ph.peek(), equalTo(Pair.of("h", 120)));
        assertThat(ph.poll(), equalTo(Pair.of("h", 120)));
        assertThat(ph.poll(), equalTo(Pair.of("b", 98)));
        assertThat(ph.poll(), equalTo(Pair.of("a", 92)));
        assertThat(ph.poll(), equalTo(Pair.of("g", 70)));
        assertThat(ph.poll(), equalTo(Pair.of("i", 45)));
        assertThat(ph.poll(), equalTo(Pair.of("e", 30)));
        assertThat(ph.poll(), equalTo(Pair.of("c", 25)));
        assertThat(ph.poll(), equalTo(Pair.of("f", 20)));
        assertThat(ph.poll(), equalTo(Pair.of("d", 10)));
    }
}
