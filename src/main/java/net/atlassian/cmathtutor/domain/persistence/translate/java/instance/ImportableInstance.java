package net.atlassian.cmathtutor.domain.persistence.translate.java.instance;

import net.atlassian.cmathtutor.domain.persistence.translate.java.PackagedType;

public interface ImportableInstance<T extends PackagedType> extends Instance<T> {

    @Override
    T getType();
}
