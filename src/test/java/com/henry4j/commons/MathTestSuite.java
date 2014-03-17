package com.henry4j.commons;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite;

import com.henry4j.commons.MathTestSuite.FibonacciTest;
import com.henry4j.commons.MathTestSuite.PowerTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ FibonacciTest.class, PowerTest.class })
public class MathTestSuite {
    @RunWith(Parameterized.class)
    public static class FibonacciTest {
        @Parameters(name = "{index}: fib({0})={1}")
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] { { 0, 0 }, { 1,  }, { 2, 1 }, { 3, 2 }, { 4, 3 }, { 5, 5 }, { 6, 8 } });
        }
     
        public @Parameter int input;
        public @Parameter(1) int expected;

        @Test
        public void testFib() {
            assertThat(Math.fibonacci(input), equalTo(expected));
        }
    }

    @RunWith(Parameterized.class)
    public static class PowerTest {
        @Parameters(name = "{index}: power({0},{1})={2}")
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] { { 0, 1, 0 }, { 1, 0, 1 }, { 2, 1, 2 }, { 3, 2, 9 }, { 2, 3, 8 } });
        }
     
        public @Parameter(0) int a;
        public @Parameter(1) int b;
        public @Parameter(2) int expected;

        @Test
        public void testPower() {
            assertThat(Math.power(a, b), equalTo(expected));
        }
    }

    public static class Math {
        public static int fibonacci(int i) {
            return i == 0 ? 0 : i == 1 ? 1 : fibonacci(i - 1) + fibonacci(i - 2);
        }

        public static int power(int a, int b) {
            if (b == 0) {
                return 1;
            } else {
                int y = power(a, b/ 2);
                return (b % 2 == 1 ? a : 1) * y * y;
            }
        }
    }
}