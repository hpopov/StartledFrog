package net.atlassian.cmathtutor.domain.persistence.model;

import java.util.EnumSet;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.atlassian.cmathtutor.domain.persistence.ConstraintType;
import net.atlassian.cmathtutor.domain.persistence.PrimitiveAttribute;
import net.atlassian.cmathtutor.domain.persistence.PrimitiveType;

@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class PrimitiveAttributeModel extends AbstractAttributeModel implements PrimitiveAttribute {

    private ObjectProperty<PrimitiveType> type = new SimpleObjectProperty<>();
    private ObservableSet<ConstraintType> constraints = FXCollections
	    .observableSet(EnumSet.noneOf(ConstraintType.class));

    public PrimitiveAttributeModel(String id) {
	super(id);
    }

    @Override
    public ObjectProperty<PrimitiveType> typeProperty() {
	return this.type;
    }

    @Override
    public PrimitiveType getType() {
	return this.typeProperty().get();
    }

    public void setType(final PrimitiveType type) {
	this.typeProperty().set(type);
    }

    @Override
    public ObservableSet<ConstraintType> getConstraints() {
	return this.constraints;
    }
}
