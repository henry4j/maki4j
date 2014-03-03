package com.henry4j.commons.base;

import java.net.SocketTimeoutException;
import java.util.Locale;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.henry4j.commons.base.Actions.Action1;
import com.henry4j.commons.base.Functions.Function1;
import com.henry4j.commons.collect.Pair;

import lombok.extern.log4j.Log4j;

@Log4j
public class Functors {
    public static Function1<String, String> UPCASE = new Function1<String, String>() {
        @Override
        public String apply(String s) {
            return s.toUpperCase(Locale.ENGLISH);
        }
    };

    public static Function1<String, String> DOWNCASE = new Function1<String, String>() {
        @Override
        public String apply(String s) {
            return s.toLowerCase(Locale.ENGLISH);
        }
    };

    private static Function1<Future<Object>, Object> RECEIVE_QUIETLY = new Function1<Future<Object>, Object>() {
        @Override
        public Object apply(Future<Object> f) {
            try {
                return f.get();
            } catch (Throwable e) {
                for (Throwable c = e; null != c.getCause(); c = c.getCause()) {
                    if (c.getCause() instanceof SocketTimeoutException) {
                        log.warn("Exception Uncaught!!!", c);
                        return null;
                    }
                }
                log.error("Exception Uncaught!!!", e);
                return null;
            }
        }
    };

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <E> Function1<Future<E>, E> receiveQuietly() {
        return (Function1)RECEIVE_QUIETLY;
    }

    public static Action1<Future<Void>> JOIN_QUIETLY = new Action1<Future<Void>>() {
        @Override
        public void apply(Future<Void> f) {
            try {
                f.get();
            } catch (Throwable e) {
                for (Throwable c = e; null != c.getCause(); c = c.getCause()) {
                    if (c.getCause() instanceof SocketTimeoutException) {
                        log.warn("Exception Uncaught!!!", c);
                        return;
                    }
                }
                log.error("Exception Uncaught!!!", e);
            }
        }
    };

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Action1<Future<Void>> joinQuietly(final long untilMillis) {
        return new Action1<Future<Void>>() {
            @Override
            public void apply(Future<Void> f) {
                try {
                    f.get(Math.max(0, untilMillis - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    log.warn("Timed Out!", e);
                } catch (Throwable e) {
                    for (Throwable c = e; null != c.getCause(); c = c.getCause()) {
                        if (c.getCause() instanceof SocketTimeoutException) {
                            log.warn("Exception Uncaught!!!", c);
                            return;
                        }
                    }
                    log.error("Exception Uncaught!!!", e);
                }
            }
        };
    }

    private static Function1<Pair<Object, Object>, Object> SECOND = new Function1<Pair<Object,Object>, Object>() {
        @Override
        public Object apply(Pair<Object, Object> pair) {
            return pair.second();
        }
    };

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <U, V> Function1<Pair<U, V>, V> second() {
        return (Function1)SECOND;
    }

    private static Function1<Pair<Object, Object>, Object> FIRST = new Function1<Pair<Object,Object>, Object>() {
        @Override
        public Object apply(Pair<Object, Object> pair) {
            return pair.first();
        }
    };

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <U, V> Function1<Pair<U, V>, V> first() {
        return (Function1)FIRST;
    }
}
