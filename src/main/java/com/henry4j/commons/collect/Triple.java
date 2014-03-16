package com.henry4j.commons.collect;
 
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
 
@RequiredArgsConstructor(staticName = "of")
@Getter @Accessors(fluent = true)
@ToString
@EqualsAndHashCode
public class Triple<U, V, W> {
    private final U first;
    private final V second;
    private final W third;
}
