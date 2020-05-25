package net.atlassian.cmathtutor.domain.persistence;

public interface Association extends Identifiable {

    AggregationKind getAggregationKind();

    ReferentialAttribute getContainerAttribute();

    ReferentialAttribute getElementAttribute();

    Persistence getPersistence();
}