package net.atlassian.cmathtutor.domain.persistence.translate.java;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PrimitiveType implements Type {

    INTEGER("Integer"),
    LONG("Long"),
    DOUBLE("Double"),
    STRING("String"),
    BOOLEAN("Boolean");

    @Getter(onMethod = @__(@Override))
    private final String name;
}
