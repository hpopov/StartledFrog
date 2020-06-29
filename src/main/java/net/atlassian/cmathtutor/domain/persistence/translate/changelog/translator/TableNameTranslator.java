package net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator;

import lombok.AllArgsConstructor;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;
import net.atlassian.cmathtutor.domain.persistence.translate.TranslatorHelper;
import net.atlassian.cmathtutor.util.CaseUtil;

@AllArgsConstructor
public class TableNameTranslator {

    private static final String TABLE_NAME_JOINER_DELIMITER = AttributeNameToColumnNameTranslator.DELIMITER + "2"
            + AttributeNameToColumnNameTranslator.DELIMITER;

    private AttributeNameToColumnNameTranslator attributeNameTranslator;

    public String translatePersistenceUnitName(String persistenceUnitName) {
        return CaseUtil.toSnakeCase(persistenceUnitName);
    }

    public String translateAssociationToTableName(
            ReferentialAttribute primaryAttribute,
            String primaryTableName, String secondaryTableName
    ) {
        String translatedPrimaryAttrName = attributeNameTranslator
                .translateReferentialAttributeName(primaryAttribute.getName(), secondaryTableName);
        String translatedSecondaryAttrName = attributeNameTranslator
                .translateReferentialAttributeName(
                        TranslatorHelper.getAnotherAttributeFromAssociation(primaryAttribute).getName(),
                        primaryTableName);
        return translatedSecondaryAttrName + TABLE_NAME_JOINER_DELIMITER + translatedPrimaryAttrName;
    }
}
