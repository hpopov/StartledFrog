package net.atlassian.cmathtutor.domain.persistence.descriptor;

import java.util.Set;
import java.util.stream.Collectors;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ObservableSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.domain.persistence.Association;
import net.atlassian.cmathtutor.domain.persistence.PersistenceUnit;
import net.atlassian.cmathtutor.domain.persistence.PrimitiveAttribute;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;
import net.atlassian.cmathtutor.domain.persistence.RepositoryOperation;
import net.atlassian.cmathtutor.domain.persistence.model.PersistenceUnitModel;
import net.atlassian.cmathtutor.domain.persistence.model.PrimitiveAttributeModel;
import net.atlassian.cmathtutor.domain.persistence.model.ReferentialAttributeModel;
import net.atlassian.cmathtutor.domain.persistence.model.RepositoryOperationModel;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PersistenceUnitDescriptor extends AbstractDescriptor implements PersistenceUnit {

    private PersistenceUnitModel persistenceUnit;
    private PersistenceDescriptor parentDescriptor;

    private boolean detached = false;
    private Set<AssociationDescriptor> detachedAssociations;

    private PersistenceUnitDescriptor(PersistenceUnitModel persistenceUnit,
	    PersistenceDescriptor parentDescriptor) {
	super(persistenceUnit.getId());
	this.persistenceUnit = persistenceUnit;
	this.parentDescriptor = parentDescriptor;
    }

    public static PersistenceUnitDescriptor wrap(@NonNull PersistenceUnitModel persistenceUnit,
	    @NonNull PersistenceDescriptor parentDescriptor) {
	if (persistenceUnit.getId() == null) {
	    throw new IllegalArgumentException("Id of the persistence unit to wrap must not be null");
	}
	return new PersistenceUnitDescriptor(persistenceUnit, parentDescriptor);
    }

    public void addNewPrimitiveAttribute(@NonNull PrimitiveAttributeModel primitiveAttribute)
	    throws IllegalOperationException {
	assertPersistenceUnitNotContainAttributeWithName(primitiveAttribute.getName());
	persistenceUnit.getPrimitiveAttributes().add(primitiveAttribute);
    }

    public void addReferentialAttribute(@NonNull ReferentialAttributeModel referentialAttribute)
	    throws IllegalOperationException {
	assertPersistenceUnitNotContainAttributeWithName(referentialAttribute.getName());
	persistenceUnit.getReferentialAttributes().add(referentialAttribute);
    }

    private void assertPersistenceUnitNotContainAttributeWithName(@NonNull String attributeName)
	    throws IllegalOperationException {
	if (persistenceUnit.getPrimitiveAttributes()
		.contains(PrimitiveAttributeModel.makeIdentifiableInstance(attributeName, persistenceUnit))) {
	    throw new IllegalOperationException(
		    "Persistence unit already contains primitive attribute with name " + attributeName);
	}
	if (persistenceUnit.getReferentialAttributes()
		.contains(ReferentialAttributeModel.makeIdentifiableInstance(attributeName, persistenceUnit))) {
	    throw new IllegalOperationException(
		    "Persistence unit already contains referential attribute with name " + attributeName);
	}
    }

    public void addNewRepositoryOperation(@NonNull RepositoryOperationModel repositoryOperation)
	    throws IllegalOperationException {
	String name = repositoryOperation.getName();
	if (name == null) {
	    throw new IllegalArgumentException("Name of the repository operation to add must not be null");
	}
	if (persistenceUnit.getRepositoryOperations()
		.contains(RepositoryOperationModel.makeIdentifiableInstance(name, persistenceUnit))) {
	    throw new IllegalOperationException(
		    "Persistence unit already contains repository operation with the same name");
	}
	persistenceUnit.getRepositoryOperations().add(repositoryOperation);
    }

    public void detachFromParent() {
	log.debug("detachFromParent is called");
	if (true == detached) {
	    log.warn("detachFromParent() was called, but descriptor {} is ALREADY detached", getId());
	    return;
	}
	detached = true;
	detachedAssociations = getUnmodifiableReferentialAttributes().stream()
		.map(ReferentialAttribute::getAssociation)
		.map(Association::getId)
		.map(parentDescriptor::getAssociationDescriptorById)
		.collect(Collectors.toSet());
	detachedAssociations.forEach(AssociationDescriptor::detachFromParent);
	parentDescriptor.detachPersistenceUnit(this);
    }

    public void attachToParent() throws IllegalOperationException {
	log.debug("attachToParent is called");
	if (false == detached) {
	    log.warn("attachToParent() was called, but descriptor {} is NOT detached", getId());
	    return;
	}
	parentDescriptor.attachPersistenceUnit(this);
	for (AssociationDescriptor detachedAssociation : detachedAssociations) {
	    detachedAssociation.attachToParent();
	}
	detachedAssociations = null;
	detached = false;
	log.debug("attachToParent finished successfully");
    }

    public PersistenceUnitModel getWrappedPersistenceUnit() {
	return persistenceUnit;
    }

    public ReadOnlyStringProperty nameProperty() {
	return persistenceUnit.nameProperty();
    }

    @Override
    public String getName() {
	return persistenceUnit.getName();
    }

    @Override
    public ObservableSet<? extends PrimitiveAttribute> getUnmodifiablePrimitiveAttributes() {
	return persistenceUnit.getUnmodifiablePrimitiveAttributes();
    }

    @Override
    public ObservableSet<? extends ReferentialAttribute> getUnmodifiableReferentialAttributes() {
	return persistenceUnit.getUnmodifiableReferentialAttributes();
    }

    @Override
    public ObservableSet<? extends RepositoryOperation> getUnmodifiableRepositoryOperations() {
	return persistenceUnit.getUnmodifiableRepositoryOperations();
    }

    @Override
    public PersistenceDescriptor getPersistence() {
	return parentDescriptor;
    }

//    @AllArgsConstructor(access = AccessLevel.PRIVATE)
//    private static class Memento {
//	private Map<String, AssociationBelongType> associationIdToAssociationBelongType;
//	
//	public static Memento of(PersistenceUnitDescriptor descriptor) {
//	    descriptor.getUnmodifiableReferentialAttributes().stream()
//	    .collect(Collectors.toM)
//	}
//	
//	private enum AssociationBelongType {
//	    CONTAINER_ONLY, ELEMENT_ONLY, BOTH;
//	}
//    }
}
