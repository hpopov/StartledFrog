package net.atlassian.cmathtutor.domain.persistence;

public interface ReferentialAttribute extends Attribute {

    OwnerType getOwnerType();

    boolean isNavigable();

    AttributeArity getArity();

    Association getAssociation();
}