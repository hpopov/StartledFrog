package net.atlassian.cmathtutor.domain.persistence.translate.java;

public interface NestedType extends PackagedType {

    String getDeclaringTypeName();

    String getNestedTypeName();

    @Override
    default String getName() {
        return getDeclaringTypeName() + "." + getNestedTypeName();
    }

    @Override
    default String getTypeUri() {
        return getPackageName() + "." + getDeclaringTypeName();
    }
}
