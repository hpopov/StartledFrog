package net.atlassian.cmathtutor.domain.persistence.translate.java.translator;

import net.atlassian.cmathtutor.domain.persistence.PrimitiveType;
import net.atlassian.cmathtutor.domain.persistence.translate.UnimplementedEnumConstantException;
import net.atlassian.cmathtutor.domain.persistence.translate.java.Type;

public class PrimitiveTypeToTypeTranslator {

    public Type translate(PrimitiveType primitiveType) {
	switch (primitiveType) {
	case BIG_INTEGER:
	    return net.atlassian.cmathtutor.domain.persistence.translate.java.PrimitiveType.LONG;
	case BOOLEAN:
	    return net.atlassian.cmathtutor.domain.persistence.translate.java.PrimitiveType.BOOLEAN;
	case INTEGER:
	    return net.atlassian.cmathtutor.domain.persistence.translate.java.PrimitiveType.INTEGER;
	case REAL:
	    return net.atlassian.cmathtutor.domain.persistence.translate.java.PrimitiveType.DOUBLE;
	case STRING:
	case TEXT:
	    return net.atlassian.cmathtutor.domain.persistence.translate.java.PrimitiveType.STRING;
	default:
	    throw new UnimplementedEnumConstantException(primitiveType);
	}
    }
}
