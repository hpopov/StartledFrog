package net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator;

import net.atlassian.cmathtutor.util.CaseUtil;

public class PersistenceUnitNameToTableNameTranslator {

    public String translate(String persistenceUnitName) {
	return CaseUtil.toSnakeCase(persistenceUnitName);
    }
}
