package net.atlassian.cmathtutor.domain.persistence.translate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import lombok.NonNull;
import net.atlassian.cmathtutor.domain.persistence.AggregationKind;
import net.atlassian.cmathtutor.domain.persistence.Association;
import net.atlassian.cmathtutor.domain.persistence.AttributeArity;
import net.atlassian.cmathtutor.domain.persistence.OwnerType;
import net.atlassian.cmathtutor.domain.persistence.Persistence;
import net.atlassian.cmathtutor.domain.persistence.PersistenceUnit;
import net.atlassian.cmathtutor.domain.persistence.PrimitiveAttribute;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.Column;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.ColumnType;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.Constraints;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.CreateTable;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.DatabaseChangeLog;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator.AttributeNameToColumnNameTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator.PersistenceUnitNameToTableNameTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator.PrimitiveAttributeToColumnTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.java.PrimitiveType;
import net.atlassian.cmathtutor.domain.persistence.translate.java.instance.AnnotationInstances;
import net.atlassian.cmathtutor.domain.persistence.translate.java.translator.AttributeNameToVariableNameTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.java.translator.PersistenceUnitNameToEntityNameTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.java.translator.PrimitiveAttributeToFieldTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.java.translator.ProjectToApplicationDataTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.CompositeElementEntityData;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.EntityData;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.TranslatedClassesData;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.VariableData;
import net.atlassian.cmathtutor.model.Project;

public class PersistenceModelTranslator {

    private static final PrimitiveType PK_FIELD_TYPE = PrimitiveType.LONG;
    private static final String PK_FIELD_NAME = "pk";
    private static final ColumnType PK_COLUMN_TYPE = ColumnType.BIGINT;
    private static final String PK_COLUMN_NAME = "PK";
    private static final String DOT_ENTITY = ".entity";
    private static final Column PK_COLUMN;
    private static final VariableData PK_FIELD;

    static {
	PK_COLUMN = Column.builder()
		.autoIncrement(true)
		.constraints(Constraints.builder().primaryKey(true).build())
		.name(PK_COLUMN_NAME)
		.type(PK_COLUMN_TYPE)
		.build();
	PK_FIELD = new VariableData(PK_FIELD_TYPE, PK_FIELD_NAME);
	PK_FIELD.getAnnotations().addAll(Arrays.asList(
		AnnotationInstances.id(),
		AnnotationInstances.generatedValueIdentity(),
		AnnotationInstances.column(PK_COLUMN_NAME)));
    }

    private final Project project;
    private final String entitiesPackageName;
    private TranslatedClassesData translatedClasses;
    private Persistence persistence = null;

    private AttributeNameToColumnNameTranslator attributeToColumnNameTranslator;
    private PersistenceUnitNameToEntityNameTranslator persistenceUnitToEntityNameTranslator;
    private AttributeNameToVariableNameTranslator attributeToFieldNameTranslator;
    private PersistenceUnitNameToTableNameTranslator persistenceUnitToTableNameTranslator;
    private PrimitiveAttributeToColumnTranslator primitiveAttributeToColumnTranslator;
    private PrimitiveAttributeToFieldTranslator primitiveAttributeToFieldTranslator;

    public PersistenceModelTranslator(Project project) {
	this.project = project;
	this.entitiesPackageName = project.getRootPackage() + DOT_ENTITY;
	translatedClasses = new TranslatedClassesData();
    }

    public void translate(Persistence persistence) {
	this.persistence = persistence;
	translatedClasses.setApplication(new ProjectToApplicationDataTranslator().translate(project));
	Map<String, CreateTable> entityNameToCreateTables = translateEntitiesWithPrimitiveAttributes();
	translateAssociations(entityNameToCreateTables);
    }

    private Map<String, CreateTable> translateEntitiesWithPrimitiveAttributes() {
	Map<String, CreateTable> entityNameToCreateTables = new HashMap<>();
	for (PersistenceUnit persistenceUnit : persistence.getUnmodifiablePersistenceUnits()) {
	    String entityName = persistenceUnitToEntityNameTranslator.translate(persistenceUnit.getName());
	    String tableName = persistenceUnitToTableNameTranslator.translate(persistenceUnit.getName());
	    List<Column> columns = new LinkedList<>();
	    EntityData entityData = createEntityData(persistenceUnit, entityName, tableName);
	    columns.add(pkColumn());
	    entityData.getFields().add(pkField());
	    for (PrimitiveAttribute attribute : persistenceUnit.getUnmodifiablePrimitiveAttributes()) {
		Column column = primitiveAttributeToColumnTranslator.translate(attribute);
		columns.add(column);
		entityData.getFields().add(primitiveAttributeToFieldTranslator.translate(attribute, column.getName()));
	    }
	    entityNameToCreateTables.put(entityName, new CreateTable(tableName, columns));
	    translatedClasses.addEntity(entityData);
	}
	return entityNameToCreateTables;
    }

    private EntityData createEntityData(PersistenceUnit persistenceUnit, String entityName, String tableName) {
	boolean isCompositeElement = persistence.getUnmodifiableAssociations().stream().filter(
		assoc -> persistenceUnit.getUnmodifiableReferentialAttributes()
			.contains(assoc.getElementAttribute()))
		.anyMatch(assoc -> assoc.getAggregationKind().equals(AggregationKind.COMPOSITE));
	if (isCompositeElement) {
	    return new CompositeElementEntityData(entityName, entitiesPackageName, tableName);
	}
	return new EntityData(entityName, entitiesPackageName, tableName);
    }

    private static Column pkColumn() {
	return PK_COLUMN;
    }

    private static VariableData pkField() {
	return PK_FIELD;
    }

    private void translateAssociations(Map<String, CreateTable> entityNameToCreateTables) {
	for (Association association : persistence.getUnmodifiableAssociations()) {
	    switch (association.getAggregationKind()) {
	    case COMPOSITE:
		translateComposition(association, entityNameToCreateTables);
		break;
	    case NONE:
		translatePureAssociation(association, entityNameToCreateTables);
		break;
	    case SHARED:
		translateAggregation(association, entityNameToCreateTables);
		break;
	    default:
		throw new UnimplementedEnumConstantException(association.getAggregationKind());
	    }
	}

    }

    private void translateComposition(Association association, Map<String, CreateTable> entityNameToCreateTables) {
	// TODO Auto-generated method stub

    }

    private void translatePureAssociation(Association association, Map<String, CreateTable> entityNameToCreateTables) {
	Pair<ReferentialAttribute, ReferentialAttribute> primaryAndSecondaryAttributes = definePrimaryAndSecondaryAttributes(
		association);
	ReferentialAttribute primaryAttr = primaryAndSecondaryAttributes.getKey();
	ReferentialAttribute secondaryAttr = primaryAndSecondaryAttributes.getValue();
	// TODO
    }

    private Pair<ReferentialAttribute, ReferentialAttribute> definePrimaryAndSecondaryAttributes(
	    Association association) {
	if (association.getContainerAttribute().getOwnerType().equals(OwnerType.CLASSIFIER)) {
	    return new ImmutablePair<>(association.getContainerAttribute(), association.getElementAttribute());
	} else if (association.getElementAttribute().getOwnerType().equals(OwnerType.CLASSIFIER)) {
	    return new ImmutablePair<>(association.getElementAttribute(), association.getContainerAttribute());
	} else {
	    throw new IllegalArgumentException(
		    "Association " + association + " must contain one end owned by classifier");
	}
    }

    private static boolean isOfArity(@NonNull ReferentialAttribute attribute, @NonNull AttributeArity arity) {
	return attribute.getArity() == arity;
    }

    private void translateAggregation(Association association, Map<String, CreateTable> entityNameToCreateTables) {
	// TODO Auto-generated method stub

    }

    public DatabaseChangeLog getTranslatedChangeLog() {
	if (persistence == null) {
	    throw new IllegalStateException(
		    "Can not retrieve translated changeLog when no translate method was called");
	}
	// TODO
	return null;
    }

    public TranslatedClassesData getTranslatedClasses() {
	if (persistence == null) {
	    throw new IllegalStateException(
		    "Can not retrieve translated classes when no translate method was called");
	}
	// TODO
	return null;
    }
}
