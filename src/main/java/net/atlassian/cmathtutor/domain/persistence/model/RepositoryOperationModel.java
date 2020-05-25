package net.atlassian.cmathtutor.domain.persistence.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.atlassian.cmathtutor.domain.persistence.PersistenceUnit;
import net.atlassian.cmathtutor.domain.persistence.RepositoryOperation;
import net.atlassian.cmathtutor.util.UidUtil;

@NoArgsConstructor
@XmlAccessorType(XmlAccessType.NONE)
@ToString(callSuper = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class RepositoryOperationModel extends AbstractIdentifyableModel implements RepositoryOperation {

    @EqualsAndHashCode.Include
    private StringProperty name = new SimpleStringProperty();
    @ToString.Exclude
    @EqualsAndHashCode.Include
    private ObjectProperty<PersistenceUnitModel> parentClassifier = new SimpleObjectProperty<>();

    public static RepositoryOperationModel makeIdentifiableInstance(String name,
	    PersistenceUnitModel parentClassifier) {
	RepositoryOperationModel repositoryOperationModel = new RepositoryOperationModel(UidUtil.getUId());
	repositoryOperationModel.setName(name);
	repositoryOperationModel.setParentClassifier(parentClassifier);
	return repositoryOperationModel;
    }

    public RepositoryOperationModel(String id) {
	super(id);
    }

    @Override
    public StringProperty nameProperty() {
	return this.name;
    }

    @XmlElement(required = true)
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
