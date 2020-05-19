package net.atlassian.cmathtutor.domain.persistence;

import javafx.collections.ObservableSet;

public interface Persistence extends Identifiable {

    ObservableSet<? extends PersistenceUnit> getPersistenceUnits();

    ObservableSet<? extends Association> getAssociations();

}