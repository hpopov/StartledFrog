package net.atlassian.cmathtutor.domain.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PrimitiveType {
    BOOLEAN("Boolean"),
    INTEGER("Integer"),
    BIG_INTEGER("Big integer"),
    REAL("Real"),
    STRING("String");

    private String appearance;
}
