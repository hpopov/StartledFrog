package net.atlassian.cmathtutor.domain.configuration.translate;

import java.io.Writer;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import net.atlassian.cmathtutor.domain.configuration.model.GlobalConfigurationModel;

public class PropertiesComposer {

    private VelocityEngine ve;

    public PropertiesComposer(VelocityEngine ve) {
        this.ve = ve;
    }

    public Writer composeApplicationProperties(Writer writer, GlobalConfigurationModel model) {
        VelocityContext context = new VelocityContext();
        context.put("model", model);
        Template template = ve.getTemplate("velocity/app-properties-template.vm");
        template.merge(context, writer);

        return writer;
    }

    public Writer composeLiquibaseProperties(Writer writer, GlobalConfigurationModel model) {
        VelocityContext context = new VelocityContext();
        context.put("model", model);
        Template template = ve.getTemplate("velocity/liquibase-properties-template.vm");
        template.merge(context, writer);

        return writer;
    }

    public Writer composeDockerYaml(Writer writer, GlobalConfigurationModel model) {
        VelocityContext context = new VelocityContext();
        context.put("model", model);
        // context.put("rootPassword", "root");
        // context.put("user", "user");
        // context.put("password", "password");
        // context.put("database", "appdb");

        Template template = ve.getTemplate("velocity/docker-compose-template.vm");
        template.merge(context, writer);

        return writer;
    }
}
