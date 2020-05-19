package net.atlassian.cmathtutor.domain.persistence.descriptor;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import lombok.AccessLevel;
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

    private PersistenceUnitModel persistenceUnit;
    private PersistenceDescriptor parentDescriptor;
    private ObservableSet<PrimitiveAttributeModel> unmodifiablePrimitiveAttributes;
    private ObservableSet<ReferentialAttributeModel> unmodifiableReferentialAttributes;
    private ObservableSet<RepositoryOperationModel> unmodifiableRepositoryOperations;

    private PersistenceUnitDescriptor(PersistenceUnitModel persistenceUnit,
	    PersistenceDescriptor parentDescriptor) {
	super(persistenceUnit.getId());
	this.persistenceUnit = persistenceUnit;
	this.parentDescriptor = parentDescriptor;
	unmodifiablePrimitiveAttributes = FXCollections
		.unmodifiableObservableSet(persistenceUnit.getPrimitiveAttributes());
	unmodifiableReferentialAttributes = FXCollections
		.unmodifiableObservableSet(persistenceUnit.getReferentialAttributes());
	unmodifiableRepositoryOperations = FXCollections
		.unmodifiableObservableSet(persistenceUnit.getRepositoryOperations());
    }

    public static PersistenceUnitDescriptor wrap(@NonNull PersistenceUnitModel persistenceUnit,
	    @NonNull PersistenceDescriptor parentDescriptor) {
	if (persistenceUnit.getId() == null) {
	    throw new IllegalArgumentException("Id of the persistence unit to wrap must not be null");
	}
	if (!parentDescriptor.getPersistenceUnits().contains(persistenceUnit)) {
	    throw new IllegalArgumentException("ParentDescriptor must contain persistenceUnit to wrap");
	}
	return new PersistenceUnitDescriptor(persistenceUnit, parentDescriptor);
    }

//    public AssociationDescriptor addNewAssociationTo(PersistenceUnitDescriptor elementDescriptor) {
//	
//    }

    @Override
    public ReadOnlyStringProperty nameProperty() {
	return persistenceUnit.nameProperty();
    }

    @Override
    public String getName() {
	return persistenceUnit.getName();
    }

    @Override
    public ObservableSet<? extends PrimitiveAttribute> getPrimitiveAttributes() {
	return unmodifiablePrimitiveAttributes;
    }

    @Override
    public ObservableSet<? extends ReferentialAttribute> getReferentialAttributes() {
	return unmodifiableReferentialAttributes;
    }

    @Override
    public ObservableSet<? extends RepositoryOperation> getRepositoryOperations() {
	return unmodifiableRepositoryOperations;
    }

}
