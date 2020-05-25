package net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator;

import java.util.Collection;

import net.atlassian.cmathtutor.domain.persistence.ConstraintType;
import net.atlassian.cmathtutor.domain.persistence.translate.UnimplementedEnumConstantException;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.Constraints;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.Constraints.ConstraintsBuilder;

public class PrimitiveAttributeConstraintsToConstraintsTranslator {

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
}
