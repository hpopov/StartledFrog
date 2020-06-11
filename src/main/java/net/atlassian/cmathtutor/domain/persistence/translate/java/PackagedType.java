package net.atlassian.cmathtutor.domain.persistence.translate.java;

public interface PackagedType extends Type, Packaged {

    default String getTypeUri() {
	return getPackageName() + "." + getName();
    }
}
