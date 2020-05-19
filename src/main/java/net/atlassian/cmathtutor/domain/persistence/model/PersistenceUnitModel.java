package net.atlassian.cmathtutor.domain.persistence.model;

import java.util.LinkedHashSet;

import javax.xml.bind.annotation.XmlTransient;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.SetChangeListener.Change;
import lombok.EqualsAndHashCode;
import net.atlassian.cmathtutor.domain.persistence.Persistence;
import net.atlassian.cmathtutor.domain.persistence.PersistenceUnit;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class PersistenceUnitModel extends AbstractIdentifyableModel implements PersistenceUnit {

    @EqualsAndHashCode.Include
    private StringProperty name = new SimpleStringProperty();
    private ObservableSet<PrimitiveAttributeModel> primitiveAttributes = FXCollections
	    .observableSet(new LinkedHashSet<>());
    private ObservableSet<ReferentialAttributeModel> referentialAttributes = FXCollections
	    .observableSet(new LinkedHashSet<>());
    private ObservableSet<RepositoryOperationModel> repositoryOperations = FXCollections
	    .observableSet(new LinkedHashSet<>());

    private ObjectProperty<PersistenceModel> persistence = new SimpleObjectProperty<>();

    public PersistenceUnitModel() {
	super();
	initBindings();
    }

    public PersistenceUnitModel(String id) {
	super(id);
	initBindings();
    }

    private void initBindings() {
	SetChangeListener<AbstractAttributeModel> attributesChangeListener = (
		Change<? extends AbstractAttributeModel> change) -> {
	    if (change.wasRemoved()) {
		change.getElementRemoved().setParentClassifier(null);
	    }
	    if (change.wasAdded()) {
		change.getElementAdded().setParentClassifier(this);
	    }
	};
	referentialAttributes.addListener(attributesChangeListener);
	primitiveAttributes.addListener(attributesChangeListener);
	repositoryOperations.addListener((Change<? extends RepositoryOperationModel> change) -> {
	    if (change.wasRemoved()) {
		change.getElementRemoved().setParentClassifier(null);
	    }
	    if (change.wasAdded()) {
		change.getElementAdded().setParentClassifier(this);
	    }
	});
    }

    @Override
    public StringProperty nameProperty() {
	return this.name;
    }

    @Override
    public String getName() {
	return this.nameProperty().get();
    }

    public void setName(final String name) {
	this.nameProperty().set(name);
    }

    @Override
    public ObservableSet<PrimitiveAttributeModel> getPrimitiveAttributes() {
	return primitiveAttributes;
    }

    @Override
    public ObservableSet<ReferentialAttributeModel> getReferentialAttributes() {
	return referentialAttributes;
    }

    @Override
    public ObservableSet<RepositoryOperationModel> getRepositoryOperations() {
	return repositoryOperations;
    }

    public ReadOnlyObjectProperty<PersistenceModel> persistenceProperty() {
	return this.persistence;
    }

    @XmlTransient
    public Persistence getPersistence() {
	return this.persistenceProperty().get();
    }

    public void setPersistence(PersistenceModel persistence) {
	this.persistence.set(persistence);
    }

}