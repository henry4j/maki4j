package com.henry4j.commons.base;

import java.util.concurrent.Future;

import lombok.extern.log4j.Log4j;

import com.henry4j.commons.base.Actions.Action1;
import com.henry4j.commons.base.Functions.Function1;
import com.henry4j.commons.collect.Pair;

@Log4j
public class Functors {
    public static Action1<Future<Void>> JOIN_QUIETLY = new Action1<Future<Void>>() {
        @Override
        public void apply(Future<Void> f) {
            try {
                f.get();
            } catch (Exception e) {
                log.error("Exception uncaught!!!", e);
            }
        }
    };

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