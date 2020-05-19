package net.atlassian.cmathtutor.domain.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AttributeArity {
    AT_MOST_ONE("0..1"), ONE("1"), AT_LEAST_ZERO("*");

    private String appearance;
}
