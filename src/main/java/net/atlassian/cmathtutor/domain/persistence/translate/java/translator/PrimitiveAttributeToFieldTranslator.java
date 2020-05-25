package net.atlassian.cmathtutor.domain.persistence.translate.java.translator;

import lombok.AllArgsConstructor;
import net.atlassian.cmathtutor.domain.persistence.PrimitiveAttribute;
import net.atlassian.cmathtutor.domain.persistence.translate.java.Type;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.VariableData;

@AllArgsConstructor
public class PrimitiveAttributeToFieldTranslator {

    private PrimitiveTypeToTypeTranslator primitiveTypeTranslator;
    private AttributeNameToVariableNameTranslator attributeNameTranslator;
    private PrimitiveAttributeConstraintsToAnnotationInstance attributeConstraintsTranslator;

    public VariableData translate(PrimitiveAttribute attribute, String columnName) {
	Type variableType = primitiveTypeTranslator.translate(attribute.getType());
	String variableName = attributeNameTranslator.translate(attribute.getName());
	VariableData field = new VariableData(variableType, variableName);
	field.getAnnotations()
		.add(attributeConstraintsTranslator.translate(attribute.getUnmodifiableConstraints(), columnName));
	return field;
    }
}
