package net.atlassian.cmathtutor.domain.persistence.model;

import java.util.EnumSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.atlassian.cmathtutor.domain.persistence.ConstraintType;
import net.atlassian.cmathtutor.domain.persistence.PrimitiveAttribute;
import net.atlassian.cmathtutor.domain.persistence.PrimitiveType;

@XmlAccessorType(XmlAccessType.NONE)
@ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class PrimitiveAttributeModel extends AbstractAttributeModel implements PrimitiveAttribute {

    private ObjectProperty<PrimitiveType> type = new SimpleObjectProperty<>();
    private ObservableSet<ConstraintType> constraints = FXCollections
	    .observableSet(EnumSet.noneOf(ConstraintType.class));

    private ObservableSet<ConstraintType> unmodifiableConstraints = FXCollections
	    .unmodifiableObservableSet(constraints);

    public static PrimitiveAttributeModel makeIdentifiableInstance(String name, PersistenceUnitModel parentClassifier) {
	PrimitiveAttributeModel primitiveAttributeModel = new PrimitiveAttributeModel();
	primitiveAttributeModel.setName(name);
	primitiveAttributeModel.setParentClassifier(parentClassifier);
	return primitiveAttributeModel;
    }

    public PrimitiveAttributeModel(String id) {
	super(id);
    }

    public ObjectProperty<PrimitiveType> typeProperty() {
	return this.type;
    }

    @XmlElement(required = true)
    @Override
    public PrimitiveType getType() {
	return this.typeProperty().get();
    }

    public void setType(final PrimitiveType type) {
	this.typeProperty().set(type);
    }

    @XmlElement(required = false, name = "constraint")
    public ObservableSet<ConstraintType> getConstraints() {
	return this.constraints;
    }

    @Override
    public ObservableSet<ConstraintType> getUnmodifiableConstraints() {
	return unmodifiableConstraints;
    }
}
