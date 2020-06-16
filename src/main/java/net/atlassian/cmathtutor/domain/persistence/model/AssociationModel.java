package net.atlassian.cmathtutor.domain.persistence.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.atlassian.cmathtutor.domain.persistence.AggregationKind;
import net.atlassian.cmathtutor.domain.persistence.Association;

@XmlAccessorType(XmlAccessType.NONE)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class AssociationModel extends AbstractIdentifyableModel implements Association {

    private ObjectProperty<AggregationKind> aggregationKind = new SimpleObjectProperty<>();
    @EqualsAndHashCode.Include
    private ObjectProperty<ReferentialAttributeModel> containerAttribute = new SimpleObjectProperty<>();
    @EqualsAndHashCode.Include
    private ObjectProperty<ReferentialAttributeModel> elementAttribute = new SimpleObjectProperty<>();

    @ToString.Exclude
    private ObjectProperty<PersistenceModel> persistence = new SimpleObjectProperty<>();

    public AssociationModel(String id) {
	super(id);
//	initBindings();
    }

    public AssociationModel() {
	super();
//	initBindings();
    }

    @SuppressWarnings("unused")
    private void initBindings() {
	ChangeListener<? super ReferentialAttributeModel> attributeChangeListener = (observable, oldValue,
		newValue) -> {
	    if (oldValue != null) {
		oldValue.setAssociation(null);
	    }
	    if (newValue != null) {
		newValue.setAssociation(this);
	    }
	};
	containerAttributeProperty().addListener(attributeChangeListener);
	elementAttributeProperty().addListener(attributeChangeListener);
    }

    @Override
    public ObjectProperty<AggregationKind> aggregationKindProperty() {
	return this.aggregationKind;
    }

    @XmlElement(required = true)
    @Override
    public AggregationKind getAggregationKind() {
	return this.aggregationKindProperty().get();
    }

    public void setAggregationKind(final AggregationKind aggregationKind) {
	this.aggregationKindProperty().set(aggregationKind);
    }

    public ObjectProperty<ReferentialAttributeModel> containerAttributeProperty() {
	return this.containerAttribute;
    }

    @XmlElement(required = true)
    @XmlIDREF
    @Override
    public ReferentialAttributeModel getContainerAttribute() {
	return this.containerAttributeProperty().get();
    }

    public void setContainerAttribute(final ReferentialAttributeModel containerAttribute) {
	this.containerAttributeProperty().set(containerAttribute);
    }

    public ObjectProperty<ReferentialAttributeModel> elementAttributeProperty() {
	return this.elementAttribute;
    }

    @XmlElement(required = true)
    @XmlIDREF
    @Override
    public ReferentialAttributeModel getElementAttribute() {
	return this.elementAttributeProperty().get();
    }

    public void setElementAttribute(final ReferentialAttributeModel elementAttribute) {
	this.elementAttributeProperty().set(elementAttribute);
    }

    public ReadOnlyObjectProperty<PersistenceModel> persistenceProperty() {
	return this.persistence;
    }

    @XmlTransient
    @Override
    public PersistenceModel getPersistence() {
	return this.persistenceProperty().get();
    }

    public void setPersistence(final PersistenceModel persistence) {
	this.persistence.set(persistence);
    }

}
