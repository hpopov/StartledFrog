package net.atlassian.cmathtutor.domain.persistence;

import javafx.beans.property.ReadOnlyObjectProperty;

public interface ReferentialAttribute extends Attribute {

    OwnerType getOwnerType();

    boolean isNavigable();

    ReadOnlyObjectProperty<AttributeArity> arityProperty();

    AttributeArity getArity();

    Association getAssociation();
}