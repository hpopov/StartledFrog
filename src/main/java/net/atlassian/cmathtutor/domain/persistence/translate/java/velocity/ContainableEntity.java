package net.atlassian.cmathtutor.domain.persistence.translate.java.velocity;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.atlassian.cmathtutor.domain.persistence.translate.java.PackagedType;
import net.atlassian.cmathtutor.domain.persistence.translate.java.instance.AnnotationInstance;
import net.atlassian.cmathtutor.domain.persistence.translate.java.instance.AnnotationInstances;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ContainableEntity extends Entity {

    private static final List<AnnotationInstance<?>> COMPOSITE_PARENT_ANNOTATIONS = Collections
	    .singletonList(AnnotationInstances.jsonIgnore());

    private Variable compositeParentField;

    public ContainableEntity(String name, String packageName, String tableName) {
	super(name, packageName, tableName);
    }

    public void selectCompositeParentField(String parentFieldName) {
	compositeParentField = getFields().stream().filter(field -> field.getName().equals(parentFieldName)).findAny()
		.orElseThrow(() -> new IllegalArgumentException(
			"Entity " + getName() + " doesn't contain field with name " + parentFieldName));
    }

    public List<AnnotationInstance<?>> getCompositeParentAnnotations() {
	return COMPOSITE_PARENT_ANNOTATIONS;
    }

    public String getContainerGetterName() {
	return composeGetterName(compositeParentField);
    }

    private static String composeGetterName(@NonNull Variable field) {
	String fieldName = field.getName();
	return "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    @Override
    public List<PackagedType> getTypesToImport() {
	Stream<PackagedType> compositeParentAnnotationsTypesToImport = getCompositeParentAnnotations().stream()
		.flatMap(annotation -> Stream.concat(Stream.of(annotation.getType()),
			annotation.getContainedTypes().stream()));
	return Stream.concat(compositeParentAnnotationsTypesToImport,
		fields.stream().flatMap(field -> field.getContainedTypes().stream()))
		.distinct()
		.filter(type -> !type.getPackageName().equals(packageName)).collect(Collectors.toList());
    }

}
