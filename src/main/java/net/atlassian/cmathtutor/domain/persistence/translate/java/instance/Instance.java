package net.atlassian.cmathtutor.domain.persistence.translate.java.instance;

import net.atlassian.cmathtutor.domain.persistence.translate.java.Type;

public interface Instance<T extends Type> {

    T getType();

    String toString();
}
