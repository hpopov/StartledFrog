package net.atlassian.cmathtutor.domain.persistence.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.atlassian.cmathtutor.domain.persistence.AttributeArity;
import net.atlassian.cmathtutor.domain.persistence.OwnerType;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;
import net.atlassian.cmathtutor.util.UidUtil;

@XmlAccessorType(XmlAccessType.NONE)
@ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class ReferentialAttributeModel extends AbstractAttributeModel implements ReferentialAttribute {

    private ObjectProperty<OwnerType> ownerType = new SimpleObjectProperty<>();
    private BooleanProperty navigable = new SimpleBooleanProperty();
    private ObjectProperty<AttributeArity> arity = new SimpleObjectProperty<>();

    @ToString.Exclude
    private ObjectProperty<AssociationModel> association = new SimpleObjectProperty<>();

    public ReferentialAttributeModel(String id) {
	super(id);
    }

    public static ReferentialAttributeModel makeIdentifiableInstance(String name,
	    PersistenceUnitModel parentClassifier) {
	ReferentialAttributeModel referentialAttributeModel = new ReferentialAttributeModel(UidUtil.getUId());
	referentialAttributeModel.setName(name);
	referentialAttributeModel.setParentClassifier(parentClassifier);
	return referentialAttributeModel;
    }

    public ReadOnlyObjectProperty<AssociationModel> associationProperty() {
	return this.association;
    }

    @XmlTransient
    public AssociationModel getAssociation() {
	return this.associationProperty().get();
    }

    public void setAssociation(final AssociationModel association) {
	this.association.set(association);
    }

    @Override
    public ObjectProperty<OwnerType> ownerTypeProperty() {
	return this.ownerType;
    }

    @XmlElement(required = true)
    @Override
    public OwnerType getOwnerType() {
	return this.ownerTypeProperty().get();
    }

    public void setOwnerType(final OwnerType ownerType) {
	this.ownerTypeProperty().set(ownerType);
    }

    @Override
    public BooleanProperty navigableProperty() {
	return this.navigable;
    }

    @XmlElement(required = true)
    @Override
    public boolean isNavigable() {
	return this.navigableProperty().get();
    }

    public void setNavigable(final boolean navigable) {
	this.navigableProperty().set(navigable);
    }

    @Override
    public ObjectProperty<AttributeArity> arityProperty() {
	return this.arity;
    }

    @XmlAttribute(required = true)
    @Override
    public AttributeArity getArity() {
	return this.arityProperty().get();
    }

    public void setArity(final AttributeArity arity) {
	this.arityProperty().set(arity);
    }

}
