package net.atlassian.cmathtutor.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.velocity.app.VelocityEngine;

import de.fxdiagram.core.XDiagram;
import de.fxdiagram.core.XRoot;
import de.fxdiagram.core.model.DomainObjectProvider;
import de.fxdiagram.core.model.ModelLoad;
import de.fxdiagram.core.model.ModelSave;
import javafx.collections.ObservableList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.domain.persistence.descriptor.PersistenceDescriptor;
import net.atlassian.cmathtutor.domain.persistence.model.AbstractAttributeModel;
import net.atlassian.cmathtutor.domain.persistence.model.AssociationModel;
import net.atlassian.cmathtutor.domain.persistence.model.PersistenceModel;
import net.atlassian.cmathtutor.domain.persistence.model.PersistenceUnitModel;
import net.atlassian.cmathtutor.domain.persistence.model.PrimitiveAttributeModel;
import net.atlassian.cmathtutor.domain.persistence.model.ReferentialAttributeModel;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.DatabaseChangeLog;
import net.atlassian.cmathtutor.domain.persistence.translate.java.Named;
import net.atlassian.cmathtutor.domain.persistence.translate.java.Packaged;
import net.atlassian.cmathtutor.domain.persistence.translate.java.translator.TranslatedClassesData;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.Application;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.ContainableEntity;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.Entity;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.JavaClassComposer;
import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.Repository;
import net.atlassian.cmathtutor.fxdiagram.StartledFrogDiagram;
import net.atlassian.cmathtutor.helper.VelocityEngineConfig;
import net.atlassian.cmathtutor.model.Project;
import net.atlassian.cmathtutor.util.FileUtil;
import net.atlassian.cmathtutor.util.UidUtil;

@Slf4j
public class PersistenceDomainService {

    private static final String PERSISTENCE_DIAGRAM_FILE_NAME = "diagram.fxd";
    private static final String INVALID_PACKAGE_MSG = "Default %1$ package for project {} is {}, but got %1$ with package {}";
    private static final String DOT_JAVA = ".java";
    private static final String ENTITY_SUBPACKAGE = "entity";
    private static final String REPOSITORY_SUBPACKAGE = "repository";
    private static final char DOT = '.';
    private static final String PERSISTENCE_MODEL_FILE_NAME = "persistence.model";
    private static final String LIQUIBASE_SCHEMA_LOCATION = "http://www.liquibase.org/xml/ns/dbchangelog "
            + "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd";
    private static final String CHANGELOG_MASTER_XML = "db/changelog/changelog-master.xml";

    public static String resolveEntityBasePackage(Project project) {
        return project.getRootPackage() + DOT + ENTITY_SUBPACKAGE;
    }

    public static String resolveRepositoryBasePackage(Project project) {
        return project.getRootPackage() + DOT + REPOSITORY_SUBPACKAGE;
    }

    private JavaClassComposer javaClassComposer;

    @Inject
    private ProjectService projectService;

    public PersistenceDomainService() {
        VelocityEngine ve = new VelocityEngine(new VelocityEngineConfig());
        javaClassComposer = new JavaClassComposer(ve);
    }

    public PersistenceModel initializeNewPersistenceModel() {
        PersistenceModel persistenceModel = new PersistenceModel(UidUtil.getUId());
        return persistenceModel;
    }

    public void persistModel(@NonNull PersistenceModel persistenceModel) {
        File persistenceModelFile = getPersistenceModelFile();
        try {
            JAXBContext context = JAXBContext.newInstance(PersistenceModel.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(persistenceModel, persistenceModelFile);
        } catch (JAXBException e) {
            log.error("Unable to persist persistence model using JAXB", e);
            return;
        }
    }

    private File getPersistenceModelFile() {
        return projectService.getCurrentStartledFrogProjectFolderPath()
                .resolve(PERSISTENCE_MODEL_FILE_NAME).toFile();
    }

    public PersistenceModel loadPersistenceModel() {
        File file = getPersistenceModelFile();
        FileReader fileReader;
        try {
            fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            log.info("Unable to load global configuration model: file {} is not found", file);
            return null;
        }
        PersistenceModel persistenceModel;
        try {
            JAXBContext context = JAXBContext.newInstance(PersistenceModel.class);
            persistenceModel = (PersistenceModel) context.createUnmarshaller().unmarshal(fileReader);
        } catch (JAXBException e) {
            log.error("Unable to load persistence model using JAXB", e);
            return null;
        }
        populateBackwardReferences(persistenceModel);
        return persistenceModel;
    }

    private void populateBackwardReferences(PersistenceModel persistenceModel) {
        Collection<PersistenceUnitModel> persistenceUnitsSnapshot = new ArrayList<>(
                persistenceModel.getPersistenceUnits());
        Collection<AssociationModel> associationsSnapshot = new ArrayList<>(persistenceModel.getAssociations());

        persistenceModel.getPersistenceUnits().clear();
        persistenceModel.getAssociations().clear();
        associationsSnapshot.forEach(a -> {
            populateBackwardReferences(a);
            a.setPersistence(persistenceModel);
        });
        persistenceUnitsSnapshot.forEach(pu -> {
            populateBackwardReferences(pu);
            pu.setPersistence(persistenceModel);
        });
        persistenceModel.getPersistenceUnits().addAll(persistenceUnitsSnapshot);
        persistenceModel.getAssociations().addAll(associationsSnapshot);
    }

    private void populateBackwardReferences(PersistenceUnitModel persistenceUnitModel) {
        Consumer<AbstractAttributeModel> attributeParentClassifierPropagator = a -> a
                .setParentClassifier(persistenceUnitModel);
        Collection<PrimitiveAttributeModel> primitiveAttributesSnapshot = new ArrayList<>(
                persistenceUnitModel.getPrimitiveAttributes());
        Collection<ReferentialAttributeModel> referentialAttributesSnapshot = new ArrayList<>(
                persistenceUnitModel.getReferentialAttributes());

        persistenceUnitModel.getPrimitiveAttributes().clear();
        persistenceUnitModel.getReferentialAttributes().clear();
        primitiveAttributesSnapshot.forEach(attributeParentClassifierPropagator);
        referentialAttributesSnapshot.forEach(attributeParentClassifierPropagator);
        persistenceUnitModel.getPrimitiveAttributes().addAll(primitiveAttributesSnapshot);
        persistenceUnitModel.getReferentialAttributes().addAll(referentialAttributesSnapshot);
    }

    private void populateBackwardReferences(AssociationModel associationModel) {
        associationModel.getContainerAttribute().setAssociation(associationModel);
        associationModel.getElementAttribute().setAssociation(associationModel);
    }

    public void persistLiquibaseChangeLog(@NonNull DatabaseChangeLog changeLog) {
        Path resourceFolderPath = projectService.getCurrentProjectResourceFolderPath();
        try {
            JAXBContext context = JAXBContext.newInstance(DatabaseChangeLog.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, LIQUIBASE_SCHEMA_LOCATION);
            marshaller.marshal(changeLog, resourceFolderPath.resolve(CHANGELOG_MASTER_XML).toFile());
        } catch (JAXBException e) {
            log.error("Unable to persist liquibase changelog using JAXB", e);
            return;
        }
    }

    public void rewriteTranslatedClasses(@NonNull TranslatedClassesData translatedClasses) {
        Project currentProject = projectService.getCurrentProject();
        Path javaSourcesPath = projectService.getCurrentProjectJavaFolderPath();
        FileUtil.emptyDirectory(javaSourcesPath.toFile());

        String rootPackageSubpath = currentProject.getRootPackage().replace(DOT, File.separatorChar);
        Path rootPackagePath = javaSourcesPath.resolve(rootPackageSubpath);
        FileUtil.createDirectory(rootPackagePath.toFile());
        Application application = translatedClasses.getTranslatedApplication();
        assertPackageNameIsCorrectFor(DeclarationType.APPLICATION, currentProject, currentProject.getRootPackage(),
                application);
        try (Writer writer = new FileWriter(resolveJavaFile(rootPackagePath, application))) {
            javaClassComposer.createApplicationClass(application, writer);
        } catch (IOException e) {
            log.error("Unable to compose application class {}", application.getName(), e);
        }

        String defaultEntityPackage = resolveEntityBasePackage(currentProject);
        Path entityPackagePath = rootPackagePath.resolve(ENTITY_SUBPACKAGE);
        FileUtil.createDirectory(entityPackagePath.toFile());
        for (Entity entity : translatedClasses.getTranslatedEntities()) {
            assertPackageNameIsCorrectFor(DeclarationType.ENTITY, currentProject, defaultEntityPackage, entity);
            try (Writer writer = new FileWriter(resolveJavaFile(entityPackagePath, entity))) {
                javaClassComposer.createEntityClass(entity, writer);
            } catch (IOException e) {
                log.error("Unable to compose entity {}", entity.getName(), e);
            }
        }
        for (ContainableEntity entity : translatedClasses.getTranslatedContainableEntities()) {
            assertPackageNameIsCorrectFor(DeclarationType.ENTITY, currentProject, defaultEntityPackage, entity);
            try (Writer writer = new FileWriter(resolveJavaFile(entityPackagePath, entity))) {
                javaClassComposer.createContainableEntityClass(entity, writer);
            } catch (IOException e) {
                log.error("Unable to compose containable entity {}", entity.getName(), e);
            }
        }

        String defaultRepositoryPackage = resolveRepositoryBasePackage(currentProject);
        Path repositoryPackagePath = rootPackagePath.resolve(REPOSITORY_SUBPACKAGE);
        FileUtil.createDirectory(repositoryPackagePath.toFile());
        for (Repository repository : translatedClasses.getTranslatedRepositories()) {
            assertPackageNameIsCorrectFor(DeclarationType.REPOSITORY, currentProject, defaultRepositoryPackage,
                    repository);
            try (Writer writer = new FileWriter(resolveJavaFile(repositoryPackagePath, repository))) {
                javaClassComposer.createRepositoryInterface(repository, writer);
            } catch (IOException e) {
                log.error("Unable to compose repository {} due to IOException {}", repository.getName(), e);
            }
        }
    }

    private void assertPackageNameIsCorrectFor(
            DeclarationType classType, Project currentProject,
            String validPackageName, Packaged packaged
    ) {
        if (false == packaged.getPackageName().equals(validPackageName)) {
            log.error(String.format(INVALID_PACKAGE_MSG, classType.getName()), currentProject.getApplicationName(),
                    validPackageName, packaged.getPackageName());
            throw new IllegalArgumentException(classType + " package for current project must be " + validPackageName);
        }
    }

    private File resolveJavaFile(Path parentPackagePath, Named named) throws IOException {
        File javaFile = parentPackagePath.resolve(named.getName() + DOT_JAVA).toFile();
        javaFile.createNewFile();
        return javaFile;
    }

    @Getter
    @AllArgsConstructor
    private static enum DeclarationType {

        APPLICATION("application"),
        ENTITY("entity"),
        REPOSITORY("repository");

        private String name;
    }

    public StartledFrogDiagram getProjectDiagram(XRoot root, PersistenceDescriptor persistenceDescriptor) {
        File file = getPersistenceDiagramFile();
        if (file.exists()) {
            return loadDiagramFromFile(root, persistenceDescriptor, file);
        } else {
            log.warn("FXDiagram file does not exist. Creating entirely new diagram..");
            return createNewEmptyDiagram(root, persistenceDescriptor, file);
        }
    }

    private File getPersistenceDiagramFile() {
        return projectService.getCurrentStartledFrogProjectFolderPath().resolve(PERSISTENCE_DIAGRAM_FILE_NAME).toFile();
    }

    private StartledFrogDiagram loadDiagramFromFile(
            XRoot root, PersistenceDescriptor persistenceDescriptor,
            File file
    ) {
        Object node = loadDiagram(file);
        if (false == node instanceof XRoot) {
            throw new IllegalStateException("Loaded diagram node must be successor of XRoot!");
        }
        ObservableList<DomainObjectProvider> domainObjectProviders = ((XRoot) node).getDomainObjectProviders();
        root.replaceDomainObjectProviders(domainObjectProviders);
        XDiagram diagram = ((XRoot) node).getDiagram();
        if (false == diagram instanceof StartledFrogDiagram) {
            throw new IllegalStateException("Loaded diagram must be startledFrog diagram!");
        }
        StartledFrogDiagram frogDiagram = (StartledFrogDiagram) diagram;
        String persistenceDescriptorId = frogDiagram.getPersistenceDescriptorId();
        if (false == persistenceDescriptor.getId().equals(persistenceDescriptorId)) {
            throw new IllegalStateException("loaded descriptor id and model id must be equal!");
        }
        frogDiagram.setPersistenceDescriptor(persistenceDescriptor);
        root.setRootDiagram(frogDiagram);
        String path = file.getPath();
        root.setFileName(path);
        return frogDiagram;
    }

    private Object loadDiagram(@NonNull File file) {
        ModelLoad modelLoad = new ModelLoad();
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            return modelLoad.load(inputStreamReader);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private StartledFrogDiagram createNewEmptyDiagram(
            XRoot root, PersistenceDescriptor persistenceDescriptor,
            File file
    ) {
        StartledFrogDiagram diagram;
        diagram = new StartledFrogDiagram();
        diagram.setPersistenceDescriptor(persistenceDescriptor);
        root.setRootDiagram(diagram);

        OutputStreamWriter outputStreamWriter;
        try {
            FileOutputStream _fileOutputStream = new FileOutputStream(file);
            outputStreamWriter = new OutputStreamWriter(_fileOutputStream, "UTF-8");
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            log.error("Unable to save new diagrm file: ", e);
            return diagram;
        }
        ModelSave modelSave = new ModelSave();
        modelSave.save(root, outputStreamWriter);
        root.setFileName(file.getPath());
        root.setNeedsSave(false);

        return diagram;
    }
}
