package net.atlassian.cmathtutor.domain.persistence;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;

public interface Attribute extends Identifiable {

    String getName();

    ReadOnlyStringProperty nameProperty();

    PersistenceUnit getParentClassifier();

    ReadOnlyObjectProperty<? extends PersistenceUnit> parentClassifierProperty();
}