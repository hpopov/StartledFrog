package net.atlassian.cmathtutor.domain.persistence.translate.java.translator;

import java.util.Set;

import javax.persistence.Column;

import net.atlassian.cmathtutor.domain.persistence.ConstraintType;
import net.atlassian.cmathtutor.domain.persistence.translate.java.instance.AnnotationInstance;
import net.atlassian.cmathtutor.domain.persistence.translate.java.instance.AnnotationInstances;

public class PrimitiveAttributeConstraintsToAnnotationInstance {

    public AnnotationInstance<Column> translate(Set<ConstraintType> constraints, String columnName) {
	boolean unique = constraints.contains(ConstraintType.UNIQUE);
	boolean nullable = !constraints.contains(ConstraintType.NON_NULL);

	return AnnotationInstances.column(columnName, nullable, unique);
    }
}
