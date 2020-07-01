package net.atlassian.cmathtutor.domain.persistence.translate.java;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.EqualsAndHashCode;
import net.atlassian.cmathtutor.domain.persistence.translate.java.instance.PackagedTypesContainer;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ClassType<T> extends AbstractClassBasedClassifier<T> implements PackagedType, PackagedTypesContainer {

    private static final String COMMA_SPACE = ", ";
    private List<Type> parameters = new LinkedList<>();

    public ClassType(Class<T> clazz, Type... parameters) {
        super(clazz);
        for (Type type : parameters) {
            this.parameters.add(type);
        }
    }

    @Override
    public boolean isGeneric() {
        return !parameters.isEmpty();
    }

    public String displayParametersList() {
        StringBuilder sb = parameters.stream().map(p -> p.getName() + COMMA_SPACE).collect(StringBuilder::new,
                StringBuilder::append,
                StringBuilder::append);
        return sb.substring(0, sb.length() - COMMA_SPACE.length());
    }

    @Override
    public Set<PackagedType> getContainedTypes() {
        return parameters.stream().distinct().filter(param -> PackagedType.class.isAssignableFrom(param.getClass()))
                .map(PackagedType.class::cast).collect(Collectors.toSet());
    }
}
