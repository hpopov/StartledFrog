package net.atlassian.cmathtutor.domain.persistence.translate.java;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Properties;

import javax.persistence.CascadeType;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.atlassian.cmathtutor.domain.persistence.translate.java.instance.AnnotationInstances;
import net.atlassian.cmathtutor.domain.persistence.translate.java.instance.PrimitiveInstances;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.ApplicationData;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.CompositeElementEntityData;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.EntityData;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.JavaClassComposer;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.OperationData;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.RepositoryData;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.VariableData;

class JavaClassComposerTest {

    JavaClassComposer javaClassComposer;
    private static VelocityEngine ve;

    @BeforeAll
    static void beforeAll() {
	ve = new VelocityEngine();
	Properties config = new Properties();
	config.put("resource.loaders", "class");
	config.put("resource.loader.class.class",
		"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
	ve.setProperties(config);
	ve.init();
    }

    @BeforeEach
    void init() {
	javaClassComposer = new JavaClassComposer(ve);
    }

    @Test
    void testCreateApplicationClass() {
	StringWriter writer = new StringWriter();
	javaClassComposer.createApplicationClass(new ApplicationData("ua.kpi.iasa.entity", "User"), writer);
	System.out.println(writer.getBuffer());
    }

    @Test
    void testCreateEntityClass() {
	EntityData entityData = new EntityData("Candle", "com.example.demo.entity.composition", "candle");
	VariableData pk = createPkVariable();
	entityData.getFields().add(pk);
	VariableData price = new VariableData(PrimitiveType.INTEGER, "price");
	price.getAnnotations().add(AnnotationInstances.column("price", false, false));
	entityData.getFields().add(price);
	StringWriter writer = new StringWriter();
	javaClassComposer.createEntityClass(entityData, writer);
	System.out.println(writer.getBuffer());
    }

    private VariableData createPkVariable() {
	VariableData pk = new VariableData(PrimitiveType.LONG, "pk");
	pk.getAnnotations().add(AnnotationInstances.id());
	pk.getAnnotations().add(AnnotationInstances.generatedValueIdentity());
	pk.getAnnotations().add(AnnotationInstances.column("PK", false, true));
	return pk;
    }

    @Test
    void when_parentElement_isUndefined_then_CreateCompositeElementEntityClass_shouldThrow_anException() {
	CompositeElementEntityData entityData = new CompositeElementEntityData("Wick",
		"com.example.demo.entity.composition", "wick");
	VariableData pk = createPkVariable();
	entityData.getFields().add(pk);
	VariableData material = new VariableData(PrimitiveType.STRING, "material");
	material.getAnnotations().add(AnnotationInstances.column("material"));
	entityData.getFields().add(material);

	StringWriter writer = new StringWriter();
	assertThrows(MethodInvocationException.class,
		() -> javaClassComposer.createCompositeElementEntityClass(entityData, writer));
//	System.out.println(writer.getBuffer());
    }

    @Test
    void when_parentElement_isDefined_then_CreateCompositeElementEntityClass_should_writeCompositeElementEntity() {
	CompositeElementEntityData entityData = new CompositeElementEntityData("Wick",
		"com.example.demo.entity.composition", "wick");
	VariableData pk = createPkVariable();
	entityData.getFields().add(pk);
	VariableData material = new VariableData(PrimitiveType.STRING, "material");
	material.getAnnotations().add(AnnotationInstances.column("material"));
	entityData.getFields().add(material);
	entityData.selectCompositeParentField("material");

	StringWriter writer = new StringWriter();
	javaClassComposer.createCompositeElementEntityClass(entityData, writer);
	System.out.println(writer.getBuffer());
    }

    @Test
    void testBoth_CreateCompositeElementEntityClass_and_CreateEntityClass() {
	CompositeElementEntityData compositeEntityData = new CompositeElementEntityData("Wick",
		"com.example.demo.entity.composition", "wick");
	EntityData entityData = new EntityData("Candle", "com.example.demo.entity.composition", "candle");
	VariableData pk = createPkVariable();
	entityData.getFields().add(pk);
	VariableData price = new VariableData(PrimitiveType.INTEGER, "price");
	price.getAnnotations().add(AnnotationInstances.column("price", false));
	entityData.getFields().add(price);
	VariableData wick = new VariableData(compositeEntityData, "wick");
	wick.getAnnotations().add(AnnotationInstances.oneToOneBuilder()
		.cascade(CascadeType.ALL)
		.optional(false)
		.build());
	wick.getAnnotations().add(AnnotationInstances.joinColumnBuilder()
		.name("wick_PK")
		.nullable(true)
		.build());
	entityData.getFields().add(wick);
	StringWriter writer = new StringWriter();
	javaClassComposer.createEntityClass(entityData, writer);

	compositeEntityData.getFields().add(createPkVariable());
	VariableData material = new VariableData(PrimitiveType.STRING, "material");
	material.getAnnotations().add(AnnotationInstances.column("material"));
	compositeEntityData.getFields().add(material);
	VariableData candle = new VariableData(entityData, "candle");
	candle.getAnnotations().add(AnnotationInstances.jsonIgnore());
	candle.getAnnotations().add(AnnotationInstances.oneToOneBuilder()
		.cascade(CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH)
		.optional(true)
		.mappedBy("wick")
		.build());
	compositeEntityData.getFields().add(candle);
	compositeEntityData.selectCompositeParentField("candle");

	javaClassComposer.createCompositeElementEntityClass(compositeEntityData, writer);
	System.out.println(writer.getBuffer());
    }

    @Test
    void test_CreateRepositoryInterface() {
	CompositeElementEntityData compositeEntityData = new CompositeElementEntityData("Wick",
		"com.example.demo.entity.composition", "wick");
	StringWriter writer = new StringWriter();
	EntityData entityData = new EntityData("Candle", "com.example.demo.entity.composition", "candle");
	RepositoryData candleRepository = new RepositoryData("CandleRepository",
		"com.example.demo.repository.composition", ClassTypes.crudRepository(entityData));
	RepositoryData wickRepository = new RepositoryData("WickRepository", "com.example.demo.repository.composition",
		ClassTypes.compositeElementRepository(compositeEntityData));

	javaClassComposer.createRepositoryInterface(candleRepository, writer);
	javaClassComposer.createRepositoryInterface(wickRepository, writer);
	System.out.println(writer);
    }

    @Test
    void test_CreateRepositoryInterfaceWithOperation() {
	StringWriter writer = new StringWriter();
	EntityData authorEntity = new EntityData("Author", "com.example.demo.entity", "author");
	EntityData bookEntity = new EntityData("Book", "com.example.demo.entity", "book");
	RepositoryData bookRepository = new RepositoryData("BookRepository",
		"com.example.demo.repository", ClassTypes.crudRepository(bookEntity));
	VariableData authorsVariable = new VariableData(ClassTypes.collection(authorEntity), "authors");
	OperationData findAllByAuthorsInOperation = new OperationData(ClassTypes.collection(bookEntity),
		"findAllByAuthorsIn", Collections.singletonList(authorsVariable));
	bookRepository.getOperations().add(findAllByAuthorsInOperation);

	javaClassComposer.createRepositoryInterface(bookRepository, writer);
	System.out.println(writer);
    }

}
