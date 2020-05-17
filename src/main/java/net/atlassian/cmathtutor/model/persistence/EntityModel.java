package net.atlassian.cmathtutor.model.persistence;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

//@XmlType(name = )
public class EntityModel {

    private StringProperty name;
    private ListProperty<PrimitiveAttributeModel> primitiveAttributes;
    private ListProperty<ReferentialAttributeModel> referentialAttributes;

    public EntityModel() {
	name = new SimpleStringProperty();
	primitiveAttributes = new SimpleListProperty<PrimitiveAttributeModel>();
	referentialAttributes = new SimpleListProperty<ReferentialAttributeModel>();
    }

    public StringProperty nameProperty() {
	return this.name;
    }

    public String getName() {
	return this.nameProperty().get();
    }

    public void setName(final String name) {
	this.nameProperty().set(name);
    }

    public ListProperty<PrimitiveAttributeModel> primitiveAttributesProperty() {
	return this.primitiveAttributes;
    }

    public ObservableList<PrimitiveAttributeModel> getPrimitiveAttributes() {
	return this.primitiveAttributesProperty().get();
    }

    public void setPrimitiveAttributes(final ObservableList<PrimitiveAttributeModel> primitiveAttributes) {
	this.primitiveAttributesProperty().set(primitiveAttributes);
    }

    public ListProperty<ReferentialAttributeModel> referentialAttributesProperty() {
	return this.referentialAttributes;
    }

    public ObservableList<ReferentialAttributeModel> getReferentialAttributes() {
	return this.referentialAttributesProperty().get();
    }

    public void setReferentialAttributes(final ObservableList<ReferentialAttributeModel> referentialAttributes) {
	this.referentialAttributesProperty().set(referentialAttributes);
    }
}
