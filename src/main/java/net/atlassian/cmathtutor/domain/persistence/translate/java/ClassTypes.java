package net.atlassian.cmathtutor.domain.persistence.translate.java;

import java.util.Collection;

import org.springframework.data.repository.CrudRepository;

import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.CompositeElementEntityData;
import ua.cmathtutor.startledfrog.repository.composition.CompositeElementRepository;

public final class ClassTypes {

    @SuppressWarnings("rawtypes")
    public static ClassType<CrudRepository> crudRepository(PackagedType entityType, PrimitiveType pkType) {
	return new ClassType<CrudRepository>(CrudRepository.class, entityType, pkType);
    }

    @SuppressWarnings("rawtypes")
    public static ClassType<CrudRepository> crudRepository(PackagedType entityType) {
	return crudRepository(entityType, PrimitiveType.LONG);
    }

    @SuppressWarnings("rawtypes")
    public static ClassType<CompositeElementRepository> compositeElementRepository(
	    CompositeElementEntityData entityType) {
	return new ClassType<CompositeElementRepository>(CompositeElementRepository.class, entityType);
    }

    @SuppressWarnings("rawtypes")
    public static ClassType<Collection> collection(Type type) {
	return new ClassType<Collection>(Collection.class, type);
    }
}
