package net.atlassian.cmathtutor.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.velocity.app.VelocityEngine;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.domain.configuration.model.GlobalConfigurationModel;
import net.atlassian.cmathtutor.domain.configuration.translate.PropertiesComposer;
import net.atlassian.cmathtutor.helper.VelocityEngineConfig;
import net.atlassian.cmathtutor.model.Project;

@Slf4j
public class ConfigurationDomainService {

    private static final String DEFAULT_DOCKER_IP = "192.168.99.100";
    private static final String LIQUIBASE_PROPERTIES = "liquibase.properties";
    private static final String DOCKER_COMPOSE_YAML = "docker-compose.yaml";
    private static final String APP_PROPERTIES = "application.properties";
    private static final String DEFAULT_USER = "startled-frog-user";
    private static final String DEFAULT_ROOT_PASSWORD = "root";
    private static final String DEFAULT_PASSWORD = "password";
    private static final String DEFAULT_DB_NAME = "appdb";
    private static final String DEFAULT_JDBC_DRIVER_PATH = "C:/Users/Hryhorii_Popov/.m2/repository/mysql/"
            + "mysql-connector-java/8.0.17/mysql-connector-java-8.0.17.jar";
    private static final String CONFIGURATION_MODEL_FILE_NAME = "configuration.model";

    @Inject
    private ProjectService projectService;

    private PropertiesComposer propertiesComposer;

    public ConfigurationDomainService() {
        VelocityEngine ve = new VelocityEngine(new VelocityEngineConfig());
        propertiesComposer = new PropertiesComposer(ve);
    }

    public GlobalConfigurationModel initializeDefaultConfigurationModel() {
        Project currentProject = projectService.getCurrentProject();
        GlobalConfigurationModel configurationModel = GlobalConfigurationModel.builder()
                .database(DEFAULT_DB_NAME)
                .jdbcDriverPath(DEFAULT_JDBC_DRIVER_PATH)
                .password(DEFAULT_PASSWORD)
                .project(currentProject)
                .rootPassword(DEFAULT_ROOT_PASSWORD)
                .user(DEFAULT_USER)
                .dockerMachineIp(DEFAULT_DOCKER_IP)
                .build();
        return configurationModel;
    }

    public void persistModel(@NonNull GlobalConfigurationModel configurationModel) {
        File globalConfigurationModelFile = getGlobalConfigurationModelFile();
        try {
            JAXBContext context = JAXBContext.newInstance(GlobalConfigurationModel.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(configurationModel, globalConfigurationModelFile);
        } catch (JAXBException e) {
            log.error("Unable to persist global configuration model using JAXB", e);
            return;
        }
    }

    private File getGlobalConfigurationModelFile() {
        return projectService.getCurrentStartledFrogProjectFolderPath()
                .resolve(CONFIGURATION_MODEL_FILE_NAME).toFile();
    }

    public GlobalConfigurationModel loadConfigurationModel() {
        File file = getGlobalConfigurationModelFile();
        GlobalConfigurationModel configurationModel;
        FileReader fileReader;
        try {
            fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            log.info("Unable to load global configuration model: file {} is not found", file);
            return null;
        }
        try {
            JAXBContext context = JAXBContext.newInstance(GlobalConfigurationModel.class);
            configurationModel = (GlobalConfigurationModel) context.createUnmarshaller()
                    .unmarshal(fileReader);
        } catch (JAXBException e) {
            log.error("Unable to load global configuration model using JAXB", e);
            return null;
        }
        configurationModel.setProject(projectService.getCurrentProject());
        return configurationModel;
    }

    public void rewriteConfigurationProperties(@NonNull GlobalConfigurationModel configurationModel) {
        Path resourceFolderPath = projectService.getCurrentProjectResourceFolderPath();
        Project currentProject = projectService.getCurrentProject();
        try (Writer writer = new FileWriter(resourceFolderPath.resolve(APP_PROPERTIES).toFile())) {
            propertiesComposer.composeApplicationProperties(writer, configurationModel);
        } catch (IOException e) {
            log.error("Unable to compose application properties for project {} due to {}",
                    currentProject.getApplicationName(), e);
        }

        try (Writer writer = new FileWriter(
                currentProject.getProjectFolder().toPath().resolve(DOCKER_COMPOSE_YAML).toFile())) {
            propertiesComposer.composeDockerYaml(writer, configurationModel);
        } catch (IOException e) {
            log.error("Unable to compose docker-compose.yaml for project {} due to {}",
                    currentProject.getApplicationName(), e);
        }

        try (Writer writer = new FileWriter(resourceFolderPath.resolve(LIQUIBASE_PROPERTIES).toFile())) {
            propertiesComposer.composeLiquibaseProperties(writer, configurationModel);
        } catch (IOException e) {
            log.error("Unable to compose application properties for project {} due to {}",
                    currentProject.getApplicationName(), e);
        }
    }
}
