package net.atlassian.cmathtutor.domain.persistence.translate.java.translator;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.JoinColumn;

import net.atlassian.cmathtutor.domain.persistence.AttributeArity;
import net.atlassian.cmathtutor.domain.persistence.ConstraintType;
import net.atlassian.cmathtutor.domain.persistence.translate.java.instance.AnnotationInstance;
import net.atlassian.cmathtutor.domain.persistence.translate.java.instance.AnnotationInstances;

public class AnnotationInstanceTranslator {

    public AnnotationInstance<Column> translateToColumn(Set<ConstraintType> constraints, String columnName) {
        boolean unique = constraints.contains(ConstraintType.UNIQUE);
        boolean nullable = !constraints.contains(ConstraintType.NON_NULL);

        return AnnotationInstances.column(columnName, nullable, unique);
    }

    public AnnotationInstance<JoinColumn> translateArityToJoinColumn(
            AttributeArity attributeArity,
            AttributeArity referencedAttributeArity, String columnName
    ) {
        return AnnotationInstances.joinColumnBuilder()
                .name(columnName)// TODO Maybe referencedColumn name is also
                                 // required in case of oneToMany
                                 // primary attr?
                .nullable(!attributeArity.equals(AttributeArity.ONE_EXACTLY))
                .unique(!referencedAttributeArity.equals(AttributeArity.AT_LEAST_ZERO))
                .build();
    }

    public AnnotationInstance<JoinColumn> translateArityToJoinColumnInJoinTable(
            AttributeArity referencedAttributeArity,
            String columnName
    ) {
        return AnnotationInstances.joinColumnBuilder()
                .name(columnName)
                .nullable(false)
                .unique(!referencedAttributeArity.equals(AttributeArity.AT_LEAST_ZERO))
                .build();
    }
}
