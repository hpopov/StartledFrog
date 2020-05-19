package net.atlassian.cmathtutor.domain.persistence.model;

import javax.xml.bind.annotation.XmlTransient;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.atlassian.cmathtutor.domain.persistence.Association;
import net.atlassian.cmathtutor.domain.persistence.AttributeArity;
import net.atlassian.cmathtutor.domain.persistence.OwnerType;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;

@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class ReferentialAttributeModel extends AbstractAttributeModel implements ReferentialAttribute {

    private ObjectProperty<AssociationModel> association = new SimpleObjectProperty<>();
    private ObjectProperty<OwnerType> ownerType = new SimpleObjectProperty<>();
    private BooleanProperty navigable = new SimpleBooleanProperty();
    private ObjectProperty<AttributeArity> arity = new SimpleObjectProperty<>();

    public ReferentialAttributeModel(String id) {
	super(id);
    }

    public ReadOnlyObjectProperty<AssociationModel> associationProperty() {
	return this.association;
    }

    @XmlTransient
    public Association getAssociation() {
	return this.associationProperty().get();
    }

    public void setAssociation(final AssociationModel association) {
	this.association.set(association);
    }

    @Override
    public ObjectProperty<OwnerType> ownerTypeProperty() {
	return this.ownerType;
    }

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

    @Override
    public AttributeArity getArity() {
	return this.arityProperty().get();
    }

    public void setArity(final AttributeArity arity) {
	this.arityProperty().set(arity);
    }

}
