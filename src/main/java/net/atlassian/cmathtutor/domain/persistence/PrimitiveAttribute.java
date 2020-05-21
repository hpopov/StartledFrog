package net.atlassian.cmathtutor.domain.persistence;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableSet;

public interface PrimitiveAttribute extends Identifiable {

    ReadOnlyObjectProperty<PrimitiveType> typeProperty();

    PrimitiveType getType();

    ObservableSet<ConstraintType> getUnmodifiableConstraints();

}