package com.henry4j.commons;

import java.util.Arrays;

import lombok.val;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import com.henry4j.commons.base.Extensions;

public class ExtensionsTest {
    @Test
    public void testListAsleep() {
        val begins = System.currentTimeMillis();
        val ages = Arrays.asList(1, 3, 5);
        @SuppressWarnings("unused")
        val ints = Extensions.listAsleep(1000, ages);
        val ends = System.currentTimeMillis();
        val ms = ends - begins;
        Assert.assertThat(ms, Matchers.greaterThan(3000L));
    }
}
