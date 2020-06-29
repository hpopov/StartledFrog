package net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator;

import net.atlassian.cmathtutor.domain.persistence.translate.changelog.ColumnReference;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.CreateIndex;

public class CreateIndexTranslator {

    private static final String INDEX_NAME_FORMAT = "fk" + AttributeNameToColumnNameTranslator.DELIMITER + "%s"
            + AttributeNameToColumnNameTranslator.DELIMITER + "%s" + AttributeNameToColumnNameTranslator.DELIMITER
            + "idx";

    public CreateIndex translate(String tableName, String columnName) {
        return CreateIndex.builder()
                .column(ColumnReference.builder().name(columnName).build())
                .indexName(String.format(INDEX_NAME_FORMAT, tableName, columnName))
                .tableName(tableName)
                .build();
    }
}
