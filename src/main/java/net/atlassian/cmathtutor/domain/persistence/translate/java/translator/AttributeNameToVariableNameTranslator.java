package net.atlassian.cmathtutor.domain.persistence.translate.java.translator;

import net.atlassian.cmathtutor.util.CaseUtil;

public class AttributeNameToVariableNameTranslator {

    public String translate(String attributeName) {
	return CaseUtil.toLowerCamelCase(attributeName);
    }
}
