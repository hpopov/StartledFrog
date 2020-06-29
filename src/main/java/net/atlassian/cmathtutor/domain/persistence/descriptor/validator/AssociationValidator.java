package net.atlassian.cmathtutor.domain.persistence.descriptor.validator;

import lombok.NonNull;
import net.atlassian.cmathtutor.domain.persistence.AttributeArity;
import net.atlassian.cmathtutor.domain.persistence.OwnerType;
import net.atlassian.cmathtutor.domain.persistence.descriptor.IllegalOperationException;
import net.atlassian.cmathtutor.domain.persistence.model.AssociationModel;
import net.atlassian.cmathtutor.domain.persistence.model.ReferentialAttributeModel;

public class AssociationValidator {

    public static void assertAssociationIsSupported(@NonNull AssociationModel association)
	    throws IllegalOperationException {
	ReferentialAttributeModel containerAttribute = association.getContainerAttribute();
	ReferentialAttributeModel elementAttribute = association.getElementAttribute();

	assertCommonRulesAreSatisfied(containerAttribute, elementAttribute);
	assertAggregationKindRulesAreSatisfied(association);
    }

    private static void assertCommonRulesAreSatisfied(@NonNull ReferentialAttributeModel containerAttribute,
	    @NonNull ReferentialAttributeModel elementAttribute) throws IllegalOperationException {
	associationMustNotBeBidirectional(containerAttribute, elementAttribute);
	associationMustNotBeOneToOne(containerAttribute, elementAttribute);
	ReferentialAttributeModel arityOneAttribute = containerAttribute.getArity().equals(AttributeArity.ONE_EXACTLY)
		? containerAttribute
		: (elementAttribute.getArity().equals(AttributeArity.ONE_EXACTLY)
			? elementAttribute
			: null);
	arityOneAttributeMustBeOwnedByClassifier(arityOneAttribute);
    }

    private static void associationMustNotBeBidirectional(ReferentialAttributeModel containerAttribute,
	    ReferentialAttributeModel elementAttribute) throws IllegalOperationException {
	if (containerAttribute.getOwnerType().equals(OwnerType.CLASSIFIER)
		&& elementAttribute.getOwnerType().equals(OwnerType.CLASSIFIER)) {
	    throw new IllegalOperationException("Association with both side owning by classifiers is not supported");
	}
    }

    private static void associationMustNotBeOneToOne(ReferentialAttributeModel containerAttribute,
	    ReferentialAttributeModel elementAttribute) throws IllegalOperationException {
	if (containerAttribute.getArity().equals(AttributeArity.ONE_EXACTLY)
		&& elementAttribute.getArity().equals(AttributeArity.ONE_EXACTLY)) {
	    throw new IllegalOperationException("One-to-one association is not supported");
	}
    }

    private static void arityOneAttributeMustBeOwnedByClassifier(ReferentialAttributeModel arityOneAttribute)
	    throws IllegalOperationException {
	if (arityOneAttribute != null && false == arityOneAttribute.getOwnerType().equals(OwnerType.CLASSIFIER)) {
	    throw new IllegalOperationException("Arity one attribute must be owned by classifier");
	}
    }

    private static void assertAggregationKindRulesAreSatisfied(AssociationModel association)
	    throws IllegalOperationException {
	switch (association.getAggregationKind()) {
	case COMPOSITE:
	    if (association.getElementAttribute().getArity().equals(AttributeArity.AT_LEAST_ZERO)) {
		throw new IllegalOperationException("Composite element can't belong to multiple containers at a time");
	    }
	    break;
	case NONE:
	    break;
	case SHARED:
	    if (association.getContainerAttribute().getArity().equals(AttributeArity.ONE_EXACTLY)) {
		throw new IllegalOperationException("Aggregation with element of arity one should be modelled as"
			+ " plain association only");
	    }
	    break;
	default:
	    throw new IllegalStateException("Unimplemented aggregation kind detected");
	}
    }
}
