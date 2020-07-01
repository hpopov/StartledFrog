package net.atlassian.cmathtutor.domain.persistence.translate.java.velocity;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.atlassian.cmathtutor.domain.persistence.translate.java.ClassType;
import net.atlassian.cmathtutor.domain.persistence.translate.java.PackagedType;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Getter
public class Repository extends AbstractDeclaration {

    private ClassType<?> superType;
    private List<Operation> operations = new LinkedList<>();

    public Repository(String name, String packageName, ClassType<?> superType) {
        super(name, packageName);
        this.superType = superType;
    }

    @Override
    public List<PackagedType> getTypesToImport() {
        return Stream.concat(Stream.of(superType),
                Stream.concat(Stream.of(superType), operations.stream())
                        .flatMap(op -> op.getContainedTypes().stream()))
                .distinct()
                .filter(type -> !type.getPackageName().equals(packageName))
                .collect(Collectors.toList());
    }
}
