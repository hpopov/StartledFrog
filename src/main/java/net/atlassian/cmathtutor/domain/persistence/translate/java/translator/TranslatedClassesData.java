package net.atlassian.cmathtutor.domain.persistence.translate.java.translator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lombok.NonNull;
import lombok.Setter;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.Application;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.ContainableEntity;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.Entity;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.Repository;

public class TranslatedClassesData {

    @Setter
    private Application application;
    private Map<String, Entity> nameToEntities = new HashMap<>();
    private Map<String, ContainableEntity> nameToContainableEntities = new HashMap<>();
    private Map<String, Repository> nameToRepositoriesMap = new HashMap<>();

    public Entity getEntityByPersistenceUnitName(@NonNull String persistenceUnitName) {
	if (nameToEntities.containsKey(persistenceUnitName)) {
	    return nameToEntities.get(persistenceUnitName);
	}
	return getContainableEntityByPersistenceUnitName(persistenceUnitName);
    }

    public ContainableEntity getContainableEntityByPersistenceUnitName(@NonNull String persistenceUnitName) {
	if (nameToContainableEntities.containsKey(persistenceUnitName)) {
	    return nameToContainableEntities.get(persistenceUnitName);
	}
	return null;
    }

    public Repository getRepositoryByPersistenceUnitName(@NonNull String persistenceUnitName) {
	if (nameToRepositoriesMap.containsKey(persistenceUnitName)) {
	    return nameToRepositoriesMap.get(persistenceUnitName);
	}
	return null;
    }

    public boolean addEntity(@NonNull String persistenceUnitName, Entity entity) {
	if (entity instanceof ContainableEntity) {
	    return addContainableEntity(persistenceUnitName, (ContainableEntity) entity);
	}
	boolean containedKey = nameToEntities.containsKey(persistenceUnitName);
	nameToEntities.put(persistenceUnitName, entity);
	return !containedKey;
    }

    private boolean addContainableEntity(@NonNull String persistenceUnitName, ContainableEntity entity) {
	boolean containedKey = nameToContainableEntities.containsKey(persistenceUnitName);
	nameToContainableEntities.put(persistenceUnitName, entity);
	return !containedKey;
    }

    public boolean addRepository(@NonNull String persistenceUnitName, Repository repository) {
	boolean containedKey = nameToRepositoriesMap.containsKey(persistenceUnitName);
	nameToRepositoriesMap.put(persistenceUnitName, repository);
	return !containedKey;
    }

    public Application getTranslatedApplication() {
	return application;
    }

    public Collection<Repository> getTranslatedRepositories() {
	return nameToRepositoriesMap.values();
    }

    public Collection<Entity> getTranslatedEntities() {
	return nameToEntities.values();
    }

    public Collection<ContainableEntity> getTranslatedContainableEntities() {
	return nameToContainableEntities.values();
    }
}
