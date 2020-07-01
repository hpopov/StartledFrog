package net.atlassian.cmathtutor.domain.persistence.translate;

@SuppressWarnings("serial")
public class UnimplementedEnumConstantException extends IllegalStateException {

    public UnimplementedEnumConstantException(Enum<?> enumConstant) {
        super("Enum constant " + enumConstant.name() + " of type " + enumConstant.getDeclaringClass().getSimpleName()
                + " is not implemented");
    }
}
