package net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator;

import lombok.AllArgsConstructor;
import net.atlassian.cmathtutor.domain.persistence.PrimitiveAttribute;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;
import net.atlassian.cmathtutor.domain.persistence.translate.TranslatorHelper;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.Column;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.ColumnType;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.Constraints;

@AllArgsConstructor
public class AttributeToColumnTranslator {

    public static final ColumnType PK_COLUMN_TYPE = ColumnType.BIGINT;
    public static final String PK_COLUMN_NAME = "PK";
    private static final Column PK_COLUMN;

    static {
        PK_COLUMN = Column.builder()
                .autoIncrement(true)
                .constraints(Constraints.builder().primaryKey(true).build())
                .name(PK_COLUMN_NAME)
                .type(PK_COLUMN_TYPE)
                .build();
    }

    private AttributeNameToColumnNameTranslator nameTranslator;
    private PrimitiveTypeToColumnTypeTranslator typeTranslator;
    private ConstraintsTranslator constraintsTranslator;

    public Column pkColumn() {
        return PK_COLUMN;
    }

    public Column translate(PrimitiveAttribute attribute) {
        return Column.builder()
                .name(nameTranslator.translate(attribute.getName()))
                .type(typeTranslator.translate(attribute.getType()))
                .constraints(constraintsTranslator.translate(attribute.getUnmodifiableConstraints()))
                .build();
    }

    public Column translateReferentialAttributeToJoinColumn(
            ReferentialAttribute attribute,
            String referencedTableName
    ) {
        ReferentialAttribute referencedAttribute = TranslatorHelper.getAnotherAttributeFromAssociation(attribute);

        return translateReferentialAttributeToColumn(attribute, referencedTableName,
                constraintsTranslator.translateArity(attribute.getArity(), referencedAttribute.getArity()));
    }

    private Column translateReferentialAttributeToColumn(
            ReferentialAttribute attribute, String referencedTableName,
            Constraints constraints
    ) {
        String fkColumnName = nameTranslator.translateReferentialAttributeName(attribute.getName(), referencedTableName)
                + AttributeNameToColumnNameTranslator.DELIMITER + PK_COLUMN_NAME;

        return Column.builder()
                .constraints(constraints)
                .name(fkColumnName)
                .type(PK_COLUMN_TYPE)
                .build();
    }

    public Column translateReferentialAttributeToColumnInJoinTable(
            ReferentialAttribute attribute,
            String referencedTableName
    ) {
        ReferentialAttribute referencedAttribute = TranslatorHelper.getAnotherAttributeFromAssociation(attribute);

        return translateReferentialAttributeToColumn(attribute, referencedTableName, constraintsTranslator
                .translateArityToJoinTableColumnConstraints(attribute.getArity(), referencedAttribute.getArity()));
    }
}
