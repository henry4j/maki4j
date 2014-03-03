package com.henry4j.commons.base;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Actions {
    public interface Action0 {
        void apply();
    }

    public interface Action1<T1> {
        void apply(T1 t1);
    }

    public interface Action2<T1, T2> {
        void apply(T1 t1, T2 t2);
    }

    public interface Action3<T1, T2, T3> {
        void apply(T1 t1, T2 t2, T3 t3);
    }

    public interface Action4<T1, T2, T3, T4> {
        void apply(T1 t1, T2 t2, T3 t3, T4 t4);
    }

    public interface Action5<T1, T2, T3, T4, T5> {
        void apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5);
    }

    public interface Action6<T1, T2, T3, T4, T5, T6> {
        void apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6);
    }

    public interface Action7<T1, T2, T3, T4, T5, T6, T7> {
        void apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7);
    }

    public interface Action8<T1, T2, T3, T4, T5, T6, T7, T8> {
        void apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8);
    }
}
