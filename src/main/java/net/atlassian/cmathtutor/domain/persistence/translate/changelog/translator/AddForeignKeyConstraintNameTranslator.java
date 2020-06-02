package net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator;

public class AddForeignKeyConstraintNameTranslator {

    private static final String CONSTRAINT_NAME_FORMAT = "fk" + AttributeNameToColumnNameTranslator.DELIMITER + "%s"
	    + AttributeNameToColumnNameTranslator.DELIMITER + "%s" + AttributeNameToColumnNameTranslator.DELIMITER
	    + "%s";

    public String translate(String baseTableName, String referencedTableName, String baseColumnName) {
	return String.format(CONSTRAINT_NAME_FORMAT, baseTableName, referencedTableName, baseColumnName);
    }
}
