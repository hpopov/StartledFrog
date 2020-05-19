package net.atlassian.cmathtutor.domain.persistence;

import javafx.beans.property.ReadOnlyStringProperty;

public interface Attribute extends Identifiable {

    ReadOnlyStringProperty nameProperty();

    String getName();

}