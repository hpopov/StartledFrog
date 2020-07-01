package net.atlassian.cmathtutor.domain.persistence.translate.java;

import java.util.Collection;
import java.util.Set;

import org.springframework.data.repository.CrudRepository;

import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.ContainableEntity;
import ua.cmathtutor.startledfrog.repository.ContainableRepository;

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
    public static ClassType<ContainableRepository> containableRepository(
            ContainableEntity entityType
    ) {
        return new ClassType<ContainableRepository>(ContainableRepository.class, entityType);
    }

    @SuppressWarnings("rawtypes")
    public static ClassType<Collection> collection(Type type) {
        return new ClassType<Collection>(Collection.class, type);
    }

    @SuppressWarnings("rawtypes")
    public static ClassType<Set> set(Type type) {
        return new ClassType<Set>(Set.class, type);
    }
}
