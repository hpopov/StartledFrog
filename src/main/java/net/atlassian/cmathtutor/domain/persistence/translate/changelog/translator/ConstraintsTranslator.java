package net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator;

import java.util.Collection;

import net.atlassian.cmathtutor.domain.persistence.AttributeArity;
import net.atlassian.cmathtutor.domain.persistence.ConstraintType;
import net.atlassian.cmathtutor.domain.persistence.translate.UnimplementedEnumConstantException;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.Constraints;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.Constraints.ConstraintsBuilder;

public class ConstraintsTranslator {

    public Constraints translate(Collection<ConstraintType> attributeConstraints) {
	ConstraintsBuilder constraintsBuilder = Constraints.builder();
	attributeConstraints.forEach(c -> {
	    switch (c) {
	    case NON_NULL:
		constraintsBuilder.nullable(false);
		break;
	    case UNIQUE:
		constraintsBuilder.unique(true);
		break;
	    default:
		throw new UnimplementedEnumConstantException(c);
	    }
	});
	return constraintsBuilder.build();
    }

    public Constraints translateArity(AttributeArity translatedAttributeArity,
	    AttributeArity referencedAttributeArity) {
	return translateArityToConstraints(translatedAttributeArity, referencedAttributeArity,
		!translatedAttributeArity.equals(AttributeArity.ONE_EXACTLY));
    }

    private Constraints translateArityToConstraints(AttributeArity translatedAttributeArity,
	    AttributeArity referencedAttributeArity, boolean nullable) {
	return Constraints.builder()
		.nullable(nullable)
		.unique(!referencedAttributeArity.equals(AttributeArity.AT_LEAST_ZERO))
		.build();
    }

    public Constraints translateArityToJoinTableColumnConstraints(AttributeArity translatedAttributeArity,
	    AttributeArity referencedAttributeArity) {
	return Constraints.builder()
		.primaryKey(true)
		.build();
    }
}
