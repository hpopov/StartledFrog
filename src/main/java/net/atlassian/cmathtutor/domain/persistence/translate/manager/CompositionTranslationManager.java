package net.atlassian.cmathtutor.domain.persistence.translate.manager;

import javax.persistence.CascadeType;

import net.atlassian.cmathtutor.domain.persistence.Association;
import net.atlassian.cmathtutor.domain.persistence.AttributeArity;

public class CompositionTranslationManager extends AbstractAssociationTranslationManager
	implements AssociationTranslationManager {

    private static final CascadeType[] ALL_CASCADE_TYPES = new CascadeType[] { CascadeType.ALL };
    private static final CascadeType[] CASCADE_TYPES_WITHOUT_REMOVE = new CascadeType[] { CascadeType.DETACH,
	    CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH };

    public CompositionTranslationManager(Association association) {
	super(association);
    }

    @Override
    public boolean isAssociationUnsupported() {
	return secondaryAttribute.getArity().equals(AttributeArity.ONE_EXACTLY)
		|| association.getElementAttribute().getArity().isMany();
    }

    @Override
    public CascadeType[] getCascadesForPrimaryReference() {
	return primaryAttribute == association.getContainerAttribute()
		? ALL_CASCADE_TYPES
		: CASCADE_TYPES_WITHOUT_REMOVE;
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
