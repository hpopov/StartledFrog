package net.atlassian.cmathtutor.fxservice;

import java.io.File;

import javafx.beans.property.ObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.model.Project;
import net.atlassian.cmathtutor.service.ProjectService;

@Slf4j
@AllArgsConstructor
public class LoadStartledFrogProjectService extends Service<Project> {

    private ObjectProperty<File> projectFileProperty;
    private ProjectService projectService;

    @Override
    protected Task<Project> createTask() {
	return new Task<Project>() {

	    @Override
	    protected Project call() throws Exception {
		updateProgress(0, 1);
		Project project = projectService.loadProject(projectFileProperty.get());
		if (project == null) {
		    throw new LoadProjectException("Unable to load the project");
		}
		log.info("Startled Frog project is loaded {}", project);
		updateProgress(1, 1);
		return project;
	    }

	};
    }

}
