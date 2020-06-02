package net.atlassian.cmathtutor.domain.persistence.translate.java.translator;

import net.atlassian.cmathtutor.util.CaseUtil;

public class PersistenceUnitNameToEntityNameTranslator {

    public String translate(String persistenceUnitName) {
	return CaseUtil.toCapitalizedCamelCase(persistenceUnitName);
    }
}
