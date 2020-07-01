package net.atlassian.cmathtutor.domain.persistence.translate.java;

public interface Type extends Named {

    default boolean isGeneric() {
        return false;
    }

    default String displayParametersList() {
        return "";
    }
}
