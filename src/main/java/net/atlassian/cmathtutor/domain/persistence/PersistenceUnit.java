package net.atlassian.cmathtutor.domain.persistence;

import java.util.Set;

public interface PersistenceUnit extends Identifiable {

    String getName();

    Set<? extends PrimitiveAttribute> getUnmodifiablePrimitiveAttributes();

    Set<? extends ReferentialAttribute> getUnmodifiableReferentialAttributes();

    Set<? extends RepositoryOperation> getUnmodifiableRepositoryOperations();

    Persistence getPersistence();
}