package net.atlassian.cmathtutor.domain.persistence.translate.java.instance;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.atlassian.cmathtutor.domain.persistence.translate.java.Annotation;
import net.atlassian.cmathtutor.domain.persistence.translate.java.PackagedType;

public class AnnotationInstance<T extends java.lang.annotation.Annotation> extends AbstractInstance<Annotation<T>>
	implements ImportableInstance<Annotation<T>>, PackagedTypesContainer {

    private static final String AT_SIGN = "@";
    private static final String COMMA_SPACE = ", ";
    private Map<String, Instance<?>> annotationValues;

    public AnnotationInstance(Annotation<T> annotationType, Map<String, Instance<?>> annotationValues) {
	super(annotationType);
	this.annotationValues = annotationValues;
	validateAnnotationValues();
    }

    private void validateAnnotationValues() {
	if (false == annotationValues.keySet().containsAll(type.getRequiredMethodNames())) {
	    Predicate<? super String> containsKey = annotationValues::containsKey;
	    List<String> values = type.getRequiredMethodNames().stream().filter(containsKey.negate())
		    .collect(Collectors.toList());
	    throw new IllegalArgumentException(
		    "Annotation of type " + type.getName() + " doesn't contain values " + values);
	}
    }

    @Override
    public Set<PackagedType> getContainedTypes() {
	return annotationValues.values().stream()
		.filter(instance -> ImportableInstance.class.isAssignableFrom(instance.getClass()))
		.map(ImportableInstance.class::cast).map(ImportableInstance::getType).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
	String nameDeclaration = AT_SIGN + type.getName();
	if (annotationValues.isEmpty()) {
	    return nameDeclaration;
	}
	StringBuilder sb = new StringBuilder(nameDeclaration).append("(");
	annotationValues.entrySet().forEach(nameToValue -> {
	    sb.append(nameToValue.getKey())
		    .append(" = ")
		    .append(nameToValue.getValue().toString())
		    .append(COMMA_SPACE);
	});
	sb.replace(sb.length() - COMMA_SPACE.length(), sb.length(), ")");
	return sb.toString();
    }

}
