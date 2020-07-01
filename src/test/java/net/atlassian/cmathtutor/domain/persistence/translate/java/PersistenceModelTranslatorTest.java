package net.atlassian.cmathtutor.domain.persistence.translate.java;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.ProjectBuildFramework;
import net.atlassian.cmathtutor.domain.persistence.Persistence;
import net.atlassian.cmathtutor.domain.persistence.facade.PersistenceFacade;
import net.atlassian.cmathtutor.domain.persistence.translate.PersistenceModelTranslator;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.DatabaseChangeLog;
import net.atlassian.cmathtutor.domain.persistence.translate.java.translator.TranslatedClassesData;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.ContainableEntity;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.Entity;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.JavaClassComposer;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.Repository;
import net.atlassian.cmathtutor.model.Project;

@Slf4j
class PersistenceModelTranslatorTest {

    private static final File FILE_2 = new File(
            "C:/Users/Hryhorii_Popov/Data/Other/FullStack/StartledFrog/manualTest2.xml");
    private static final File LIQUIBASE_FILE = new File(
            "C:/Users/Hryhorii_Popov/Data/Other/FullStack/StartledFrog/liquibase2.xml");

    private static VelocityEngine ve;
    private Project project;
    private PersistenceModelTranslator persistenceModelTranslator;

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
        project = Project.builder()
                .applicationName("Retina")
                .projectBuildFramework(ProjectBuildFramework.MAVEN)
                .rootPackage("some.root.package")
                .build();
        persistenceModelTranslator = new PersistenceModelTranslator(project);
    }

    @Test
    final void test() {
        Persistence persistence = PersistenceFacade.loadFromFile(FILE_2).getWrappedPersistence();
        persistenceModelTranslator.translate(persistence);
        JavaClassComposer javaClassComposer = new JavaClassComposer(ve);
        TranslatedClassesData translatedClasses = persistenceModelTranslator.getTranslatedClasses();

        StringWriter writer = new StringWriter();
        javaClassComposer.createApplicationClass(translatedClasses.getTranslatedApplication(), writer);
        for (ContainableEntity ce : translatedClasses.getTranslatedContainableEntities()) {
            javaClassComposer.createContainableEntityClass(ce, writer);
        }
        for (Entity e : translatedClasses.getTranslatedEntities()) {
            javaClassComposer.createEntityClass(e, writer);
        }
        for (Repository r : translatedClasses.getTranslatedRepositories()) {
            javaClassComposer.createRepositoryInterface(r, writer);
        }
        System.out.println(writer.getBuffer());
    }

    @Test
    final void testLiquibaseChangeLog() {
        boolean success = true;
        Persistence persistence = PersistenceFacade.loadFromFile(FILE_2).getWrappedPersistence();
        persistenceModelTranslator.translate(persistence);
        DatabaseChangeLog databaseChangeLog = persistenceModelTranslator.getTranslatedChangeLog();

        try {
            JAXBContext context = JAXBContext.newInstance(DatabaseChangeLog.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,
                    "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd");
            marshaller.marshal(databaseChangeLog, LIQUIBASE_FILE);
        } catch (JAXBException e) {
            log.error("Unable to persist changelog model using JAXB", e);
            success = false;
        }
        assertTrue(success);
    }
}
