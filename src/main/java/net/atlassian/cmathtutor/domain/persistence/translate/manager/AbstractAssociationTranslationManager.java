package net.atlassian.cmathtutor.domain.persistence.translate.manager;

import org.apache.commons.lang3.tuple.Pair;

import lombok.Getter;
import net.atlassian.cmathtutor.domain.persistence.Association;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;
import net.atlassian.cmathtutor.domain.persistence.translate.TranslatorHelper;

@Getter(onMethod = @__(@Override))
public abstract class AbstractAssociationTranslationManager implements AssociationTranslationManager {

    protected Association association;
    protected ReferentialAttribute primaryAttribute;
    protected ReferentialAttribute secondaryAttribute;

    public AbstractAssociationTranslationManager(Association association) {
        Pair<ReferentialAttribute, ReferentialAttribute> primaryAndSecondaryAttributes = TranslatorHelper
                .definePrimaryAndSecondaryAttributes(association);
        this.primaryAttribute = primaryAndSecondaryAttributes.getKey();
        this.secondaryAttribute = primaryAndSecondaryAttributes.getValue();
        this.association = association;
    }
}
