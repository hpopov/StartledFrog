package net.atlassian.cmathtutor.domain.persistence.translate.java.velocity;

import java.io.Writer;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class JavaClassComposer {

    private VelocityEngine ve;

    public JavaClassComposer(VelocityEngine ve) {
        this.ve = ve;
    }

    public Writer createEntityClass(Entity entity, Writer writer) {
        VelocityContext context = new VelocityContext();
        context.put("entity", entity);
        Template template = ve.getTemplate("velocity/entity-template.vm");
        template.merge(context, writer);

        return writer;
    }

    public Writer createContainableEntityClass(ContainableEntity containableEntity, Writer writer) {
        VelocityContext context = new VelocityContext();
        context.put("entity", containableEntity);
        Template template = ve.getTemplate("velocity/containable-entity-template.vm");
        template.merge(context, writer);

        return writer;
    }

    public Writer createApplicationClass(Application application, Writer writer) {
        VelocityContext context = new VelocityContext();
        context.put("app", application);
        Template template = ve.getTemplate("velocity/app-template.vm");
        template.merge(context, writer);

        return writer;
    }

    public Writer createRepositoryInterface(Repository repository, Writer writer) {
        VelocityContext context = new VelocityContext();
        context.put("repository", repository);
        Template template = ve.getTemplate("velocity/repository-template.vm");
        template.merge(context, writer);

        return writer;
    }
}
