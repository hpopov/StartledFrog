package net.atlassian.cmathtutor.domain.persistence.model;

import javax.xml.bind.annotation.XmlTransient;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.EqualsAndHashCode;
import net.atlassian.cmathtutor.domain.persistence.PersistenceUnit;
import net.atlassian.cmathtutor.domain.persistence.RepositoryOperation;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class RepositoryOperationModel extends AbstractIdentifyableModel implements RepositoryOperation {

    @EqualsAndHashCode.Include
    private StringProperty name = new SimpleStringProperty();
    @EqualsAndHashCode.Include
    private ObjectProperty<PersistenceUnitModel> parentClassifier = new SimpleObjectProperty<>();

    public RepositoryOperationModel() {
	name = new SimpleStringProperty();
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

    public ReadOnlyObjectProperty<PersistenceUnitModel> parentClassifierProperty() {
	return this.parentClassifier;
    }

    @XmlTransient
    public PersistenceUnit getParentClassifier() {
	return this.parentClassifierProperty().get();
    }

    public void setParentClassifier(final PersistenceUnitModel parentClassifier) {
	this.parentClassifier.set(parentClassifier);
    }

}
