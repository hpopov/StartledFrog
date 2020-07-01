package net.atlassian.cmathtutor.domain.persistence.translate.java.translator;

import net.atlassian.cmathtutor.domain.persistence.translate.java.velocity.Application;
import net.atlassian.cmathtutor.model.Project;

public class ProjectToApplicationTranslator {

    private static final String APPLICATION = "Application";

    public Application translate(Project project) {
        return new Application(project.getRootPackage(), project.getApplicationName() + APPLICATION);
    }
}
