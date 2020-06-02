package net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.NonNull;
import net.atlassian.cmathtutor.domain.persistence.Association;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;
import net.atlassian.cmathtutor.domain.persistence.translate.TranslatorHelper;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.AddForeignKeyConstraint;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.Column;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.CreateIndex;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.CreateTable;

public class TranslatedTablesData {

    private Map<String, EntityTableData> persistenceUnitNameToEntityTables;
    private Map<Association, JoinTableData> associationToJoinTables;
    @Getter
    private List<CreateIndex> createIndices;
    @Getter
    private List<AddForeignKeyConstraint> addForeignKeyConstraints;

    public TranslatedTablesData() {
	persistenceUnitNameToEntityTables = new HashMap<>();
	associationToJoinTables = new HashMap<>();
	createIndices = new LinkedList<>();
	addForeignKeyConstraints = new LinkedList<>();
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

    public CreateTable getCreateTableByReferentialAttribute(@NonNull ReferentialAttribute referentialAttribute) {
	CreateTable byAssociation = getByAssociation(referentialAttribute.getAssociation());
	if (byAssociation != null) {
	    return byAssociation;
	}
	EntityTableData entityTable = getByPersistenceUnitName(referentialAttribute.getParentClassifier().getName());
	return Optional.ofNullable(entityTable).map(EntityTableData::getCreateTable).orElse(null);
    }

    public Column getJoinColumnByReferentialAttribute(@NonNull ReferentialAttribute referentialAttribute) {
	Association association = referentialAttribute.getAssociation();
	if (associationToJoinTables.containsKey(association)) {
	    ReferentialAttribute primaryAttribute = TranslatorHelper.definePrimaryAttribute(association);
	    if (referentialAttribute == primaryAttribute) {

		return associationToJoinTables.get(association).getPrimaryAttributeJoinColumn();
	    } else {

		return associationToJoinTables.get(association).getSecondaryAttributeJoinColumn();
	    }
	}
	String persistenceUnitName = referentialAttribute.getParentClassifier().getName();
	return getColumnByReferentialAttributeAndPersistenceUnitName(referentialAttribute, persistenceUnitName);
//	return parentTableColumn;
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

    public void addCreateIndex(CreateIndex createIndex) {
	createIndices.add(createIndex);
    }

    public void addAddForeignKeyConstraint(AddForeignKeyConstraint addForeignKeyConstraint) {
	addForeignKeyConstraints.add(addForeignKeyConstraint);
    }

    public Stream<CreateTable> getTranslatedCreateTables() {
	return Stream.concat(persistenceUnitNameToEntityTables.values().stream().map(EntityTableData::getCreateTable),
		associationToJoinTables.values().stream().map(JoinTableData::getCreateTable)).distinct();
    }
}
