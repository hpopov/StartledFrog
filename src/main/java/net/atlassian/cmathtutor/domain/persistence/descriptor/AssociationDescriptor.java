package net.atlassian.cmathtutor.domain.persistence.descriptor;

import javafx.beans.property.ReadOnlyObjectProperty;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.domain.persistence.AggregationKind;
import net.atlassian.cmathtutor.domain.persistence.Association;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;
import net.atlassian.cmathtutor.domain.persistence.descriptor.validator.AssociationValidator;
import net.atlassian.cmathtutor.domain.persistence.model.AssociationModel;
import net.atlassian.cmathtutor.domain.persistence.model.ReferentialAttributeModel;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AssociationDescriptor extends AbstractDescriptor implements Association {

    private AssociationModel association;
    private PersistenceDescriptor parentDescriptor;

    private boolean detached = false;
    // private Memento memento;

    public static AssociationDescriptor wrap(
            @NonNull AssociationModel association,
            @NonNull PersistenceDescriptor parentDescriptor
    ) throws IllegalOperationException {
        if (association.getId() == null) {
            throw new IllegalArgumentException("Association must have non-null id");
        }
        if (association.getAggregationKind() == null) {
            throw new IllegalArgumentException("Association must have non-null aggregationKind");
        }
        validateAssociationAttributes(association.getContainerAttribute(), association.getElementAttribute());
        AssociationValidator.assertAssociationIsSupported(association);
        return new AssociationDescriptor(association, parentDescriptor);
    }

    private static void validateAssociationAttributes(
            @NonNull ReferentialAttributeModel containerAttribute,
            @NonNull ReferentialAttributeModel elementAttribute
    ) {
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
        if (containerAttribute.getAssociation() == null || elementAttribute.getAssociation() == null) {
            throw new IllegalArgumentException("Both container and element attributes must have non-null association");
        }
    }

    private AssociationDescriptor(AssociationModel association, PersistenceDescriptor parentDescriptor) {
        super(association.getId());
        this.association = association;
        this.parentDescriptor = parentDescriptor;
    }

    public void detachFromParent() {
        if (true == detached) {
            log.warn("detachFromParent() was called, but descriptor {} is ALREADY detached", getId());
            return;
        }
        detached = true;
        parentDescriptor.detachAssociation(this);
        ReferentialAttributeModel containerAttribute = association.getContainerAttribute();
        ReferentialAttributeModel elementAttribute = association.getElementAttribute();
        PersistenceUnitDescriptor containerDescriptor = parentDescriptor
                .getPersistenceUnitDescriptorById(containerAttribute.getParentClassifier().getId());
        PersistenceUnitDescriptor elementDescriptor = parentDescriptor
                .getPersistenceUnitDescriptorById(elementAttribute.getParentClassifier().getId());
        boolean containerRemoved = containerDescriptor.removeReferentialAttribute(containerAttribute);
        log.debug("Detaching association {} from parent, container attribute was removed? {}", getId(),
                containerRemoved);
        boolean elementRemoved = elementDescriptor.removeReferentialAttribute(elementAttribute);
        log.debug("Detaching association {} from parent, element attribute was removed? {}", getId(), elementRemoved);
    }

    public void attachToParent() throws IllegalOperationException {
        if (false == detached) {
            log.warn("attachToParent() was called, but descriptor {} is NOT detached", getId());
            return;
        }
        ReferentialAttributeModel containerAttribute = association.getContainerAttribute();
        ReferentialAttributeModel elementAttribute = association.getElementAttribute();
        PersistenceUnitDescriptor containerPersistenceUnitDescriptor = parentDescriptor
                .getPersistenceUnitDescriptorById(containerAttribute.getParentClassifier().getId());
        log.debug("Attaching association: container PU descriptor is {}", containerPersistenceUnitDescriptor);
        PersistenceUnitDescriptor elementPersistenceUnitDescriptor = parentDescriptor
                .getPersistenceUnitDescriptorById(elementAttribute.getParentClassifier().getId());
        log.debug("Attaching association: element PU descriptor is {}", containerPersistenceUnitDescriptor);
        containerPersistenceUnitDescriptor.addReferentialAttribute(containerAttribute);
        elementPersistenceUnitDescriptor.addReferentialAttribute(elementAttribute);
        parentDescriptor.attachAssociation(this);
        detached = false;
    }

    public AssociationModel getWrappedAssociation() {
        return association;
    }

    @Override
    public ReadOnlyObjectProperty<AggregationKind> aggregationKindProperty() {
        return association.aggregationKindProperty();
    }

    @Override
    public AggregationKind getAggregationKind() {
        return association.getAggregationKind();
    }

    public ReadOnlyObjectProperty<? extends ReferentialAttribute> containerAttributeProperty() {
        return association.containerAttributeProperty();
    }

    @Override
    public ReferentialAttribute getContainerAttribute() {
        return association.getContainerAttribute();
    }

    public ReadOnlyObjectProperty<? extends ReferentialAttribute> elementAttributeProperty() {
        return association.elementAttributeProperty();
    }

    @Override
    public ReferentialAttribute getElementAttribute() {
        return association.getElementAttribute();
    }

    @Override
    public PersistenceDescriptor getPersistence() {
        return parentDescriptor;
    }
    //
    // @Getter
    // @AllArgsConstructor(access = AccessLevel.PRIVATE)
    // private static class Memento {
    // private String containerPersistenceUnitId;
    // private String elementPersistenceUnitId;
    //
    // public static Memento of(AssociationDescriptor descriptor) {
    // return new
    // Memento(descriptor.getContainerAttribute().getParentClassifier().getId(),
    // descriptor.getElementAttribute().getParentClassifier().getId());
    // }
    // }
}
