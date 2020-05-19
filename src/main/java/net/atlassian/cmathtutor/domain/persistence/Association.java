package net.atlassian.cmathtutor.domain.persistence;

import javafx.beans.property.ReadOnlyObjectProperty;

public interface Association extends Identifiable {

    ReadOnlyObjectProperty<AggregationKind> aggregationKindProperty();

    AggregationKind getAggregationKind();

    ReadOnlyObjectProperty<? extends ReferentialAttribute> containerAttributeProperty();

    ReferentialAttribute getContainerAttribute();

    ReadOnlyObjectProperty<? extends ReferentialAttribute> elementAttributeProperty();

    ReferentialAttribute getElementAttribute();

}