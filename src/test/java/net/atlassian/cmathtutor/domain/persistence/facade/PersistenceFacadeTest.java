package net.atlassian.cmathtutor.domain.persistence.facade;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.domain.persistence.AggregationKind;
import net.atlassian.cmathtutor.domain.persistence.AttributeArity;
import net.atlassian.cmathtutor.domain.persistence.ConstraintType;
import net.atlassian.cmathtutor.domain.persistence.OwnerType;
import net.atlassian.cmathtutor.domain.persistence.PrimitiveType;
import net.atlassian.cmathtutor.domain.persistence.descriptor.PersistenceUnitDescriptor;
import net.atlassian.cmathtutor.domain.persistence.model.PersistenceModel;

@Slf4j
class PersistenceFacadeTest {

    private static final File FILE_1 = new File(
	    "C:/Users/Hryhorii_Popov/Data/Other/FullStack/StartledFrog/manualTest1.xml");
    private static final File FILE_2 = new File(
	    "C:/Users/Hryhorii_Popov/Data/Other/FullStack/StartledFrog/manualTest2.xml");
    PersistenceFacade persistenceFacade;

    @BeforeEach
    void setup() {
	persistenceFacade = PersistenceFacade.newInstance();
    }

    @Test
    @Order(value = 1)
    final void manualTest() {
	boolean success = true;
	createTestSet2();

	try {
	    JAXBContext context = JAXBContext.newInstance(PersistenceModel.class);
	    Marshaller marshaller = context.createMarshaller();
	    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	    marshaller.marshal(persistenceFacade.getWrappedPersistence(), FILE_1);
	} catch (JAXBException e) {
	    log.error("Unable to persist persistence model using JAXB", e);
	    success = false;
	}

	assertTrue(success);
    }

//    private void createTestSet1() {
//	PersistenceUnitDescriptor aPersistenceUnitDescriptor = persistenceFacade.persistenceUnitBuilder("A")
//		.withPrimitiveAttribute()
//		.name("name")
//		.build()
//		.withRepositoryOperation()
//		.name("findByNameIs")
//		.build()
//		.build();
//	PersistenceUnitDescriptor bPersistenceUnitDescriptor = persistenceFacade.persistenceUnitBuilder("b")
//		.withPrimitiveAttribute()
//		.name("age")
//		.type(PrimitiveType.INTEGER)
//		.build()
//		.build();
//	persistenceFacade.associationBuilder(aPersistenceUnitDescriptor, bPersistenceUnitDescriptor)
//		.aggregationKind(AggregationKind.COMPOSITE)
//		.containerAttribute().build()
//		.elementAttribute().build()
//		.build();
//    }

    private void createTestSet2() {
	PersistenceUnitDescriptor worldUnitDescriptor = persistenceFacade.persistenceUnitBuilder("  The beau-tiful  word_")
		.withPrimitiveAttribute()
			.name("word   age__ _ -")
			.type(PrimitiveType.BIG_INTEGER)
			.withConstraint(ConstraintType.NON_NULL)
			.build()
		.withPrimitiveAttribute()
			.name("existed")
			.type(PrimitiveType.BOOLEAN)
			.withConstraint(ConstraintType.NON_NULL)
			.build()
		.withPrimitiveAttribute()
			.name("world name")
			.withConstraint(ConstraintType.UNIQUE)
			.withConstraint(ConstraintType.UNIQUE)
			.withConstraint(ConstraintType.NON_NULL)
			.build()
		.withRepositoryOperation()
			.name("findByExistedIsTrue")
			.build()
		.build();
	PersistenceUnitDescriptor humanUnitDescriptor = persistenceFacade.persistenceUnitBuilder("human")
		.withPrimitiveAttribute()
			.name("age")
			.type(PrimitiveType.INTEGER)
			.build()
		.build();
	persistenceFacade.persistenceUnitBuilder("Standalone superman")
		.build();
	persistenceFacade.associationBuilder(worldUnitDescriptor, humanUnitDescriptor)
		.aggregationKind(AggregationKind.COMPOSITE)
		.containerAttribute()
			.arity(AttributeArity.AT_LEAST_ZERO)
			.navigable(true)
			.name("Inhabitants_human")
			.build()
		.elementAttribute()
			.arity(AttributeArity.ONE_EXACTLY)
			.navigable(true)
			.ownerType(OwnerType.CLASSIFIER)
			.build()
		.build();
    }
    
    @Test
    @Order(value = 2)
    void manualTestLoad() {
	boolean success = true;
	PersistenceModel persistence = null;
	try {
	    JAXBContext context = JAXBContext.newInstance(PersistenceModel.class);
	    persistence = (PersistenceModel) context.createUnmarshaller().unmarshal(new FileReader(FILE_1));
	} catch (JAXBException | FileNotFoundException e) {
	    log.error("Unable to load persistence model using JAXB", e);
	    success = false;
	}

	assertTrue(success);

	try {
	    JAXBContext context = JAXBContext.newInstance(PersistenceModel.class);
	    Marshaller marshaller = context.createMarshaller();
	    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	    marshaller.marshal(persistence, FILE_2);
	} catch (JAXBException e) {
	    log.error("Unable to persist persistence model using JAXB", e);
	    success = false;
	}

	assertTrue(success);
    }

    @Test
    void when_nameIsInvalid_then_persistenceUnitBuilder_throwsAnException() {
	assertThrows(IllegalArgumentException.class,
		() -> persistenceFacade.persistenceUnitBuilder("some ? invalid имя").build());
    }
    
    @Test
    void when_cycleAggregationIsAdded_then_associationBuilder_throwsAnException() {
	assertThrows(RuntimeException.class, () -> {
		PersistenceUnitDescriptor supermanUnitDescriptor = persistenceFacade
			.persistenceUnitBuilder("Standalone superman")
			.build();
		persistenceFacade.associationBuilder(supermanUnitDescriptor, supermanUnitDescriptor)
			.aggregationKind(AggregationKind.SHARED)
			.containerAttribute()
				.arity(AttributeArity.AT_LEAST_ZERO)
				.navigable(true)
				.name("Inhabitants_human")
				.build()
			.elementAttribute()
				.arity(AttributeArity.AT_LEAST_ZERO)
				.navigable(true)
				.ownerType(OwnerType.CLASSIFIER)
				.build()
			.build();
	});
    }
    
    @Test
    void when_secondAggregationIsAddedToElement_then_associationBuilder_throwsAnException() {
	PersistenceUnitDescriptor supermanUnitDescriptor = persistenceFacade
		.persistenceUnitBuilder("Standalone superman")
		.build();
	PersistenceUnitDescriptor anotherUnitDescriptor = persistenceFacade
		.persistenceUnitBuilder("superman")
		.build();
	PersistenceUnitDescriptor thirdUnitDescriptor = persistenceFacade
		.persistenceUnitBuilder("third")
		.build();
	persistenceFacade.associationBuilder(supermanUnitDescriptor, anotherUnitDescriptor)
		.aggregationKind(AggregationKind.SHARED)
		.containerAttribute()
			.build()
		.elementAttribute()
			.build()
		.build();
	assertThrows(RuntimeException.class, () -> {
	    persistenceFacade.associationBuilder(thirdUnitDescriptor, anotherUnitDescriptor)
		.aggregationKind(AggregationKind.COMPOSITE)
		.containerAttribute()
			.build()
		.elementAttribute()
			.build()
		.build();
	});
    }

}
