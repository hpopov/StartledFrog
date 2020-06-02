package net.atlassian.cmathtutor.domain.persistence.translate;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javax.persistence.CascadeType;

import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.domain.persistence.AggregationKind;
import net.atlassian.cmathtutor.domain.persistence.Association;
import net.atlassian.cmathtutor.domain.persistence.Persistence;
import net.atlassian.cmathtutor.domain.persistence.PersistenceUnit;
import net.atlassian.cmathtutor.domain.persistence.PrimitiveAttribute;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.AbstractChangeSet;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.AddForeignKeyConstraint;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.AddForeignKeyConstraintChangeSet;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.Column;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.CreateIndex;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.CreateIndexChangeSet;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.CreateTable;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.CreateTableChangeSet;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.DatabaseChangeLog;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.FkCascadeActionType;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator.AddForeignKeyConstraintNameTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator.AssociationToJoinTableDataTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator.AttributeNameToColumnNameTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator.AttributeToColumnTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator.ConstraintsTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator.CreateIndexTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator.EntityTableData;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator.JoinTableData;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator.PrimitiveTypeToColumnTypeTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator.TableNameTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.translator.TranslatedTablesData;
import net.atlassian.cmathtutor.domain.persistence.translate.java.ClassType;
import net.atlassian.cmathtutor.domain.persistence.translate.java.ClassTypes;
import net.atlassian.cmathtutor.domain.persistence.translate.java.Type;
import net.atlassian.cmathtutor.domain.persistence.translate.java.translator.AnnotationInstanceTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.java.translator.AttributeNameToVariableNameTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.java.translator.AttributeToFieldTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.java.translator.PersistenceUnitNameToEntityNameTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.java.translator.PrimitiveTypeToTypeTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.java.translator.ProjectToApplicationTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.java.translator.TranslatedClassesData;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.ContainableEntity;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.Entity;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.Operation;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.Repository;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.Variable;
import net.atlassian.cmathtutor.domain.persistence.translate.manager.AssociationTranslationManager;
import net.atlassian.cmathtutor.model.Project;
import net.atlassian.cmathtutor.service.PersistenceDomainService;
import net.atlassian.cmathtutor.util.CaseUtil;

@Slf4j
public class PersistenceModelTranslator {

    private static final CascadeType SECONDARY_CASCADE_TYPE = CascadeType.DETACH;
    private static final CascadeType[] SECONDARY_CASCADE_TYPES = { CascadeType.MERGE, CascadeType.PERSIST,
	    CascadeType.REFRESH };

    private final Project project;
    private final String entitiesPackageName;
    private final String repositoriesPackageName;
    private TranslatedClassesData translatedClasses;
    private TranslatedTablesData translatedTables;
    private DatabaseChangeLog changeLog;
    private Persistence persistence = null;

    private AttributeNameToColumnNameTranslator attributeToColumnNameTranslator = new AttributeNameToColumnNameTranslator();
    private PersistenceUnitNameToEntityNameTranslator persistenceUnitToEntityNameTranslator = new PersistenceUnitNameToEntityNameTranslator();
    private AttributeNameToVariableNameTranslator attributeToFieldNameTranslator = new AttributeNameToVariableNameTranslator();
    private TableNameTranslator tableNameTranslator = new TableNameTranslator(attributeToColumnNameTranslator);
    private PrimitiveTypeToColumnTypeTranslator typeTranslator = new PrimitiveTypeToColumnTypeTranslator();
    private ConstraintsTranslator constraintsTranslator = new ConstraintsTranslator();
    private AttributeToColumnTranslator attributeToColumnTranslator = new AttributeToColumnTranslator(
	    attributeToColumnNameTranslator, typeTranslator, constraintsTranslator);
    private AnnotationInstanceTranslator annotationInstanceTranslator = new AnnotationInstanceTranslator();
    private PrimitiveTypeToTypeTranslator primitiveTypeTranslator = new PrimitiveTypeToTypeTranslator();
    private AttributeToFieldTranslator attributeToFieldTranslator = new AttributeToFieldTranslator(
	    primitiveTypeTranslator, attributeToFieldNameTranslator, annotationInstanceTranslator);
    private AssociationToJoinTableDataTranslator joinTableDataTranslator = new AssociationToJoinTableDataTranslator(
	    tableNameTranslator, attributeToColumnTranslator);
    private CreateIndexTranslator createIndexTranslator = new CreateIndexTranslator();
    private AddForeignKeyConstraintNameTranslator fkConstraintNameTranslator = new AddForeignKeyConstraintNameTranslator();

    public PersistenceModelTranslator(Project project) {
	this.project = project;
	this.entitiesPackageName = PersistenceDomainService.resolveEntityBasePackage(project);
	this.repositoriesPackageName = PersistenceDomainService.resolveRepositoryBasePackage(project);
	translatedClasses = new TranslatedClassesData();
	translatedTables = new TranslatedTablesData();
    }

    public void translate(Persistence persistence) {
	this.persistence = persistence;
	translatedClasses.setApplication(new ProjectToApplicationTranslator().translate(project));
	translateEntitiesWithPrimitiveAttributes();
	translateAssociations();
	translateForeignKeysConstraints();
	translateRepositories();
	changeLog = wrapTranslatedTablesInChangeLog();
    }

    private void translateEntitiesWithPrimitiveAttributes() {
	for (PersistenceUnit persistenceUnit : persistence.getUnmodifiablePersistenceUnits()) {
	    String persistenceUnitName = persistenceUnit.getName();
	    String entityName = persistenceUnitToEntityNameTranslator.translate(persistenceUnitName);
	    String tableName = tableNameTranslator.translatePersistenceUnitName(persistenceUnitName);
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
	}
    }

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

    private void translateForeignKeysConstraints() {
	persistence.getUnmodifiableAssociations().stream()
		.flatMap(assoc -> Stream.of(assoc.getContainerAttribute(), assoc.getElementAttribute()))
		.forEach(this::createIndexAndForeignKeyConstraint);
    }

    private void createIndexAndForeignKeyConstraint(ReferentialAttribute attribute) {
	CreateTable createTable = translatedTables.getCreateTableByReferentialAttribute(attribute);
	Column joinColumn = translatedTables.getJoinColumnByReferentialAttribute(attribute);
	if (joinColumn == null) {
	    log.info("attribute {}.{} doesn't have corresponding column. Skipping it...",
		    attribute.getParentClassifier().getName(), attribute.getName());
	    return;
	}
	String baseTableName = createTable.getTableName();
	String baseColumnName = joinColumn.getName();
	CreateIndex createIndex = createIndexTranslator.translate(baseTableName, baseColumnName);
	translatedTables.addCreateIndex(createIndex);
	PersistenceUnit referencedPersistenceUnit = TranslatorHelper.getAnotherAttributeFromAssociation(attribute)
		.getParentClassifier();
	String referencedTableName = translatedTables
		.getByPersistenceUnitName(referencedPersistenceUnit.getName()).getName();
	boolean isTableJoin = translatedTables.getByPersistenceUnitName(attribute.getParentClassifier().getName())
		.getName() != baseTableName;
	AddForeignKeyConstraint addForeignKeyConstraint = AddForeignKeyConstraint.builder()
		.baseColumnNames(baseColumnName)
		.baseTableName(baseTableName)
		.constraintName(
			fkConstraintNameTranslator.translate(baseTableName, referencedTableName, baseColumnName))
		.deferrable(false)
		.initiallyDeferred(false)
		.onDelete(translateOnDeleteCascadeType(attribute, isTableJoin))
		.onUpdate(FkCascadeActionType.RESTRICT)
		.referencedColumnNames(AttributeToColumnTranslator.PK_COLUMN_NAME)
		.referencedTableName(referencedTableName)
		.build();
	translatedTables.addAddForeignKeyConstraint(addForeignKeyConstraint);
    }

    private FkCascadeActionType translateOnDeleteCascadeType(ReferentialAttribute attribute, boolean isTableJoin) {
	Association association = attribute.getAssociation();
	AggregationKind aggregationKind = association.getAggregationKind();
	switch (aggregationKind) {
	case NONE:
	    if (attribute.getArity().isNullable()) {
		if (isTableJoin) {
		    return FkCascadeActionType.CASCADE;
		} else {
		    return FkCascadeActionType.SET_NULL;
		}
	    }
	    break;
	case SHARED:
	    if (attribute == association.getContainerAttribute() || attribute.getArity().isNotNullable()) {
		return FkCascadeActionType.RESTRICT;
	    } else if (isTableJoin) {
		return FkCascadeActionType.CASCADE;
	    } else if (attribute.getArity().isNullable()) {
		return FkCascadeActionType.SET_NULL;
	    }
	    break;
	case COMPOSITE:
	    if (attribute == association.getElementAttribute()) {
		return FkCascadeActionType.CASCADE;// ?
	    }
	    break;
	default:
	    throw new UnimplementedEnumConstantException(aggregationKind);
	}
	return FkCascadeActionType.RESTRICT;
    }

    private void translateRepositories() {
	for (PersistenceUnit pu : persistence.getUnmodifiablePersistenceUnits()) {
	    ContainableEntity containableEntity = translatedClasses
		    .getContainableEntityByPersistenceUnitName(pu.getName());
	    Entity entity = translatedClasses.getEntityByPersistenceUnitName(pu.getName());
	    String repositoryName = entity.getName() + "Repository";
	    ClassType<?> superType = containableEntity != null ? ClassTypes.containableRepository(containableEntity)
		    : ClassTypes.crudRepository(entity);
	    Repository repository = new Repository(repositoryName, repositoriesPackageName, superType);
	    translatedClasses.addRepository(pu.getName(), repository);
	}
	persistence.getUnmodifiableAssociations().stream().map(TranslatorHelper::defineSecondaryAttribute)
		.filter(ReferentialAttribute::isNavigable).forEach(this::addSecondaryReferenceRepositoryMethod);
    }

    private void addSecondaryReferenceRepositoryMethod(ReferentialAttribute secondaryAttribute) {
	ReferentialAttribute primaryAttribute = TranslatorHelper.getAnotherAttributeFromAssociation(secondaryAttribute);
	String primaryPuName = primaryAttribute.getParentClassifier().getName();
	Repository repository = translatedClasses.getRepositoryByPersistenceUnitName(primaryPuName);
	Type methodReturnType = attributeToFieldTranslator.translateAttributeArityToType(
		translatedClasses.getEntityByPersistenceUnitName(primaryPuName), secondaryAttribute.getArity());
	String primaryFieldName = attributeToFieldNameTranslator.translate(primaryAttribute.getName());
	String methodName = "findBy" + CaseUtil.capitalizeFirstCharacter(primaryFieldName);
	if (primaryAttribute.getArity().isMany()) {
	    methodName = methodName + "In";
	}

	String secondaryPuName = secondaryAttribute.getParentClassifier().getName();
	Variable methodParameter = attributeToFieldTranslator.translateReferentialAttribute(primaryAttribute,
		translatedClasses.getEntityByPersistenceUnitName(secondaryPuName));
	repository.getOperations().add(Operation.builder()
		.arguments(Collections.singletonList(methodParameter))
		.name(methodName)
		.returnType(methodReturnType)
		.build());
    }

    public DatabaseChangeLog getTranslatedChangeLog() {
	if (persistence == null) {
	    throw new IllegalStateException(
		    "Can not retrieve translated changeLog when no translate method was called");
	}

	return changeLog;
    }

    private DatabaseChangeLog wrapTranslatedTablesInChangeLog() {
	String author = "Startled Frog (generated)";
	DatabaseChangeLog changeLog = new DatabaseChangeLog();
	List<AbstractChangeSet> changeSets = changeLog.getAbstractChangeSets();
	String dateString = Calendar.getInstance().getTime().toString() + "-";
	int[] changeSetNumber = { 1 };
	translatedTables.getTranslatedCreateTables().map(ct -> CreateTableChangeSet.builder()
		.author(author)
		.createTable(ct)
		.id(dateString + changeSetNumber[0]++)
		.build()).forEach(changeSets::add);
	translatedTables.getCreateIndices().stream().map(ci -> CreateIndexChangeSet.builder()
		.author(author)
		.createIndex(ci)
		.id(dateString + changeSetNumber[0]++)
		.build()).forEach(changeSets::add);
	translatedTables.getAddForeignKeyConstraints().stream().map(afkc -> AddForeignKeyConstraintChangeSet.builder()
		.author(author)
		.addForeignKeyConstraint(afkc)
		.id(dateString + changeSetNumber[0]++)
		.build()).forEach(changeSets::add);

	return changeLog;
    }

    public TranslatedClassesData getTranslatedClasses() {
	if (persistence == null) {
	    throw new IllegalStateException(
		    "Can not retrieve translated classes when no translate() method has been called");
	}

	return translatedClasses;
    }
}
