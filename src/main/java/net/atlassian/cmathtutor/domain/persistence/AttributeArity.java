package net.atlassian.cmathtutor.domain.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AttributeArity {
    AT_MOST_ONE("0..1"), ONE_EXACTLY("1"), AT_LEAST_ZERO("*");

    private String appearance;

    public boolean isMany() {
	return AT_LEAST_ZERO == this;
    }

    public boolean isOne() {
	return AT_LEAST_ZERO != this;
    }

    public boolean isNullable() {
	return ONE_EXACTLY != this;
    }

    public boolean isNotNullable() {
	return ONE_EXACTLY == this;
    }
}
