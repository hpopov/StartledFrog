package net.atlassian.cmathtutor.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.Version;
import net.atlassian.cmathtutor.domain.persistence.translate.changelog.DatabaseChangeLog;
import net.atlassian.cmathtutor.model.Project;

@Slf4j
@Getter
public class ProjectService {
    private static final String SF_PROJECT_FILENAME = ".startled-frog";
    private static final String SF_PROJECT_FOLDER_NAME = ".sf-project";
    private static final Version CURRENT_VERSION = Version.V_0_0_1_a;
    private static final String CHANGELOG_MASTER_XML = "src/main/resources/db/changelog/changelog-master.xml";
    public static final String CURRENT_DEPENDENCY_VERSION = "0.0.1a";

    private Project currentProject;

    public void setCurrentProject(@NonNull Project project) {
	currentProject = project;
    }

    public void persistCurrentProject() {
	assertCurrentProjectIsNotNull();
	currentProject.setStartledFrogProjectFolder(SF_PROJECT_FOLDER_NAME);
	currentProject.setVersion(CURRENT_VERSION);
	persistProject(currentProject);
    }

    private void assertCurrentProjectIsNotNull() {
	if (currentProject == null) {
	    throw new NullPointerException("ProjectService does not contain currentProject");
	}
    }

    private void persistProject(Project project) {
	try {
	    JAXBContext context = JAXBContext.newInstance(Project.class);
	    Marshaller marshaller = context.createMarshaller();
	    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	    marshaller.marshal(project, project.getProjectFolder().toPath().resolve(SF_PROJECT_FILENAME).toFile());
	} catch (JAXBException e) {
	    log.error("Unable to persist the project using JAXB", e);
	    return;
	}
	project.getProjectFolder().toPath().resolve(SF_PROJECT_FOLDER_NAME).toFile().mkdir();
    }

    public Project loadProject(@NonNull File projectFile) {
	Project project = null;
	try {
	    JAXBContext context = JAXBContext.newInstance(Project.class);
	    project = (Project) context.createUnmarshaller().unmarshal(new FileReader(projectFile));
	} catch (JAXBException | FileNotFoundException e) {
	    log.error("Unable to load the project using JAXB", e);
	    return project;
	}
	project.setProjectFolder(projectFile.getParentFile());
	return project;
    }

    public String getProjectExtension() {
	return SF_PROJECT_FILENAME;
    }

    public void persistLiquibaseChangeLog(DatabaseChangeLog changeLog) {
	assertCurrentProjectIsNotNull();
	try {
	    JAXBContext context = JAXBContext.newInstance(DatabaseChangeLog.class);
	    Marshaller marshaller = context.createMarshaller();
	    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	    marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,
		    "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd");
	    marshaller.marshal(changeLog,
		    currentProject.getProjectFolder().toPath().resolve(CHANGELOG_MASTER_XML).toFile());
	} catch (JAXBException e) {
	    log.error("Unable to persist the liquibase changelog using JAXB", e);
	    return;
	}
    }

}
