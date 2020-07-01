package net.atlassian.cmathtutor.domain.persistence.translate.java;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public abstract class AbstractClassBasedClassifier<T> implements Named, Packaged {

    private Class<T> clazz;

    @EqualsAndHashCode.Include
    @Override
    public String getName() {
        return clazz.getSimpleName();
    }

    @EqualsAndHashCode.Include
    @Override
    public String getPackageName() {
        return clazz.getPackage().getName();
    }
}
