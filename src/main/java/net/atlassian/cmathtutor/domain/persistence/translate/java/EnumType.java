package net.atlassian.cmathtutor.domain.persistence.translate.java;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class EnumType<T extends Enum<T>> extends AbstractClassBasedClassifier<T> implements PackagedType {

    public EnumType(Class<T> clazz) {
        super(clazz);
    }
}
