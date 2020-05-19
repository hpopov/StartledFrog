package net.atlassian.cmathtutor.domain.persistence.descriptor;

import javafx.beans.property.ReadOnlyObjectProperty;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.atlassian.cmathtutor.domain.persistence.AggregationKind;
import net.atlassian.cmathtutor.domain.persistence.Association;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;
import net.atlassian.cmathtutor.domain.persistence.model.AssociationModel;
import net.atlassian.cmathtutor.domain.persistence.model.ReferentialAttributeModel;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AssociationDescriptor extends AbstractDescriptor implements Association {

    private AssociationModel association;

    private AssociationDescriptor(AssociationModel association) {
	super(association.getId());
	this.association = association;
    }

    public static AssociationDescriptor wrap(@NonNull AssociationModel association) {
	if (association.getId() == null) {
	    throw new IllegalArgumentException("Association must have non-null id");
	}
	if (association.getAggregationKind() == null) {
	    throw new IllegalArgumentException("Association must have non-null aggregationKind");
	}
	if (association.getPersistence() != null) {
	    throw new IllegalArgumentException("Association must NOT have persistence yet");
	}
	validateAssociationAttributes(association.getContainerAttribute(), association.getElementAttribute());
	return new AssociationDescriptor(association);
    }

    private static void validateAssociationAttributes(@NonNull ReferentialAttributeModel containerAttribute,
	    @NonNull ReferentialAttributeModel elementAttribute) {
	if (containerAttribute.getArity() == null || elementAttribute.getArity() == null) {
	    throw new IllegalArgumentException("Both container and element attributes must have non-null arity");
	}
	if (containerAttribute.getName() == null || elementAttribute.getName() == null) {
	    throw new IllegalArgumentException("Both container and element attributes must have non-null name");
	}
	if (containerAttribute.getOwnerType() == null || elementAttribute.getOwnerType() == null) {
	    throw new IllegalArgumentException("Both container and element attributes must have non-null owner type");
	}
	if (containerAttribute.getParentClassifier() == null || elementAttribute.getParentClassifier() == null) {
	    throw new IllegalArgumentException(
		    "Both container and element attributes must have non-null parent classifier");
	}
    }

    @Override
    public ReadOnlyObjectProperty<AggregationKind> aggregationKindProperty() {
	return association.aggregationKindProperty();
    }

    @Override
    public AggregationKind getAggregationKind() {
	return association.getAggregationKind();
    }

    @Override
    public ReadOnlyObjectProperty<? extends ReferentialAttribute> containerAttributeProperty() {
	return association.containerAttributeProperty();
    }

    @Override
    public ReferentialAttribute getContainerAttribute() {
	return association.getContainerAttribute();
    }

    @Override
    public ReadOnlyObjectProperty<? extends ReferentialAttribute> elementAttributeProperty() {
	return association.elementAttributeProperty();
    }

    @Override
    public ReferentialAttribute getElementAttribute() {
	return association.getElementAttribute();
    }

}
