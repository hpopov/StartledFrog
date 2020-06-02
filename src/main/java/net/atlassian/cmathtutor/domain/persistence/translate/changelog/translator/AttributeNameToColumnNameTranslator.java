package net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator;

import net.atlassian.cmathtutor.util.CaseUtil;

public class AttributeNameToColumnNameTranslator {

    public static final String DELIMITER = "_";

    public String translate(String attributeName) {
	return CaseUtil.toSnakeCase(attributeName);
    }

    public String translateReferentialAttributeName(String referentialAttributeName, String referencedTableName) {
	return amendTranslatedAttrNameByTableName(CaseUtil.toSnakeCase(referentialAttributeName), referencedTableName);
    }

    private String amendTranslatedAttrNameByTableName(String translatedAttrName, String referencedTableName) {
	if (translatedAttrName.equals(referencedTableName)) {
	    return translatedAttrName;
	} else {
	    return referencedTableName + DELIMITER + translatedAttrName;
	}
    }
}
