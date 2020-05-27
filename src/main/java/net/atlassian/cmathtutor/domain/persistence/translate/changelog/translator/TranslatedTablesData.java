package net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import lombok.NonNull;
import net.atlassian.cmathtutor.domain.persistence.Association;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;
import net.atlassian.cmathtutor.domain.persistence.translate.TranslatorHelper;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.Column;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.CreateTable;

public class TranslatedTablesData {

    private Map<String, EntityTableData> persistenceUnitNameToEntityTables;
    private Map<Association, JoinTableData> associationToJoinTables;

    public TranslatedTablesData() {
	persistenceUnitNameToEntityTables = new HashMap<>();
    }

    public EntityTableData getByPersistenceUnitName(@NonNull String persistenceUnitName) {
	return persistenceUnitNameToEntityTables.get(persistenceUnitName);
    }

    public CreateTable getByAssociation(@NonNull Association association) {
	if (associationToJoinTables.containsKey(association)) {
	    return associationToJoinTables.get(association).getCreateTable();
	}
	return null;
    }

    public Column getJoinColumnByReferentialAttribute(@NonNull ReferentialAttribute referentialAttribute) {
	Association association = referentialAttribute.getAssociation();
	if (associationToJoinTables.containsKey(association)) {
	    Pair<ReferentialAttribute, ReferentialAttribute> primaryAndSecondaryAttributes = TranslatorHelper
		    .definePrimaryAndSecondaryAttributes(association);
	    if (referentialAttribute.equals(primaryAndSecondaryAttributes.getKey())) {

		return associationToJoinTables.get(association).getPrimaryAttributeJoinColumn();
	    } else {

		return associationToJoinTables.get(association).getSecondaryAttributeJoinColumn();
	    }
	}
	String persistenceUnitName = referentialAttribute.getParentClassifier().getName();
	Column parentTableColumn = getColumnByReferentialAttributeAndPersistenceUnitName(referentialAttribute,
		persistenceUnitName);
	return parentTableColumn;
//	if (parentTableColumn != null) {
//
//	    return parentTableColumn;
//	}
//	ReferentialAttribute anotherAttr = TranslatorHelper.getAnotherAttributeFromAssociation(referentialAttribute);
//	String anotherPuName = anotherAttr.getParentClassifier().getName();
//
//	return getColumnByReferentialAttributeAndPersistenceUnitName(anotherAttr, anotherPuName);
    }

    private Column getColumnByReferentialAttributeAndPersistenceUnitName(ReferentialAttribute referentialAttribute,
	    String persistenceUnitName) {
	if (persistenceUnitNameToEntityTables.containsKey(persistenceUnitName)) {
	    EntityTableData refAttrEntityTableData = persistenceUnitNameToEntityTables.get(persistenceUnitName);
	    Column refAttrColumn = refAttrEntityTableData.getJoinColumnByReferentialAttribute(referentialAttribute);

	    return refAttrColumn;
	}

	return null;
    }

    public void addPersistenceUnitEntityTable(@NonNull String persistenceUnitName,
	    @NonNull EntityTableData entityTable) {
	persistenceUnitNameToEntityTables.put(persistenceUnitName, entityTable);
    }

    public void addAssociationJoinTable(@NonNull Association association, @NonNull JoinTableData joinTable) {
	associationToJoinTables.put(association, joinTable);
    }

    public Stream<CreateTable> getTranslatedCreateTables() {
	return Stream.concat(persistenceUnitNameToEntityTables.values().stream().map(EntityTableData::getCreateTable),
		associationToJoinTables.values().stream().map(JoinTableData::getCreateTable)).distinct();
    }
}
