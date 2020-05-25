package net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator;

import lombok.AllArgsConstructor;
import net.atlassian.cmathtutor.domain.persistence.PrimitiveAttribute;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.Column;

@AllArgsConstructor
public class PrimitiveAttributeToColumnTranslator {

    private AttributeNameToColumnNameTranslator nameTranslator;
    private PrimitiveTypeToColumnTypeTranslator typeTranslator;
    private PrimitiveAttributeConstraintsToConstraintsTranslator constraintsTranslator;

    public Column translate(PrimitiveAttribute attribute) {
	return Column.builder()
		.name(nameTranslator.translate(attribute.getName()))
		.type(typeTranslator.translate(attribute.getType()))
		.constraints(constraintsTranslator.translate(attribute.getUnmodifiableConstraints()))
		.build();
    }
}
