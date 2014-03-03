package com.henry4j.commons.base;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import com.henry4j.commons.base.Functions.Function0;
import com.henry4j.commons.base.Functions.Function1;
import com.henry4j.commons.base.Functions.Function2;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Predicates {
    public interface Predicate0<T1> extends Function0<Boolean> {
    }

    public interface Predicate1<T1> extends Function1<T1, Boolean> {
    }

    public interface Predicate2<T1, T2> extends Function2<T1, T2, Boolean> {
    }
}
