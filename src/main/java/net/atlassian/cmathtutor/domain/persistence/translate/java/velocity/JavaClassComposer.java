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

    public Writer createEntityClass(EntityData entity, Writer writer) {
	VelocityContext context = new VelocityContext();
	context.put("entity", entity);
	Template template = ve.getTemplate("velocity/entity-template.vm");
	template.merge(context, writer);

	return writer;
    }

    public Writer createCompositeElementEntityClass(CompositeElementEntityData compositeElementEntity, Writer writer) {
	VelocityContext context = new VelocityContext();
	context.put("entity", compositeElementEntity);
	Template template = ve.getTemplate("velocity/composite-element-template.vm");
	template.merge(context, writer);

	return writer;
    }

    public Writer createApplicationClass(ApplicationData applicationData, Writer writer) {
	VelocityContext context = new VelocityContext();
	context.put("app", applicationData);
	Template template = ve.getTemplate("velocity/app-template.vm");
	template.merge(context, writer);

	return writer;
    }

    public Writer createRepositoryInterface(RepositoryData repositoryData, Writer writer) {
	VelocityContext context = new VelocityContext();
	context.put("repository", repositoryData);
	Template template = ve.getTemplate("velocity/repository-template.vm");
	template.merge(context, writer);

	return writer;
    }

}
