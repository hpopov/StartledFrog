package net.atlassian.cmathtutor.domain.persistence;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;

public interface ReferentialAttribute extends Attribute {

    ReadOnlyObjectProperty<OwnerType> ownerTypeProperty();

    OwnerType getOwnerType();

    ReadOnlyBooleanProperty navigableProperty();

    boolean isNavigable();

    ReadOnlyObjectProperty<AttributeArity> arityProperty();

    AttributeArity getArity();

}