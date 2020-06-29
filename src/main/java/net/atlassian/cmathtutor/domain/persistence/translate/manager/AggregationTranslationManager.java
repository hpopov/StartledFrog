package net.atlassian.cmathtutor.domain.persistence.translate.manager;

import javax.persistence.CascadeType;

import net.atlassian.cmathtutor.domain.persistence.Association;
import net.atlassian.cmathtutor.domain.persistence.AttributeArity;

public class AggregationTranslationManager extends AbstractAssociationTranslationManager
        implements AssociationTranslationManager {

    private static CascadeType[] CASCADE_TYPES_FOR_PRIMARY_REFERENCE = new CascadeType[] { CascadeType.DETACH,
            CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH };

    public AggregationTranslationManager(Association association) {
        super(association);
    }

    @Override
    public boolean isAssociationUnsupported() {
        return secondaryAttribute.getArity().equals(AttributeArity.ONE_EXACTLY);
    }

    @Override
    public CascadeType[] getCascadesForPrimaryReference() {
        return CASCADE_TYPES_FOR_PRIMARY_REFERENCE;
    }

    @Override
    public boolean isTableJoin() {
        return primaryAttribute.getArity().isMany()
                && (secondaryAttribute.getArity().isMany() || secondaryAttribute == association.getElementAttribute());
    }

    @Override
    public boolean isSecondaryReferenceFieldToBeCreated() {
        return secondaryAttribute.isNavigable() || secondaryAttribute == association.getElementAttribute();
    }

    @Override
    public boolean isAssociationContainable() {
        return true;
    }
}
