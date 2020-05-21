package net.atlassian.cmathtutor.domain.persistence.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener.Change;
import lombok.ToString;
import net.atlassian.cmathtutor.domain.persistence.Association;
import net.atlassian.cmathtutor.domain.persistence.Persistence;
import net.atlassian.cmathtutor.domain.persistence.PersistenceUnit;

@ToString(callSuper = true)
public class PersistenceModel extends AbstractIdentifyableModel implements Persistence {

    private ObservableSet<PersistenceUnitModel> persistenceUnits = FXCollections.observableSet();
    private ObservableSet<AssociationModel> associations = FXCollections.observableSet();

    private ObservableSet<? extends PersistenceUnit> unmodifiablePersistenceUnits = FXCollections
	    .unmodifiableObservableSet(persistenceUnits);
    private ObservableSet<? extends Association> unmodifiableAssociations = FXCollections
	    .unmodifiableObservableSet(associations);

    public PersistenceModel() {
	super();
	initBindings();
    }

    public PersistenceModel(String id) {
	super(id);
	initBindings();
    }

    private void initBindings() {
	persistenceUnits.addListener((Change<? extends PersistenceUnitModel> change) -> {
	    if (change.wasRemoved()) {
		change.getElementRemoved().setPersistence(null);
	    }
	    if (change.wasAdded()) {
		change.getElementAdded().setPersistence(this);
	    }
	});
	associations.addListener((Change<? extends AssociationModel> change) -> {
	    if (change.wasRemoved()) {
		change.getElementRemoved().setPersistence(null);
	    }
	    if (change.wasAdded()) {
		change.getElementAdded().setPersistence(this);
	    }
	});
    }

    public ObservableSet<PersistenceUnitModel> getPersistenceUnits() {
	return persistenceUnits;
    }

    public ObservableSet<AssociationModel> getAssociations() {
	return associations;
    }

    @Override
    public ObservableSet<? extends PersistenceUnit> getUnmodifiablePersistenceUnits() {
	return unmodifiablePersistenceUnits;
    }

    @Override
    public ObservableSet<? extends Association> getUnmodifiableAssociations() {
	return unmodifiableAssociations;
    }

}
