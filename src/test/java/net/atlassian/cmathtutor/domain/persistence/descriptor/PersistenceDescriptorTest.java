package net.atlassian.cmathtutor.domain.persistence.descriptor;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import net.atlassian.cmathtutor.domain.persistence.model.PersistenceModel;
import net.atlassian.cmathtutor.domain.persistence.model.PersistenceUnitModel;
import net.atlassian.cmathtutor.util.UidUtil;

class PersistenceDescriptorTest {

    @Test
    void testWrapPersistenceUnit() {
	PersistenceModel persistenceModel = new PersistenceModel(UidUtil.getUId());
	PersistenceUnitModel persistenceUnitModel = new PersistenceUnitModel(UidUtil.getUId());
	
    }

}
