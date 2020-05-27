package net.atlassian.cmathtutor.domain.persistence.translate;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import net.atlassian.cmathtutor.domain.persistence.Association;
import net.atlassian.cmathtutor.domain.persistence.OwnerType;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;

public class TranslatorHelper {

    public static ReferentialAttribute getAnotherAttributeFromAssociation(ReferentialAttribute attribute) {
	Association association = attribute.getAssociation();
	return association.getContainerAttribute().equals(attribute)
		? association.getElementAttribute()
		: association.getContainerAttribute();
    }

    public static Pair<ReferentialAttribute, ReferentialAttribute> definePrimaryAndSecondaryAttributes(
	    Association association) {
	if (association.getContainerAttribute().getOwnerType().equals(OwnerType.CLASSIFIER)) {
	    return new ImmutablePair<>(association.getContainerAttribute(), association.getElementAttribute());
	} else if (association.getElementAttribute().getOwnerType().equals(OwnerType.CLASSIFIER)) {
	    return new ImmutablePair<>(association.getElementAttribute(), association.getContainerAttribute());
	} else {
	    throw new IllegalArgumentException(
		    "Association " + association + " must contain one end owned by classifier");
	}
    }
}
