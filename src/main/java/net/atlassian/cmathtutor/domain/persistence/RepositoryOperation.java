package net.atlassian.cmathtutor.domain.persistence;

public interface RepositoryOperation extends Identifiable {

    String getName();

    PersistenceUnit getParentClassifier();
}