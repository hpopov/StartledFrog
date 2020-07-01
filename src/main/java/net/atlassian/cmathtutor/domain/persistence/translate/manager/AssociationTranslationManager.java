package net.atlassian.cmathtutor.domain.persistence.translate.manager;

import javax.persistence.CascadeType;

import lombok.NonNull;
import net.atlassian.cmathtutor.domain.persistence.Association;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;
import net.atlassian.cmathtutor.domain.persistence.translate.UnimplementedEnumConstantException;

public interface AssociationTranslationManager {

    public static AssociationTranslationManager of(@NonNull Association association) {
        switch (association.getAggregationKind()) {
        case COMPOSITE:
            return new CompositionTranslationManager(association);
        case NONE:
            return new PureAssociationTranslationManager(association);
        case SHARED:
            return new AggregationTranslationManager(association);
        default:
            throw new UnimplementedEnumConstantException(association.getAggregationKind());
        }
    }

    Association getAssociation();

    ReferentialAttribute getPrimaryAttribute();

    ReferentialAttribute getSecondaryAttribute();

    boolean isAssociationUnsupported();

    CascadeType[] getCascadesForPrimaryReference();

    boolean isTableJoin();

    boolean isSecondaryReferenceFieldToBeCreated();

    boolean isAssociationContainable();
}
