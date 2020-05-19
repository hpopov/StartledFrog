package net.atlassian.cmathtutor.domain.persistence;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ObservableSet;

public interface PersistenceUnit extends Identifiable {

    ReadOnlyStringProperty nameProperty();

    String getName();

    ObservableSet<? extends PrimitiveAttribute> getPrimitiveAttributes();

    ObservableSet<? extends ReferentialAttribute> getReferentialAttributes();

    ObservableSet<? extends RepositoryOperation> getRepositoryOperations();

}