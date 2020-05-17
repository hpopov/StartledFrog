package net.atlassian.cmathtutor.fxdiagram;

import org.eclipse.xtext.xbase.lib.Functions.Function1;

import de.fxdiagram.core.XDomainObjectOwner;
import de.fxdiagram.core.model.DomainObjectDescriptor;
import de.fxdiagram.core.model.ModelElementImpl;
import de.fxdiagram.mapping.AbstractDiagramConfig;
import de.fxdiagram.mapping.AbstractMappedElementDescriptor;
import de.fxdiagram.mapping.AbstractMapping;
import de.fxdiagram.mapping.IMappedElementDescriptor;
import de.fxdiagram.mapping.IMappedElementDescriptorProvider;
import de.fxdiagram.mapping.MappingAcceptor;
import de.fxdiagram.mapping.NodeMapping;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StartledFrogConfig extends AbstractDiagramConfig {

    public static final String CONFIG_ID = "startled-frog-default-config";
    public static final String DEFAULT_MAPPING = "typeNode";
    private final NodeMapping<Class<Object>> typeNode = new NodeMapping<Class<Object>>(this, DEFAULT_MAPPING, "Class");

    public StartledFrogConfig() {
	setID(CONFIG_ID);
    }

    @Override
    public void initialize(XDomainObjectOwner shape) {
	log.debug("Initializing | suppose we can skip this step. XDomainObjectOwner shape is {}", shape);
	shape.domainObjectDescriptorProperty().addListener((observable, oldV, newV) -> {
	    log.debug("shape's domain object descriptor changed {} -> {}", oldV, newV);
	});
    }

    @Override
    protected IMappedElementDescriptorProvider createDomainObjectProvider() {
	return new IMappedElementDescriptorProvider() {

	    @Override
	    public void postLoad() {
		log.debug("IMappedElementDescriptorProvider: postLoad");
	    }

	    @Override
	    public <T> DomainObjectDescriptor createDescriptor(T domainObject) {
		throw new UnsupportedOperationException("StartledFrog config supports IMappedElementDescriptors only");
	    }

	    @Override
	    public <T> IMappedElementDescriptor<T> createMappedElementDescriptor(T domainObject,
		    AbstractMapping<? extends T> mapping) {
		log.debug("Anonymous IMappedElementDescriptorProvider is going to create IMappedElementDescriptor "
			+ "with domainObject {} and mapping {}", domainObject, mapping);
		return new AbstractMappedElementDescriptor<T>(CONFIG_ID, mapping.getID()) {

		    @Override
		    public <U> U withDomainObject(Function1<? super T, ? extends U> lambda) {
			log.debug("Invoking 'withDomainObject', domainObject is {}", domainObject);
			U resultingObject = lambda.apply(domainObject);
			log.debug(", resulting object is {}", resultingObject);
			return resultingObject;
//			throw new UnsupportedOperationException(
//				"Opening model editor (populated with domain object) is unsupported in StartledFrog");
		    }

		    @Override
		    public Object openInEditor(boolean select) {
			throw new UnsupportedOperationException("Startled Frog has no editor to open in");
		    }

		    @Override
		    public String getName() {
			return "Any Java class";
		    }

		    @Override
		    public void populate(ModelElementImpl modelElement) {
			super.populate(modelElement);
			log.debug("AbstractMappedElementDescriptor is going to populate modelElement {}", modelElement);
		    }

		};
	    }
	};
    }

    @Override
    protected <ARG> void entryCalls(ARG domainArgument, MappingAcceptor<ARG> acceptor) {
	acceptor.add(typeNode);
    }

}
