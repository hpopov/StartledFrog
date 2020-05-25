package net.atlassian.cmathtutor.domain.persistence.translate.java.translator;

import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.ApplicationData;
import net.atlassian.cmathtutor.model.Project;

public class ProjectToApplicationDataTranslator {

    private static final String APPLICATION = "Application";

    public ApplicationData translate(Project project) {
	return new ApplicationData(project.getRootPackage(), project.getApplicationName() + APPLICATION);
    }
}
