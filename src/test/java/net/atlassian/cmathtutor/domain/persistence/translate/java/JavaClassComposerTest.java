package net.atlassian.cmathtutor.domain.persistence.translate.java;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Properties;

import javax.persistence.CascadeType;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.atlassian.cmathtutor.domain.persistence.translate.java.instance.AnnotationInstances;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.Application;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.ContainableEntity;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.Entity;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.JavaClassComposer;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.Operation;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.Repository;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.Variable;

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
	javaClassComposer.createApplicationClass(new Application("ua.kpi.iasa.entity", "User"), writer);
	System.out.println(writer.getBuffer());
    }

    @Test
    void testCreateEntityClass() {
	Entity entity = new Entity("Candle", "com.example.demo.entity.composition", "candle");
	Variable pk = createPkVariable();
	entity.getFields().add(pk);
	Variable price = new Variable(PrimitiveType.INTEGER, "price");
	price.getAnnotations().add(AnnotationInstances.column("price", false, false));
	entity.getFields().add(price);
	StringWriter writer = new StringWriter();
	javaClassComposer.createEntityClass(entity, writer);
	System.out.println(writer.getBuffer());
    }

    private Variable createPkVariable() {
	Variable pk = new Variable(PrimitiveType.LONG, "pk");
	pk.getAnnotations().add(AnnotationInstances.id());
	pk.getAnnotations().add(AnnotationInstances.generatedValueIdentity());
	pk.getAnnotations().add(AnnotationInstances.column("PK", false, true));
	return pk;
    }

    @Test
    void when_parentElement_isUndefined_then_CreateCompositeElementEntityClass_shouldThrow_anException() {
	ContainableEntity entityData = new ContainableEntity("Wick",
		"com.example.demo.entity.composition", "wick");
	Variable pk = createPkVariable();
	entityData.getFields().add(pk);
	Variable material = new Variable(PrimitiveType.STRING, "material");
	material.getAnnotations().add(AnnotationInstances.column("material"));
	entityData.getFields().add(material);

	StringWriter writer = new StringWriter();
	assertThrows(MethodInvocationException.class,
		() -> javaClassComposer.createContainableEntityClass(entityData, writer));
//	System.out.println(writer.getBuffer());
    }

    @Test
    void when_parentElement_isDefined_then_CreateCompositeElementEntityClass_should_writeCompositeElementEntity() {
	ContainableEntity entityData = new ContainableEntity("Wick",
		"com.example.demo.entity.composition", "wick");
	Variable pk = createPkVariable();
	entityData.getFields().add(pk);
	Variable material = new Variable(PrimitiveType.STRING, "material");
	material.getAnnotations().add(AnnotationInstances.column("material"));
	entityData.getFields().add(material);
	entityData.selectCompositeParentField("material");

	StringWriter writer = new StringWriter();
	javaClassComposer.createContainableEntityClass(entityData, writer);
	System.out.println(writer.getBuffer());
    }

    @Test
    void testBoth_CreateCompositeElementEntityClass_and_CreateEntityClass() {
	ContainableEntity compositeEntityData = new ContainableEntity("Wick",
		"com.example.demo.entity.composition", "wick");
	Entity entity = new Entity("Candle", "com.example.demo.entity.composition", "candle");
	Variable pk = createPkVariable();
	entity.getFields().add(pk);
	Variable price = new Variable(PrimitiveType.INTEGER, "price");
	price.getAnnotations().add(AnnotationInstances.column("price", false));
	entity.getFields().add(price);
	Variable wick = new Variable(compositeEntityData, "wick");
	wick.getAnnotations().add(AnnotationInstances.oneToOneBuilder()
		.cascade(CascadeType.ALL)
		.optional(false)
		.build());
	wick.getAnnotations().add(AnnotationInstances.joinColumnBuilder()
		.name("wick_PK")
		.nullable(true)
		.build());
	entity.getFields().add(wick);
	StringWriter writer = new StringWriter();
	javaClassComposer.createEntityClass(entity, writer);

	compositeEntityData.getFields().add(createPkVariable());
	Variable material = new Variable(PrimitiveType.STRING, "material");
	material.getAnnotations().add(AnnotationInstances.column("material"));
	compositeEntityData.getFields().add(material);
	Variable candle = new Variable(entity, "candle");
	candle.getAnnotations().add(AnnotationInstances.jsonIgnore());
	candle.getAnnotations().add(AnnotationInstances.oneToOneBuilder()
		.cascade(CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH)
		.optional(true)
		.mappedBy("wick")
		.build());
	compositeEntityData.getFields().add(candle);
	compositeEntityData.selectCompositeParentField("candle");

	javaClassComposer.createContainableEntityClass(compositeEntityData, writer);
	System.out.println(writer.getBuffer());
    }

    @Test
    void test_CreateRepositoryInterface() {
	ContainableEntity compositeEntityData = new ContainableEntity("Wick",
		"com.example.demo.entity.composition", "wick");
	StringWriter writer = new StringWriter();
	Entity entity = new Entity("Candle", "com.example.demo.entity.composition", "candle");
	Repository candleRepository = new Repository("CandleRepository",
		"com.example.demo.repository.composition", ClassTypes.crudRepository(entity));
	Repository wickRepository = new Repository("WickRepository", "com.example.demo.repository.composition",
		ClassTypes.containableRepository(compositeEntityData));

	javaClassComposer.createRepositoryInterface(candleRepository, writer);
	javaClassComposer.createRepositoryInterface(wickRepository, writer);
	System.out.println(writer);
    }

    @Test
    void test_CreateRepositoryInterfaceWithOperation() {
	StringWriter writer = new StringWriter();
	Entity authorEntity = new Entity("Author", "com.example.demo.entity", "author");
	Entity bookEntity = new Entity("Book", "com.example.demo.entity", "book");
	Repository bookRepository = new Repository("BookRepository",
		"com.example.demo.repository", ClassTypes.crudRepository(bookEntity));
	Variable authorsVariable = new Variable(ClassTypes.collection(authorEntity), "authors");
	Operation findAllByAuthorsInOperation = new Operation(ClassTypes.collection(bookEntity),
		"findAllByAuthorsIn", Collections.singletonList(authorsVariable));
	bookRepository.getOperations().add(findAllByAuthorsInOperation);

	javaClassComposer.createRepositoryInterface(bookRepository, writer);
	System.out.println(writer);
    }

}
