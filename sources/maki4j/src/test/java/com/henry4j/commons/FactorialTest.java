package com.henry4j.commons;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Comparator;
import java.util.PriorityQueue;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.junit.Test;

public class FactorialTest {
    @Test
    public void testFactorial() {
        assertThat(BigInt.factorial(8), equalTo(new BigInt(new int[] { 2 * 3 * 4 * 5 * 6 * 7 * 8 }, 1)));
        assertThat(BigInt.factorial(10), equalTo(new BigInt(new int[] { 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10 }, 1)));
    }

    @ToString
    @EqualsAndHashCode
    public static class BigInt {
        final int signum;
        final int[] mag;
        final static long LONG_MASK = 0xffffffffL;
        public static final BigInt ZERO = new BigInt(new int[0], 0);

        public BigInt(int[] magnitude, int signum) {
            this.mag = stripLeadingZeroInts(magnitude);

            if (signum < -1 || signum > 1)
                throw(new NumberFormatException("Invalid signum value"));

            if (this.mag.length==0) {
                this.signum = 0;
            } else {
                if (signum == 0)
                    throw(new NumberFormatException("signum-magnitude mismatch"));
                this.signum = signum;
            }
        }

        public int compareMagnitude(BigInt val) {
            int[] m1 = mag;
            int len1 = m1.length;
            int[] m2 = val.mag;
            int len2 = m2.length;
            if (len1 < len2)
                return -1;
            if (len1 > len2)
                return 1;
            for (int i = 0; i < len1; i++) {
                int a = m1[i];
                int b = m2[i];
                if (a != b)
                    return ((a & LONG_MASK) < (b & LONG_MASK)) ? -1 : 1;
            }
            return 0;
        }

        public BigInt multiply(BigInt val) {
            if (val.signum == 0 || signum == 0)
                return ZERO;
            int[] result = multiplyToLen(mag, mag.length, val.mag, val.mag.length, null);
            result = stripLeadingZeroInts(result);
            return new BigInt(result, signum == val.signum ? 1 : -1);
        }

        public static BigInt factorial(int n) {
            PriorityQueue<BigInt> acc = new PriorityQueue<FactorialTest.BigInt>(1, new Comparator<BigInt>() {
                @Override
                public int compare(BigInt a, BigInt b) {
                    return a.compareMagnitude(b);
                }
            });
            for (int i = 2; i <= n; i++) {
                acc.offer(new BigInt(new int[] { i }, 1));
            }
            while (acc.size() > 1) {
                acc.offer(acc.poll().multiply(acc.poll()));
            }
            return acc.poll();
        }

        private int[] multiplyToLen(int[] x, int xlen, int[] y, int ylen, int[] z) {
            int xstart = xlen - 1;
            int ystart = ylen - 1;

            if (z == null || z.length < (xlen+ ylen))
                z = new int[xlen+ylen];

            long carry = 0;
            for (int j=ystart, k=ystart+1+xstart; j>=0; j--, k--) {
                long product = (y[j] & LONG_MASK) *
                               (x[xstart] & LONG_MASK) + carry;
                z[k] = (int)product;
                carry = product >>> 32;
            }
            z[xstart] = (int)carry;

            for (int i = xstart-1; i >= 0; i--) {
                carry = 0;
                for (int j=ystart, k=ystart+1+i; j>=0; j--, k--) {
                    long product = (y[j] & LONG_MASK) *
                                   (x[i] & LONG_MASK) +
                                   (z[k] & LONG_MASK) + carry;
                    z[k] = (int)product;
                    carry = product >>> 32;
                }
                z[i] = (int)carry;
            }
            return z;
        }

        private static int[] stripLeadingZeroInts(int val[]) {
            int vlen = val.length;
            int keep;

            // Find first nonzero byte
            for (keep = 0; keep < vlen && val[keep] == 0; keep++)
                ;
            return java.util.Arrays.copyOfRange(val, keep, vlen);
        }
    }
}
