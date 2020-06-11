package net.atlassian.cmathtutor.domain.persistence.descriptor;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ObservableSet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.atlassian.cmathtutor.domain.persistence.PersistenceUnit;
import net.atlassian.cmathtutor.domain.persistence.PrimitiveAttribute;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;
import net.atlassian.cmathtutor.domain.persistence.RepositoryOperation;
import net.atlassian.cmathtutor.domain.persistence.model.PersistenceUnitModel;
import net.atlassian.cmathtutor.domain.persistence.model.PrimitiveAttributeModel;
import net.atlassian.cmathtutor.domain.persistence.model.ReferentialAttributeModel;
import net.atlassian.cmathtutor.domain.persistence.model.RepositoryOperationModel;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PersistenceUnitDescriptor extends AbstractDescriptor implements PersistenceUnit {

    @Getter
    private PersistenceUnitModel persistenceUnit;
    private PersistenceDescriptor parentDescriptor;

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

    public void initializeNewReferentialAttribute(@NonNull ReferentialAttributeModel referentialAttribute)
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
}
