package net.atlassian.cmathtutor.domain.persistence.translate;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;

import org.apache.commons.lang3.tuple.Pair;

import net.atlassian.cmathtutor.domain.persistence.AggregationKind;
import net.atlassian.cmathtutor.domain.persistence.Association;
import net.atlassian.cmathtutor.domain.persistence.AttributeArity;
import net.atlassian.cmathtutor.domain.persistence.Persistence;
import net.atlassian.cmathtutor.domain.persistence.PersistenceUnit;
import net.atlassian.cmathtutor.domain.persistence.PrimitiveAttribute;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.Column;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.CreateTable;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.DatabaseChangeLog;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator.AssociationToJoinTableDataTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator.AttributeNameToColumnNameTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator.AttributeToColumnTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator.EntityTableData;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator.JoinTableData;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator.TableNameTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator.TranslatedTablesData;
import net.atlassian.cmathtutor.domain.persistence.translate.java.translator.AttributeNameToVariableNameTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.java.translator.AttributeToFieldTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.java.translator.PersistenceUnitNameToEntityNameTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.java.translator.ProjectToApplicationTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.java.translator.TranslatedClassesData;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.ContainableEntity;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.Entity;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.Variable;
import net.atlassian.cmathtutor.domain.persistence.translate.manager.AssociationTranslationManager;
import net.atlassian.cmathtutor.model.Project;

public class PersistenceModelTranslator {

    private static final String DOT_ENTITY = ".entity";

    private static final CascadeType SECONDARY_CASCADE_TYPE = CascadeType.DETACH;
    private static final CascadeType[] SECONDARY_CASCADE_TYPES = { CascadeType.MERGE, CascadeType.PERSIST,
	    CascadeType.PERSIST, CascadeType.REFRESH };

    private final Project project;
    private final String entitiesPackageName;
    private TranslatedClassesData translatedClasses;
    private TranslatedTablesData translatedTables;
    private Persistence persistence = null;

    private AttributeNameToColumnNameTranslator attributeToColumnNameTranslator;
    private PersistenceUnitNameToEntityNameTranslator persistenceUnitToEntityNameTranslator;
    private AttributeNameToVariableNameTranslator attributeToFieldNameTranslator;
    private TableNameTranslator persistenceUnitToTableNameTranslator;
    private AttributeToColumnTranslator attributeToColumnTranslator;
    private AttributeToFieldTranslator attributeToFieldTranslator;
    private AssociationToJoinTableDataTranslator joinTableDataTranslator;

    public PersistenceModelTranslator(Project project) {
	this.project = project;
	this.entitiesPackageName = project.getRootPackage() + DOT_ENTITY;
	translatedClasses = new TranslatedClassesData();
	translatedTables = new TranslatedTablesData();
    }

    public void translate(Persistence persistence) {
	this.persistence = persistence;
	translatedClasses.setApplication(new ProjectToApplicationTranslator().translate(project));
	translateEntitiesWithPrimitiveAttributes();
	translateAssociations();
    }

    private void translateEntitiesWithPrimitiveAttributes() {
	for (PersistenceUnit persistenceUnit : persistence.getUnmodifiablePersistenceUnits()) {
	    String persistenceUnitName = persistenceUnit.getName();
	    String entityName = persistenceUnitToEntityNameTranslator.translate(persistenceUnitName);
	    String tableName = persistenceUnitToTableNameTranslator.translatePersistenceUnitName(persistenceUnitName);
	    EntityTableData entityTable = new EntityTableData(tableName);
	    Entity entity = createEntityData(persistenceUnit, entityName, tableName);
	    entityTable.addColumn(attributeToColumnTranslator.pkColumn());
	    entity.getFields().add(attributeToFieldTranslator.pkField());
	    for (PrimitiveAttribute attribute : persistenceUnit.getUnmodifiablePrimitiveAttributes()) {
		Column column = attributeToColumnTranslator.translate(attribute);
		entityTable.addColumn(column);
		entity.getFields()
			.add(attributeToFieldTranslator.translatePrimitiveAttribute(attribute, column.getName()));
	    }
	    translatedTables.addPersistenceUnitEntityTable(persistenceUnitName, entityTable);
	    translatedClasses.addEntity(persistenceUnitName, entity);
	}
    }

    private Entity createEntityData(PersistenceUnit persistenceUnit, String entityName, String tableName) {
	boolean isContainableEntity = persistenceUnit.getUnmodifiableReferentialAttributes().stream()
		.filter(ra -> ra.getAssociation().getElementAttribute().equals(ra))
		.map(ReferentialAttribute::getAssociation)
		.map(Association::getAggregationKind)
		.anyMatch(kind -> kind.equals(AggregationKind.COMPOSITE) || kind.equals(AggregationKind.SHARED));
	if (isContainableEntity) {
	    return new ContainableEntity(entityName, entitiesPackageName, tableName);
	}
	return new Entity(entityName, entitiesPackageName, tableName);
    }

    private void translateAssociations() {
	for (Association association : persistence.getUnmodifiableAssociations()) {
	    translateAssociation(association);
//	    switch (association.getAggregationKind()) {
//	    case COMPOSITE:
//		translateComposition(association);
//		break;
//	    case NONE:
//		translatePureAssociation(association);
//		break;
//	    case SHARED:
//		translateAggregation(association);
//		break;
//	    default:
//		throw new UnimplementedEnumConstantException(association.getAggregationKind());
//	    }
	}

    }

//    private void translateComposition(Association association) {
//	Pair<ReferentialAttribute, ReferentialAttribute> primaryAndSecondaryAttributes = TranslatorHelper
//		.definePrimaryAndSecondaryAttributes(association);
//	ReferentialAttribute primaryAttr = primaryAndSecondaryAttributes.getKey();
//	ReferentialAttribute secondaryAttr = primaryAndSecondaryAttributes.getValue();
//	if (secondaryAttr.getArity().equals(AttributeArity.ONE_EXACTLY)
//		|| association.getElementAttribute().getArity().isMany()) {
//	    throw new UnsupportedAssociationException(primaryAttr, secondaryAttr);
//	}
//
//	String primaryPersistenceUnitName = primaryAttr.getParentClassifier().getName();
//	String secondaryPersistenceUnitName = secondaryAttr.getParentClassifier().getName();
//	EntityTableData primaryEntityTable = translatedTables.getByPersistenceUnitName(primaryPersistenceUnitName);
//	EntityTableData secondaryEntityTable = translatedTables.getByPersistenceUnitName(secondaryPersistenceUnitName);
//
//	Entity primaryEntity = translatedClasses.getEntityByPersistenceUnitName(primaryPersistenceUnitName);
//	Entity secondaryEntity = translatedClasses.getEntityByPersistenceUnitName(secondaryPersistenceUnitName);
//	CascadeType cascadeType = CascadeType.DETACH;
//	CascadeType[] cascadeTypesWithoutRemove = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH };
//	CascadeType[] cascadeTypesWithRemove = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH,
//		CascadeType.REMOVE };
//	CascadeType[] cascadeTypes = (association.getAggregationKind().equals(AggregationKind.COMPOSITE)
//		&& primaryAttr == association.getContainerAttribute())
//			? cascadeTypesWithRemove
//			: cascadeTypesWithoutRemove;
//
//	Variable primaryReferenceField;
//	Variable secondaryReferenceField = null;
//	if (primaryAttr.getArity().isMany()
//		&& (secondaryAttr.getArity().isMany() || secondaryAttr == association.getElementAttribute())) {
//	    JoinTableData joinTable = joinTableDataTranslator.translateAssociationToJoinTable(primaryAttr,
//		    primaryEntityTable.getName(), secondaryEntityTable.getName());
//	    translatedTables.addAssociationJoinTable(association, joinTable);
//	    primaryReferenceField = attributeToFieldTranslator
//		    .translatePrimaryReferentialAttribute(primaryAttr, secondaryEntity, joinTable, cascadeType,
//			    cascadeTypes);
//	} else {
//	    Column fkColumn;
//	    if (primaryAttr.getArity().isOne()) {
//		fkColumn = attributeToColumnTranslator.translateReferentialAttributeToJoinColumn(primaryAttr,
//			secondaryEntityTable.getName());
//		primaryEntityTable.addJoinColumn(primaryAttr, fkColumn);
//	    } else {
//		fkColumn = attributeToColumnTranslator.translateReferentialAttributeToJoinColumn(secondaryAttr,
//			primaryEntityTable.getName());
//		secondaryEntityTable.addJoinColumn(secondaryAttr, fkColumn);
//	    }
//	    primaryReferenceField = attributeToFieldTranslator
//		    .translatePrimaryReferentialAttribute(primaryAttr, secondaryEntity, fkColumn.getName(), cascadeType,
//			    cascadeTypes);
//	}
//	primaryEntity.getFields().add(primaryReferenceField);
//	if (secondaryAttr.isNavigable() || secondaryAttr == association.getElementAttribute()) {
//	    secondaryReferenceField = attributeToFieldTranslator
//		    .translateSecondaryReferentialAttribute(secondaryAttr, primaryEntity, cascadeType,
//			    cascadeTypesWithoutRemove);
//	    secondaryEntity.getFields().add(secondaryReferenceField);
//	}
//	if (primaryEntity instanceof ContainableEntity) {
//	    ContainableEntity containablePrimaryEntity = (ContainableEntity) primaryEntity;
//	    containablePrimaryEntity.selectCompositeParentField(primaryReferenceField.getName());
//	} else if (secondaryEntity instanceof ContainableEntity) {
//	    ContainableEntity containableSecondaryEntity = (ContainableEntity) secondaryEntity;
//	    containableSecondaryEntity.selectCompositeParentField(secondaryReferenceField.getName());
//	}
//    }

//    private void translatePureAssociation(Association association) {
//	Pair<ReferentialAttribute, ReferentialAttribute> primaryAndSecondaryAttributes = TranslatorHelper
//		.definePrimaryAndSecondaryAttributes(association);
//	ReferentialAttribute primaryAttr = primaryAndSecondaryAttributes.getKey();
//	ReferentialAttribute secondaryAttr = primaryAndSecondaryAttributes.getValue();
//
//	String primaryPersistenceUnitName = primaryAttr.getParentClassifier().getName();
//	String secondaryPersistenceUnitName = secondaryAttr.getParentClassifier().getName();
//	EntityTableData primaryEntityTable = translatedTables.getByPersistenceUnitName(primaryPersistenceUnitName);
//	EntityTableData secondaryEntityTable = translatedTables.getByPersistenceUnitName(secondaryPersistenceUnitName);
//
//	Entity primaryEntity = translatedClasses.getEntityByPersistenceUnitName(primaryPersistenceUnitName);
//	Entity secondaryEntity = translatedClasses.getEntityByPersistenceUnitName(secondaryPersistenceUnitName);
//	CascadeType cascadeType = CascadeType.DETACH;
//	CascadeType[] cascadeTypes = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH };
//	if (secondaryAttr.getArity().equals(AttributeArity.ONE_EXACTLY)) {
//	    throw new UnsupportedAssociationException(primaryAttr, secondaryAttr);
//	}
//
//	if (primaryAttr.getArity().isMany() && secondaryAttr.getArity().isMany()) {
//	    JoinTableData joinTable = joinTableDataTranslator.translateAssociationToJoinTable(primaryAttr,
//		    primaryEntityTable.getName(), secondaryEntityTable.getName());
//	    translatedTables.addAssociationJoinTable(association, joinTable);
//	    Variable primaryReferenceField = attributeToFieldTranslator
//		    .translatePrimaryReferentialAttribute(primaryAttr, secondaryEntity, joinTable, cascadeType,
//			    cascadeTypes);
//	    primaryEntity.getFields().add(primaryReferenceField);
//	} else {
//	    Column fkColumn;
//	    if (primaryAttr.getArity().isOne()) {
//		fkColumn = attributeToColumnTranslator.translateReferentialAttributeToJoinColumn(primaryAttr,
//			secondaryEntityTable.getName());
//		primaryEntityTable.addJoinColumn(primaryAttr, fkColumn);
//	    } else {
//		fkColumn = attributeToColumnTranslator.translateReferentialAttributeToJoinColumn(secondaryAttr,
//			primaryEntityTable.getName());
//		secondaryEntityTable.addJoinColumn(secondaryAttr, fkColumn);
//	    }
//	    Variable primaryReferenceField = attributeToFieldTranslator
//		    .translatePrimaryReferentialAttribute(primaryAttr, secondaryEntity, fkColumn.getName(), cascadeType,
//			    cascadeTypes);
//	    primaryEntity.getFields().add(primaryReferenceField);
//	}
//	if (secondaryAttr.isNavigable()) {
//	    Variable secondaryReferenceField = attributeToFieldTranslator
//		    .translateSecondaryReferentialAttribute(secondaryAttr, primaryEntity, cascadeType, cascadeTypes);
//	    secondaryEntity.getFields().add(secondaryReferenceField);
//	}
//    }

//    private void translateAggregation(Association association) {
//	Pair<ReferentialAttribute, ReferentialAttribute> primaryAndSecondaryAttributes = TranslatorHelper
//		.definePrimaryAndSecondaryAttributes(association);
//	ReferentialAttribute primaryAttr = primaryAndSecondaryAttributes.getKey();
//	ReferentialAttribute secondaryAttr = primaryAndSecondaryAttributes.getValue();
//
//	String primaryPersistenceUnitName = primaryAttr.getParentClassifier().getName();
//	String secondaryPersistenceUnitName = secondaryAttr.getParentClassifier().getName();
//	EntityTableData primaryEntityTable = translatedTables.getByPersistenceUnitName(primaryPersistenceUnitName);
//	EntityTableData secondaryEntityTable = translatedTables.getByPersistenceUnitName(secondaryPersistenceUnitName);
//
//	Entity primaryEntity = translatedClasses.getEntityByPersistenceUnitName(primaryPersistenceUnitName);
//	Entity secondaryEntity = translatedClasses.getEntityByPersistenceUnitName(secondaryPersistenceUnitName);
//	CascadeType cascadeType = CascadeType.DETACH;
//	CascadeType[] cascadeTypes = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH };
//	if (secondaryAttr.getArity().equals(AttributeArity.ONE_EXACTLY)) {
//	    throw new UnsupportedAssociationException(primaryAttr, secondaryAttr);
//	}
//
//	Variable primaryReferenceField;
//	Variable secondaryReferenceField = null;
//	if (primaryAttr.getArity().isMany()
//		&& (secondaryAttr.getArity().isMany() || secondaryAttr == association.getElementAttribute())) {
//	    JoinTableData joinTable = joinTableDataTranslator.translateAssociationToJoinTable(primaryAttr,
//		    primaryEntityTable.getName(), secondaryEntityTable.getName());
//	    translatedTables.addAssociationJoinTable(association, joinTable);
//	    primaryReferenceField = attributeToFieldTranslator
//		    .translatePrimaryReferentialAttribute(primaryAttr, secondaryEntity, joinTable, cascadeType,
//			    cascadeTypes);
//	} else {
//	    Column fkColumn;
//	    if (primaryAttr.getArity().isOne()) {
//		fkColumn = attributeToColumnTranslator.translateReferentialAttributeToJoinColumn(primaryAttr,
//			secondaryEntityTable.getName());
//		primaryEntityTable.addJoinColumn(primaryAttr, fkColumn);
//	    } else {
//		fkColumn = attributeToColumnTranslator.translateReferentialAttributeToJoinColumn(secondaryAttr,
//			primaryEntityTable.getName());
//		secondaryEntityTable.addJoinColumn(secondaryAttr, fkColumn);
//	    }
//	    primaryReferenceField = attributeToFieldTranslator
//		    .translatePrimaryReferentialAttribute(primaryAttr, secondaryEntity, fkColumn.getName(), cascadeType,
//			    cascadeTypes);
//	}
//	primaryEntity.getFields().add(primaryReferenceField);
//	if (secondaryAttr.isNavigable() || secondaryAttr == association.getElementAttribute()) {
//	    secondaryReferenceField = attributeToFieldTranslator
//		    .translateSecondaryReferentialAttribute(secondaryAttr, primaryEntity, cascadeType,
//			    cascadeTypes);
//	    secondaryEntity.getFields().add(secondaryReferenceField);
//	}
//	if (primaryEntity instanceof ContainableEntity) {
//	    ContainableEntity containablePrimaryEntity = (ContainableEntity) primaryEntity;
//	    containablePrimaryEntity.selectCompositeParentField(primaryReferenceField.getName());
//	} else if (secondaryEntity instanceof ContainableEntity) {
//	    ContainableEntity containableSecondaryEntity = (ContainableEntity) secondaryEntity;
//	    containableSecondaryEntity.selectCompositeParentField(secondaryReferenceField.getName());
//	}
//    }

    private void translateAssociation(Association association) {
	AssociationTranslationManager translationManager = AssociationTranslationManager.of(association);
	ReferentialAttribute primaryAttr = translationManager.getPrimaryAttribute();
	ReferentialAttribute secondaryAttr = translationManager.getSecondaryAttribute();
	if (translationManager.isAssociationUnsupported()) {
	    throw new UnsupportedAssociationException(primaryAttr, secondaryAttr);
	}

	String primaryPersistenceUnitName = primaryAttr.getParentClassifier().getName();
	String secondaryPersistenceUnitName = secondaryAttr.getParentClassifier().getName();
	EntityTableData primaryEntityTable = translatedTables.getByPersistenceUnitName(primaryPersistenceUnitName);
	EntityTableData secondaryEntityTable = translatedTables.getByPersistenceUnitName(secondaryPersistenceUnitName);

	Entity primaryEntity = translatedClasses.getEntityByPersistenceUnitName(primaryPersistenceUnitName);
	Entity secondaryEntity = translatedClasses.getEntityByPersistenceUnitName(secondaryPersistenceUnitName);

	CascadeType[] cascadesArray = translationManager.getCascadesForPrimaryReference();
	CascadeType primaryCascadeType = cascadesArray[0];
	CascadeType[] primaryCascadeTypes = Arrays.copyOfRange(cascadesArray, 1, cascadesArray.length);

	Variable primaryReferenceField;
	Variable secondaryReferenceField = null;
	if (translationManager.isTableJoin()) {
	    JoinTableData joinTable = joinTableDataTranslator.translateAssociationToJoinTable(primaryAttr,
		    primaryEntityTable.getName(), secondaryEntityTable.getName());
	    translatedTables.addAssociationJoinTable(association, joinTable);
	    primaryReferenceField = attributeToFieldTranslator
		    .translatePrimaryReferentialAttribute(primaryAttr, secondaryEntity, joinTable, primaryCascadeType,
			    primaryCascadeTypes);
	} else {
	    Column fkColumn;
	    if (primaryAttr.getArity().isMany() && secondaryAttr.getArity().isOne()) {
		fkColumn = attributeToColumnTranslator.translateReferentialAttributeToJoinColumn(secondaryAttr,
			primaryEntityTable.getName());
		secondaryEntityTable.addJoinColumn(secondaryAttr, fkColumn);
	    } else {
		fkColumn = attributeToColumnTranslator.translateReferentialAttributeToJoinColumn(primaryAttr,
			secondaryEntityTable.getName());
		primaryEntityTable.addJoinColumn(primaryAttr, fkColumn);
	    }
	    primaryReferenceField = attributeToFieldTranslator
		    .translatePrimaryReferentialAttribute(primaryAttr, secondaryEntity, fkColumn.getName(),
			    primaryCascadeType, primaryCascadeTypes);
	}
	primaryEntity.getFields().add(primaryReferenceField);
	if (translationManager.isSecondaryReferenceFieldToBeCreated()) {
	    secondaryReferenceField = attributeToFieldTranslator
		    .translateSecondaryReferentialAttribute(secondaryAttr, primaryEntity, SECONDARY_CASCADE_TYPE,
			    SECONDARY_CASCADE_TYPES);
	    secondaryEntity.getFields().add(secondaryReferenceField);
	}
	if (primaryEntity instanceof ContainableEntity) {
	    ContainableEntity containablePrimaryEntity = (ContainableEntity) primaryEntity;
	    containablePrimaryEntity.selectCompositeParentField(primaryReferenceField.getName());
	} else if (secondaryEntity instanceof ContainableEntity) {
	    ContainableEntity containableSecondaryEntity = (ContainableEntity) secondaryEntity;
	    containableSecondaryEntity.selectCompositeParentField(secondaryReferenceField.getName());
	}
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
