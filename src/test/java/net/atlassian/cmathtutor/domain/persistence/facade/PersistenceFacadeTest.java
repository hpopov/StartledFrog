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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.domain.persistence.AggregationKind;
import net.atlassian.cmathtutor.domain.persistence.AttributeArity;
import net.atlassian.cmathtutor.domain.persistence.ConstraintType;
import net.atlassian.cmathtutor.domain.persistence.OwnerType;
import net.atlassian.cmathtutor.domain.persistence.PrimitiveType;
import net.atlassian.cmathtutor.domain.persistence.descriptor.IllegalOperationException;
import net.atlassian.cmathtutor.domain.persistence.descriptor.PersistenceDescriptor;
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
    @Disabled
    @Order(value = 1)
    final void manualTest() {
	boolean success = true;
	createDiplomaTestSet();

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
//
//    private void createTestSet2() {
//	PersistenceUnitDescriptor worldUnitDescriptor = persistenceFacade.persistenceUnitBuilder("  The beau-tiful  word_")
//		.withPrimitiveAttribute()
//			.name("word   age__ _ -")
//			.type(PrimitiveType.BIG_INTEGER)
//			.withConstraint(ConstraintType.NON_NULL)
//			.build()
//		.withPrimitiveAttribute()
//			.name("existed")
//			.type(PrimitiveType.BOOLEAN)
//			.withConstraint(ConstraintType.NON_NULL)
//			.build()
//		.withPrimitiveAttribute()
//			.name("world name")
//			.withConstraint(ConstraintType.UNIQUE)
//			.withConstraint(ConstraintType.UNIQUE)
//			.withConstraint(ConstraintType.NON_NULL)
//			.build()
//		.withRepositoryOperation()
//			.name("findByExistedIsTrue")
//			.build()
//		.build();
//	PersistenceUnitDescriptor humanUnitDescriptor = persistenceFacade.persistenceUnitBuilder("human")
//		.withPrimitiveAttribute()
//			.name("age")
//			.type(PrimitiveType.INTEGER)
//			.build()
//		.build();
//	persistenceFacade.persistenceUnitBuilder("Standalone superman")
//		.build();
//	persistenceFacade.associationBuilder(worldUnitDescriptor, humanUnitDescriptor)
//		.aggregationKind(AggregationKind.COMPOSITE)
//		.containerAttribute()
//			.arity(AttributeArity.AT_LEAST_ZERO)
//			.navigable(true)
//			.name("Inhabitants_human")
//			.build()
//		.elementAttribute()
//			.arity(AttributeArity.ONE_EXACTLY)
//			.navigable(true)
//			.ownerType(OwnerType.CLASSIFIER)
//			.build()
//		.build();
//    }

    private void createDiplomaTestSet() {
	PersistenceUnitDescriptor patientDescriptor = persistenceFacade.persistenceUnitBuilder("Patient")
		.withPrimitiveAttribute()
		.name("User UID")
		.type(PrimitiveType.STRING)
		.withConstraint(ConstraintType.UNIQUE)
		.withConstraint(ConstraintType.NON_NULL)
		.build()
		.withPrimitiveAttribute()
		.name("Full name")
		.type(PrimitiveType.STRING)
		.withConstraint(ConstraintType.NON_NULL)
		.build()
		.withPrimitiveAttribute()
		.name("Birth year")
		.type(PrimitiveType.INTEGER)
		.build()
		.build();

	PersistenceUnitDescriptor medicalCardDescriptor = persistenceFacade.persistenceUnitBuilder("Medical card")
		.withPrimitiveAttribute()
		.name("Creation year")
		.type(PrimitiveType.INTEGER)
		.withConstraint(ConstraintType.NON_NULL)
		.build()
		.build();

	PersistenceUnitDescriptor doctorDescriptor = persistenceFacade.persistenceUnitBuilder("Doctor")
		.withPrimitiveAttribute()
		.name("User UID")
		.type(PrimitiveType.STRING)
		.withConstraint(ConstraintType.UNIQUE)
		.withConstraint(ConstraintType.NON_NULL)
		.build()
		.withPrimitiveAttribute()
		.name("Full name")
		.type(PrimitiveType.STRING)
		.withConstraint(ConstraintType.NON_NULL)
		.build()
		.withPrimitiveAttribute()
		.name("Main specialization")
		.type(PrimitiveType.STRING)
		.withConstraint(ConstraintType.NON_NULL)
		.build()
		.build();

	PersistenceUnitDescriptor entryDescriptor = persistenceFacade.persistenceUnitBuilder("Medical card entry")
		.withPrimitiveAttribute()
		.name("Date")
		.type(PrimitiveType.STRING)
		.withConstraint(ConstraintType.NON_NULL)
		.build()
		.withPrimitiveAttribute()
		.name("Message")
		.type(PrimitiveType.TEXT)
		.build()
		.build();

	PersistenceUnitDescriptor indexDescriptor = persistenceFacade.persistenceUnitBuilder("Health index")
		.withPrimitiveAttribute()
		.name("Name")
		.type(PrimitiveType.STRING)
		.withConstraint(ConstraintType.NON_NULL)
		.build()
		.withPrimitiveAttribute()
		.name("Value")
		.type(PrimitiveType.STRING)
		.withConstraint(ConstraintType.NON_NULL)
		.build()
		.build();

	persistenceFacade.associationBuilder(patientDescriptor, medicalCardDescriptor)
		.aggregationKind(AggregationKind.NONE)
		.containerAttribute()
		.arity(AttributeArity.AT_MOST_ONE)
		.navigable(true)
		.ownerType(OwnerType.ASSOCIATION)
		.build()
		.elementAttribute()
		.arity(AttributeArity.ONE_EXACTLY)
		.navigable(true)
		.ownerType(OwnerType.CLASSIFIER)
		.build()
		.build();

	persistenceFacade.associationBuilder(doctorDescriptor, medicalCardDescriptor)
		.aggregationKind(AggregationKind.SHARED)
		.containerAttribute()
		.arity(AttributeArity.AT_LEAST_ZERO)
		.navigable(true)
		.ownerType(OwnerType.ASSOCIATION)
		.name("Created Medical cards")
		.build()
		.elementAttribute()
		.arity(AttributeArity.AT_MOST_ONE)
		.navigable(true)
		.ownerType(OwnerType.CLASSIFIER)
		.name("Creator")
		.build()
		.build();

	persistenceFacade.associationBuilder(medicalCardDescriptor, entryDescriptor)
		.aggregationKind(AggregationKind.COMPOSITE)
		.containerAttribute()
		.arity(AttributeArity.AT_LEAST_ZERO)
		.navigable(true)
		.ownerType(OwnerType.ASSOCIATION)
		.name("entries")
		.build()
		.elementAttribute()
		.arity(AttributeArity.ONE_EXACTLY)
		.navigable(true)
		.ownerType(OwnerType.CLASSIFIER)
		.build()
		.build();

	persistenceFacade.associationBuilder(entryDescriptor, indexDescriptor)
		.aggregationKind(AggregationKind.COMPOSITE)
		.containerAttribute()
		.arity(AttributeArity.AT_LEAST_ZERO)
		.navigable(true)
		.ownerType(OwnerType.ASSOCIATION)
		.name("Health indices")
		.build()
		.elementAttribute()
		.arity(AttributeArity.ONE_EXACTLY)
		.navigable(true)
		.ownerType(OwnerType.CLASSIFIER)
		.build()
		.build();

	persistenceFacade.associationBuilder(entryDescriptor, doctorDescriptor)
		.aggregationKind(AggregationKind.NONE)
		.containerAttribute()
		.arity(AttributeArity.ONE_EXACTLY)
		.navigable(true)
		.ownerType(OwnerType.CLASSIFIER)
		.name("Entry creator")
		.build()
		.elementAttribute()
		.arity(AttributeArity.AT_LEAST_ZERO)
		.navigable(true)
		.ownerType(OwnerType.ASSOCIATION)
		.name("Created entries")
		.build()
		.build();
    }

    @Test
    @Order(value = 2)
    void manualTestLoad() throws IllegalOperationException {
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
	PersistenceDescriptor.wrap(persistence);

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
    @Disabled
    void when_nameIsInvalid_then_persistenceUnitBuilder_throwsAnException() {
	assertThrows(IllegalArgumentException.class,
		() -> persistenceFacade.persistenceUnitBuilder("some ? invalid имя").build());
    }

    @Test
    @Disabled
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
    @Disabled
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
