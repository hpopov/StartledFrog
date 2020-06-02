package net.atlassian.cmathtutor.domain.persistence;

import java.util.Set;

public interface Persistence extends Identifiable {

    Set<? extends PersistenceUnit> getUnmodifiablePersistenceUnits();

    Set<? extends Association> getUnmodifiableAssociations();
}