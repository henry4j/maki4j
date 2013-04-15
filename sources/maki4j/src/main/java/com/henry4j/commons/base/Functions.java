package com.henry4j.commons.base;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Functions {
    public interface Function0<R> {
        R apply();
    }

    public interface Function1<T1, R> {
        R apply(T1 t1);
    }

    public interface Function2<T1, T2, R> {
        R apply(T1 t1, T2 t2);
    }

    public interface Function3<T1, T2, T3, R> {
        R apply(T1 t1, T2 t2, T3 t3);
    }

    public interface Function4<T1, T2, T3, T4, R> {
        R apply(T1 t1, T2 t2, T3 t3, T4 t4);
    }

    public interface Function5<T1, T2, T3, T4, T5, R> {
        R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5);
    }

    public interface Function6<T1, T2, T3, T4, T5, T6, R> {
        R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6);
    }

    public interface Function7<T1, T2, T3, T4, T5, T6, T7, R> {
        R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7);
    }

    public interface Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> {
        R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8);
    }
}
