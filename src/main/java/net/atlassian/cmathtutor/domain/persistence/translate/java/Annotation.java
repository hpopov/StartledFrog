package net.atlassian.cmathtutor.domain.persistence.translate.java;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.EqualsAndHashCode;
import net.atlassian.cmathtutor.util.Lazy;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Annotation<T extends java.lang.annotation.Annotation> extends AbstractClassBasedClassifier<T>
	implements PackagedType {

    private Map<String, Boolean> existedMethods;

    protected Annotation(Class<T> clazz) {
	super(clazz);
	existedMethods = new HashMap<>();
	for (Method m : clazz.getDeclaredMethods()) {
	    existedMethods.put(m.getName(), m.getDefaultValue() == null);
	}
    }

    @SuppressWarnings("unchecked")
    public static <T extends java.lang.annotation.Annotation> Annotation<T> of(Class<T> clazz) {
	Annotation<T> annotation = (Annotation<T>) FlyWeight.getInstance().createdInstances.get(clazz);
	if (annotation == null) {
	    if (clazz.getDeclaringClass() == null) {
		annotation = new Annotation<T>(clazz);
	    } else {
		annotation = new NestedAnnotation<T>(clazz);
	    }
	    FlyWeight.getInstance().createdInstances.put(clazz, annotation);
	}
	return annotation;
    }

    public Collection<String> getRequiredMethodNames() {
	return existedMethods.keySet().stream().filter(existedMethods::get).collect(Collectors.toList());
    }

    public Collection<String> getAvailableMethodNames() {
	return existedMethods.keySet();
    }

    public static class FlyWeight {

	private static final Lazy<FlyWeight> INSTANCE = Lazy.of(FlyWeight::new);

	public static FlyWeight getInstance() {
	    return INSTANCE.get();
	}

	public static void clearInstance() {
	    INSTANCE.clear();
	}

	private Map<Class<? extends java.lang.annotation.Annotation>, Annotation<?>> createdInstances;

	public FlyWeight() {
	    createdInstances = new HashMap<>();
	}
    }

    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    public static class NestedAnnotation<T extends java.lang.annotation.Annotation> extends Annotation<T>
	    implements NestedType {

	private String declaringClassName;

	protected NestedAnnotation(Class<T> clazz) {
	    super(clazz);
	    declaringClassName = clazz.getDeclaringClass().getSimpleName();
	}

	@EqualsAndHashCode.Include
	@Override
	public String getDeclaringTypeName() {
	    return declaringClassName;
	}

	@EqualsAndHashCode.Include
	@Override
	public String getNestedTypeName() {
	    return super.getName();
	}

	@EqualsAndHashCode.Include
	@Override
	public String getName() {
	    return NestedType.super.getName();
	}

    }
}
