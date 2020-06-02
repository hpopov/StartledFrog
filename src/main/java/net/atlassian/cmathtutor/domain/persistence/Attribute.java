package net.atlassian.cmathtutor.domain.persistence;

public interface Attribute extends Identifiable {

    String getName();

    PersistenceUnit getParentClassifier();
}