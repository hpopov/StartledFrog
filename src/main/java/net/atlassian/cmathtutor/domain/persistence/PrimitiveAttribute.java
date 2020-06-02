package net.atlassian.cmathtutor.domain.persistence;

import java.util.Set;

public interface PrimitiveAttribute extends Attribute {

    PrimitiveType getType();

    Set<ConstraintType> getUnmodifiableConstraints();
}