package net.atlassian.cmathtutor.domain.persistence;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableSet;

public interface PrimitiveAttribute extends Attribute {

    PrimitiveType getType();

    ReadOnlyObjectProperty<PrimitiveType> typeProperty();

    ObservableSet<ConstraintType> getUnmodifiableConstraints();
}