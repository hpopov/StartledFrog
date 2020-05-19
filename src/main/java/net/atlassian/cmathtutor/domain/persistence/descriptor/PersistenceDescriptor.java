package net.atlassian.cmathtutor.domain.persistence.descriptor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.atlassian.cmathtutor.domain.persistence.Association;
import net.atlassian.cmathtutor.domain.persistence.Persistence;
import net.atlassian.cmathtutor.domain.persistence.PersistenceUnit;
import net.atlassian.cmathtutor.domain.persistence.model.AssociationModel;
import net.atlassian.cmathtutor.domain.persistence.model.PersistenceModel;
import net.atlassian.cmathtutor.domain.persistence.model.PersistenceUnitModel;
import net.atlassian.cmathtutor.util.UidUtil;
//import com.google.common.graph.Graph;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PersistenceDescriptor extends AbstractDescriptor implements Persistence {

    private PersistenceModel persistence;
    private ObservableSet<PersistenceUnitModel> unmodifiablePersistenceUnits;
    private ObservableSet<AssociationModel> unmodifiableAssociations;
//    private com.google.common.

    private PersistenceDescriptor(String id) {
	super(id);
	persistence = new PersistenceModel(id);
	unmodifiablePersistenceUnits = FXCollections.unmodifiableObservableSet(persistence.getPersistenceUnits());
	unmodifiableAssociations = FXCollections.unmodifiableObservableSet(persistence.getAssociations());
    }

    public static PersistenceDescriptor newInstance() {
	return new PersistenceDescriptor(UidUtil.getUId());
    }

    public PersistenceUnitDescriptor addNewPersistenceUnit(@NonNull String name) throws IllegalOperationException {
	PersistenceUnitModel persistenceUnit = new PersistenceUnitModel(UidUtil.getUId());
	persistenceUnit.setName(name);
	if (persistence.getPersistenceUnits().contains(persistenceUnit)) {
	    throw new IllegalOperationException("Equal persistence unit already exists");
	}
	persistence.getPersistenceUnits().add(persistenceUnit);
	return PersistenceUnitDescriptor.wrap(persistenceUnit, this);
    }

    public AssociationDescriptor addNewAssociation(@NonNull AssociationModel association)
	    throws IllegalOperationException {
	AssociationDescriptor associationDescriptor = AssociationDescriptor.wrap(association);
	if (persistence.getAssociations().contains(association)) {
	    throw new IllegalOperationException("Equal association already exists");
	}

	return associationDescriptor;
    }

    @Override
    public ObservableSet<? extends PersistenceUnit> getPersistenceUnits() {
	return unmodifiablePersistenceUnits;
    }

    @Override
    public ObservableSet<? extends Association> getAssociations() {
	return unmodifiableAssociations;
    }

}
