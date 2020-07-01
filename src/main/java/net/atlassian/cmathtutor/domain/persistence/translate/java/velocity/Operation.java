package net.atlassian.cmathtutor.domain.persistence.translate.java.velocity;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.atlassian.cmathtutor.domain.persistence.translate.java.PackagedType;
import net.atlassian.cmathtutor.domain.persistence.translate.java.Type;
import net.atlassian.cmathtutor.domain.persistence.translate.java.instance.PackagedTypesContainer;

@Getter
@AllArgsConstructor
@Builder
public class Operation implements PackagedTypesContainer {

    private static final String COMMA_SPACE = ", ";
    private Type returnType;
    private String name;
    @Builder.Default
    private List<Variable> arguments = new LinkedList<>();

    public String displayArgumentsList() {
        if (arguments.isEmpty()) {
            return "";
        }
        StringBuilder sb = arguments.stream().map(arg -> displayArgument(arg) + COMMA_SPACE).collect(StringBuilder::new,
                StringBuilder::append, StringBuilder::append);
        return sb.substring(0, sb.length() - COMMA_SPACE.length());
    }

    private static String displayArgument(Variable arg) {
        return arg.getType().getName()
                + (arg.getType().isGeneric() ? "<" + arg.getType().displayParametersList() + ">" : "") + " "
                + arg.getName();
    }

    @Override
    public Set<PackagedType> getContainedTypes() {// TODO: nested generics are
                                                  // not handled!
        Stream<PackagedType> returnTypesToImport = Stream.empty();
        if (returnType instanceof PackagedType) {
            returnTypesToImport = Stream.concat(returnTypesToImport, Stream.of((PackagedType) returnType));
        }
        if (returnType instanceof PackagedTypesContainer) {
            returnTypesToImport = Stream.concat(returnTypesToImport,
                    ((PackagedTypesContainer) returnType).getContainedTypes().stream());
        }
        return Stream.concat(returnTypesToImport, arguments.stream().flatMap(arg -> arg.getContainedTypes().stream()))
                .collect(Collectors.toSet());
    }
}
