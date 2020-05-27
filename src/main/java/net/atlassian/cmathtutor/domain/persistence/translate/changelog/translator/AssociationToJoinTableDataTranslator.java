package net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;
import net.atlassian.cmathtutor.domain.persistence.translate.TranslatorHelper;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.Column;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.CreateTable;

@AllArgsConstructor
public class AssociationToJoinTableDataTranslator {

    private TableNameTranslator tableNameTranslator;
    private AttributeToColumnTranslator attributeToColumnTranslator;

    public JoinTableData translateAssociationToJoinTable(@NonNull ReferentialAttribute primaryAttribute,
	    @NonNull String primaryTableName, @NonNull String secondaryTableName) {
	String joinTableName = tableNameTranslator.translateAssociationToTableName(primaryAttribute, primaryTableName,
		secondaryTableName);
	ReferentialAttribute secondaryAttribute = TranslatorHelper.getAnotherAttributeFromAssociation(primaryAttribute);
	Column primaryAttrJoinColumn = attributeToColumnTranslator
		.translateReferentialAttributeToColumnInJoinTable(primaryAttribute, secondaryTableName);
	Column secondaryAttrJoinColumn = attributeToColumnTranslator
		.translateReferentialAttributeToColumnInJoinTable(secondaryAttribute, primaryTableName);
	CreateTable createTable = CreateTable.builder()
		.columns(Arrays.asList(secondaryAttrJoinColumn, primaryAttrJoinColumn))
		.tableName(joinTableName)
		.build();
	return JoinTableData.builder()
		.primaryAttributeJoinColumn(primaryAttrJoinColumn)
		.secondaryAttributeJoinColumn(secondaryAttrJoinColumn)
		.createTable(createTable)
		.build();
    }
}
