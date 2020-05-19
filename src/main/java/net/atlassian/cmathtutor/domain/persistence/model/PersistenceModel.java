package net.atlassian.cmathtutor.domain.persistence.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener.Change;
import net.atlassian.cmathtutor.domain.persistence.Persistence;

public class PersistenceModel extends AbstractIdentifyableModel implements Persistence {

    private ObservableSet<PersistenceUnitModel> persistenceUnits = FXCollections.observableSet();
    private ObservableSet<AssociationModel> associations = FXCollections.observableSet();

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

    @Override
    public ObservableSet<PersistenceUnitModel> getPersistenceUnits() {
	return persistenceUnits;
    }

    @Override
    public ObservableSet<AssociationModel> getAssociations() {
	return associations;
    }

}
