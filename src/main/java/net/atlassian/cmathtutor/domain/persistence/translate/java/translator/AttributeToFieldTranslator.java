package net.atlassian.cmathtutor.domain.persistence.translate.java.translator;

import static net.atlassian.cmathtutor.domain.persistence.translate.java.instance.AnnotationInstances.column;
import static net.atlassian.cmathtutor.domain.persistence.translate.java.instance.AnnotationInstances.equalsAndHashCodeExclude;
import static net.atlassian.cmathtutor.domain.persistence.translate.java.instance.AnnotationInstances.generatedValueIdentity;
import static net.atlassian.cmathtutor.domain.persistence.translate.java.instance.AnnotationInstances.id;
import static net.atlassian.cmathtutor.domain.persistence.translate.java.instance.AnnotationInstances.jsonIgnore;
import static net.atlassian.cmathtutor.domain.persistence.translate.java.instance.AnnotationInstances.restResource;
import static net.atlassian.cmathtutor.domain.persistence.translate.java.instance.AnnotationInstances.toStringExclude;

import java.util.Arrays;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;

import org.apache.commons.lang3.tuple.ImmutablePair;

import lombok.AllArgsConstructor;
import net.atlassian.cmathtutor.domain.persistence.AttributeArity;
import net.atlassian.cmathtutor.domain.persistence.PrimitiveAttribute;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;
import net.atlassian.cmathtutor.domain.persistence.translate.TranslatorHelper;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator.AttributeToColumnTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator.JoinTableData;
import net.atlassian.cmathtutor.domain.persistence.translate.java.ClassTypes;
import net.atlassian.cmathtutor.domain.persistence.translate.java.PrimitiveType;
import net.atlassian.cmathtutor.domain.persistence.translate.java.Type;
import net.atlassian.cmathtutor.domain.persistence.translate.java.instance.AnnotationInstance;
import net.atlassian.cmathtutor.domain.persistence.translate.java.instance.AnnotationInstances;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.Entity;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.Variable;

@AllArgsConstructor
public class AttributeToFieldTranslator {

    private static final PrimitiveType PK_FIELD_TYPE = PrimitiveType.LONG;
    private static final String PK_FIELD_NAME = "pk";
    private static final Variable PK_FIELD;

    static {
	PK_FIELD = new Variable(PK_FIELD_TYPE, PK_FIELD_NAME);
	PK_FIELD.getAnnotations().addAll(
		Arrays.asList(id(), generatedValueIdentity(), column(AttributeToColumnTranslator.PK_COLUMN_NAME)));
    }

    private PrimitiveTypeToTypeTranslator primitiveTypeTranslator;
    private AttributeNameToVariableNameTranslator attributeNameTranslator;
    private AnnotationInstanceTranslator annotationInstanceTranslator;

    public Variable translatePrimitiveAttribute(PrimitiveAttribute attribute, String columnName) {
	Type variableType = primitiveTypeTranslator.translate(attribute.getType());
	String variableName = attributeNameTranslator.translate(attribute.getName());
	Variable field = new Variable(variableType, variableName);
	field.getAnnotations()
		.add(annotationInstanceTranslator.translateToColumn(attribute.getUnmodifiableConstraints(),
			columnName));
	return field;
    }

    public Variable pkField() {
	return PK_FIELD;
    }

    public Variable translatePrimaryReferentialAttribute(ReferentialAttribute primaryAttr,
	    Entity secondaryEntity, String joinColumnName, CascadeType fieldCascade, CascadeType... fieldCascades) {
	Variable primaryReferenceField = translateReferentialAttribute(primaryAttr, secondaryEntity);
	primaryReferenceField.getAnnotations()
		.add(getArityAnnotationForPrimaryReferentialAttribute(primaryAttr, fieldCascade, fieldCascades));
	ReferentialAttribute secondaryAttr = TranslatorHelper.getAnotherAttributeFromAssociation(primaryAttr);

	primaryReferenceField.getAnnotations().add(annotationInstanceTranslator
		.translateArityToJoinColumn(primaryAttr.getArity(), secondaryAttr.getArity(), joinColumnName));
	return primaryReferenceField;
    }

    public Variable translatePrimaryReferentialAttribute(ReferentialAttribute primaryAttr,
	    Entity secondaryEntity, JoinTableData joinTable, CascadeType fieldCascade,
	    CascadeType... fieldCascades) {
	Variable primaryReferenceField = translateReferentialAttribute(primaryAttr, secondaryEntity);
	primaryReferenceField.getAnnotations()
		.add(getArityAnnotationForPrimaryReferentialAttribute(primaryAttr, fieldCascade, fieldCascades));
	ReferentialAttribute secondaryAttr = TranslatorHelper.getAnotherAttributeFromAssociation(primaryAttr);

	AnnotationInstance<JoinColumn> inverseJoinColumn = annotationInstanceTranslator
		.translateArityToJoinColumnInJoinTable(secondaryAttr.getArity(),
			joinTable.getPrimaryAttributeJoinColumn().getName());
	AnnotationInstance<JoinColumn> joinColumn = annotationInstanceTranslator
		.translateArityToJoinColumnInJoinTable(primaryAttr.getArity(),
			joinTable.getSecondaryAttributeJoinColumn().getName());
	primaryReferenceField.getAnnotations().add(AnnotationInstances.joinTableBuilder()
		.inverseJoinColumns(inverseJoinColumn)
		.joinColumns(joinColumn)
		.name(joinTable.getName())
		.build());
	return primaryReferenceField;
    }

    public Variable translateReferentialAttribute(ReferentialAttribute attribute, Entity referencedEntity) {
	Type primaryReferenceType = translateAttributeArityToType(referencedEntity, attribute.getArity());
	return new Variable(primaryReferenceType, attributeNameTranslator.translate(attribute.getName()));
    }

    public Type translateAttributeArityToType(Entity referencedEntity, AttributeArity attributeArity) {
	return attributeArity.isMany()
		? ClassTypes.set(referencedEntity)
		: referencedEntity;
    }

    private AnnotationInstance<?> getArityAnnotationForPrimaryReferentialAttribute(ReferentialAttribute primaryAttr,
	    CascadeType fieldCascade, CascadeType... fieldCascades) {
	ReferentialAttribute primaryAttribute = TranslatorHelper.getAnotherAttributeFromAssociation(primaryAttr);
	ImmutablePair<AttributeArity, AttributeArity> arity = new ImmutablePair<>(primaryAttribute.getArity(),
		primaryAttr.getArity());
	if (arity.getLeft().isMany()) {
	    if (arity.getRight().isMany()) {// many to many
		return AnnotationInstances.manyToManyBuilder()
			.cascade(fieldCascade, fieldCascades)
			.fetch(FetchType.LAZY)
			.build();
	    } else {// many to one
		return AnnotationInstances.manyToOneBuilder()
			.cascade(fieldCascade, fieldCascades)
			.fetch(FetchType.LAZY)
			.optional(arity.getRight().equals(AttributeArity.AT_MOST_ONE))
			.build();
	    }
	} else {
	    if (arity.getRight().isMany()) {// one to many
		return AnnotationInstances.oneToManyBuilder()
			.cascade(fieldCascade, fieldCascades)
			.fetch(FetchType.LAZY)
			.build();
	    } else {// one to one
		return AnnotationInstances.oneToOneBuilder()
			.cascade(fieldCascade, fieldCascades)
			.fetch(FetchType.LAZY)// changed from eager
			.optional(arity.getRight().equals(AttributeArity.AT_MOST_ONE))
			.build();
	    }
	}
    }

    public Variable translateSecondaryReferentialAttribute(ReferentialAttribute secondaryAttr,
	    Entity primaryEntity, CascadeType fieldCascade, CascadeType... fieldCascades) {
	Variable secondaryReferenceField = translateReferentialAttribute(secondaryAttr, primaryEntity);
	secondaryReferenceField.getAnnotations().addAll(Arrays.asList(
		equalsAndHashCodeExclude(), toStringExclude(), jsonIgnore(), restResource(false)));
	if (secondaryAttr.getArity().isOne()
		&& TranslatorHelper.getAnotherAttributeFromAssociation(secondaryAttr).getArity().isMany()) {
	    secondaryReferenceField.getAnnotations().add(AnnotationInstances.javaxPersistenceTransient());
	}
	secondaryReferenceField.getAnnotations()
		.add(getArityAnnotationForSecondaryReferentialAttribute(secondaryAttr, fieldCascade,
			fieldCascades));

	return secondaryReferenceField;
    }

    private AnnotationInstance<?> getArityAnnotationForSecondaryReferentialAttribute(ReferentialAttribute secondaryAttr,
	    CascadeType fieldCascade,
	    CascadeType... fieldCascades) {
	ReferentialAttribute primaryAttribute = TranslatorHelper.getAnotherAttributeFromAssociation(secondaryAttr);
	ImmutablePair<AttributeArity, AttributeArity> arity = new ImmutablePair<>(primaryAttribute.getArity(),
		secondaryAttr.getArity());
	String primaryFieldName = attributeNameTranslator.translate(primaryAttribute.getName());
	if (arity.getLeft().isMany()) {
	    if (arity.getRight().isMany()) {// many to many
		return AnnotationInstances.manyToManyBuilder()
			.cascade(fieldCascade, fieldCascades)
			.fetch(FetchType.LAZY)
			.mappedBy(primaryFieldName)
			.build();
	    } else {// many to one
		return AnnotationInstances.manyToOneBuilder()
			.cascade(fieldCascade, fieldCascades)
			.fetch(FetchType.LAZY)
			.optional(arity.getRight().equals(AttributeArity.AT_MOST_ONE))
			.build();
	    }
	} else {
	    if (arity.getRight().isMany()) {// one to many
		return AnnotationInstances.oneToManyBuilder()
			.cascade(fieldCascade, fieldCascades)
			.fetch(FetchType.LAZY)
			.mappedBy(primaryFieldName)
			.build();
	    } else {// one to one
		return AnnotationInstances.oneToOneBuilder()
			.cascade(fieldCascade, fieldCascades)
			.fetch(FetchType.LAZY)// changed from EAGER
			.optional(arity.getRight().equals(AttributeArity.AT_MOST_ONE))
			.mappedBy(primaryFieldName)
			.build();
	    }
	}
    }
}
