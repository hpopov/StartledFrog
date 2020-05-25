package net.atlassian.cmathtutor.domain.persistence.translate.java.velocity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lombok.NonNull;
import lombok.Setter;

public class TranslatedClassesData {

    @Setter
    private ApplicationData application;
    private Map<String, EntityData> nameToEntities = new HashMap<>();
    private Map<String, CompositeElementEntityData> nameToCompositeElementEntities = new HashMap<>();
    private Map<String, RepositoryData> nameToRepositoriesMap = new HashMap<>();

    public EntityData getEntityByName(@NonNull String name) {
	if (nameToEntities.containsKey(name)) {
	    return nameToEntities.get(name);
	}
	if (nameToCompositeElementEntities.containsKey(name)) {
	    return nameToCompositeElementEntities.get(name);
	}
	return null;
    }

    public CompositeElementEntityData getCompositeElementByName(@NonNull String name) {
	if (nameToCompositeElementEntities.containsKey(name)) {
	    return nameToCompositeElementEntities.get(name);
	}
	return null;
    }

    public RepositoryData getRepositoryByName(@NonNull String name) {
	if (nameToRepositoriesMap.containsKey(name)) {
	    return nameToRepositoriesMap.get(name);
	}
	return null;
    }

    public boolean addEntity(EntityData entity) {
	if (entity instanceof CompositeElementEntityData) {
	    return addCompositeElementEntity((CompositeElementEntityData) entity);
	}
	boolean containedKey = nameToEntities.containsKey(entity.getName());
	nameToEntities.put(entity.getName(), entity);
	return !containedKey;
    }

    private boolean addCompositeElementEntity(CompositeElementEntityData entity) {
	boolean containedKey = nameToCompositeElementEntities.containsKey(entity.getName());
	nameToCompositeElementEntities.put(entity.getName(), entity);
	return !containedKey;
    }

    public boolean addRepository(RepositoryData repository) {
	boolean containedKey = nameToRepositoriesMap.containsKey(repository.getName());
	nameToRepositoriesMap.put(repository.getName(), repository);
	return !containedKey;
    }

    public ApplicationData getTranslatedApplication() {
	return application;
    }

    public Collection<RepositoryData> getTranslatedRepositories() {
	return nameToRepositoriesMap.values();
    }

    public Collection<EntityData> getTranslatedEntities() {
	return nameToEntities.values();
    }

    public Collection<CompositeElementEntityData> getTranslatedCompositeElementEntities() {
	return nameToCompositeElementEntities.values();
    }
}
