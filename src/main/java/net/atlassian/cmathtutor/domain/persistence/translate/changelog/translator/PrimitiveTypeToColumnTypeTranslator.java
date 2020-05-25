package net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator;

import net.atlassian.cmathtutor.domain.persistence.PrimitiveType;
import net.atlassian.cmathtutor.domain.persistence.translate.UnimplementedEnumConstantException;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.ColumnType;

public class PrimitiveTypeToColumnTypeTranslator {

    public ColumnType translate(PrimitiveType type) {
	switch (type) {
	case BIG_INTEGER:
	    return ColumnType.BIGINT;
	case BOOLEAN:
	    return ColumnType.TINYINT;
	case INTEGER:
	    return ColumnType.INT;
	case REAL:
	    return ColumnType.DOUBLE;
	case STRING:
	    return ColumnType.VARCHAR;
	default:
	    throw new UnimplementedEnumConstantException(type);
	}
    }
}
