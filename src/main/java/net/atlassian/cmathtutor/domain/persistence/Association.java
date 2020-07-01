package net.atlassian.cmathtutor.domain.persistence;

import javafx.beans.property.ReadOnlyObjectProperty;

public interface Association extends Identifiable {

    ReadOnlyObjectProperty<AggregationKind> aggregationKindProperty();

    AggregationKind getAggregationKind();

    ReferentialAttribute getContainerAttribute();

    ReferentialAttribute getElementAttribute();

    Persistence getPersistence();
}