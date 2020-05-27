package net.atlassian.cmathtutor.domain.persistence.translate.java.velocity;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import net.atlassian.cmathtutor.domain.persistence.translate.java.PackagedType;
import net.atlassian.cmathtutor.domain.persistence.translate.java.Type;
import net.atlassian.cmathtutor.domain.persistence.translate.java.instance.AnnotationInstance;
import net.atlassian.cmathtutor.domain.persistence.translate.java.instance.ImportableInstance;
import net.atlassian.cmathtutor.domain.persistence.translate.java.instance.PackagedTypesContainer;

@Getter
public class Variable implements PackagedTypesContainer {

    private Type type;
    private String name;
    private List<AnnotationInstance<?>> annotations = new LinkedList<>();

    public Variable(Type type, String name) {
	this.type = type;
	this.name = name;
    }

    @Override
    public Set<PackagedType> getContainedTypes() {
	return Stream.concat(getUsedPackagedTypes(), getUsedPackagedTypesFromContainers())
		.collect(Collectors.toSet());
    }

    private Stream<PackagedType> getUsedPackagedTypes() {
	Stream<PackagedType> annotationTypesStream = annotations.stream().map(ImportableInstance::getType);
	if (type instanceof PackagedType) {
	    return Stream.concat(Stream.of((PackagedType) type), annotationTypesStream);
	}
	return annotationTypesStream;
    }

    private Stream<PackagedType> getUsedPackagedTypesFromContainers() {
	Stream<PackagedType> annotationsContainedTypes = annotations.stream()
		.map(PackagedTypesContainer::getContainedTypes)
		.flatMap(containedTypes -> containedTypes.stream());
	if (type instanceof PackagedTypesContainer) {
	    return Stream.concat(((PackagedTypesContainer) type).getContainedTypes().stream(),
		    annotationsContainedTypes);
	}
	return annotationsContainedTypes;
    }
}
