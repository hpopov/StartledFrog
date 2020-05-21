package net.atlassian.cmathtutor.domain.persistence.facade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import net.atlassian.cmathtutor.domain.persistence.AggregationKind;
import net.atlassian.cmathtutor.domain.persistence.PrimitiveType;
import net.atlassian.cmathtutor.domain.persistence.descriptor.PersistenceUnitDescriptor;

class PersistenceFacadeTest {

    PersistenceFacade persistenceFacade;

    @BeforeEach
    void setup() {
	persistenceFacade = new PersistenceFacade();
    }

    @Test
    final void manualTest() {
	PersistenceUnitDescriptor aPersistenceUnitDescriptor = persistenceFacade.persistenceUnitBuilder("A")
		.withPrimitiveAttribute()
		.name("name")
		.build()
		.withRepositoryOperation()
		.name("findByNameIs")
		.build()
		.build();
	PersistenceUnitDescriptor bPersistenceUnitDescriptor = persistenceFacade.persistenceUnitBuilder("b")
		.withPrimitiveAttribute()
		.name("age")
		.type(PrimitiveType.INTEGER)
		.build()
		.build();
	persistenceFacade.associationBuilder(aPersistenceUnitDescriptor, bPersistenceUnitDescriptor)
		.aggregationKind(AggregationKind.COMPOSITE)
		.containerAttribute().build()
		.elementAttribute().build()
		.build();
	System.out.println(persistenceFacade.getWrappedPersistence());
	assertTrue(true);
    }

}
