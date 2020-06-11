package net.atlassian.cmathtutor.domain.persistence.translate.java.instance;

import java.util.Set;

import net.atlassian.cmathtutor.domain.persistence.translate.java.PackagedType;

public interface PackagedTypesContainer {

    Set<PackagedType> getContainedTypes();
}
