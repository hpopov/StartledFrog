package net.atlassian.cmathtutor.domain.persistence;

import javafx.beans.property.ReadOnlyStringProperty;

public interface RepositoryOperation extends Identifiable {

    ReadOnlyStringProperty nameProperty();

    String getName();

}