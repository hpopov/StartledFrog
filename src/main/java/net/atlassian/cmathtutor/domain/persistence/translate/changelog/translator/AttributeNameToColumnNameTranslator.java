package net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator;

import net.atlassian.cmathtutor.util.CaseUtil;

public class AttributeNameToColumnNameTranslator {

    public String translate(String attributeName) {
	return CaseUtil.toSnakeCase(attributeName);
    }
}
