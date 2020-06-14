package net.atlassian.cmathtutor.domain.persistence.facade;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.domain.persistence.AggregationKind;
import net.atlassian.cmathtutor.domain.persistence.AttributeArity;
import net.atlassian.cmathtutor.domain.persistence.ConstraintType;
import net.atlassian.cmathtutor.domain.persistence.OwnerType;
import net.atlassian.cmathtutor.domain.persistence.PrimitiveType;
import net.atlassian.cmathtutor.domain.persistence.descriptor.AssociationDescriptor;
import net.atlassian.cmathtutor.domain.persistence.descriptor.IllegalOperationException;
import net.atlassian.cmathtutor.domain.persistence.descriptor.PersistenceDescriptor;
import net.atlassian.cmathtutor.domain.persistence.descriptor.PersistenceUnitDescriptor;
import net.atlassian.cmathtutor.domain.persistence.model.AssociationModel;
import net.atlassian.cmathtutor.domain.persistence.model.PersistenceModel;
import net.atlassian.cmathtutor.domain.persistence.model.PrimitiveAttributeModel;
import net.atlassian.cmathtutor.domain.persistence.model.ReferentialAttributeModel;
import net.atlassian.cmathtutor.domain.persistence.model.RepositoryOperationModel;
import net.atlassian.cmathtutor.util.UidUtil;

@Slf4j
public class PersistenceFacade {

    private static final int REFERENTIAL_ATTRIBUTE_DEFAULT_NAME_TRESHOLD = 3;
    private PersistenceDescriptor persistenceDescriptor;

    private PersistenceFacade(PersistenceDescriptor persistenceDescriptor) {
	this.persistenceDescriptor = persistenceDescriptor;
    }

    public PersistenceModel getWrappedPersistence() {
	return persistenceDescriptor.getPersistence();
    }

    public static PersistenceFacade newInstance() {
	return new PersistenceFacade(PersistenceDescriptor.newInstance());
    }

    public static PersistenceFacade loadFromFile(File persistenceModelFile) {
	PersistenceModel persistence = null;
	try {
	    JAXBContext context = JAXBContext.newInstance(PersistenceModel.class);
	    persistence = (PersistenceModel) context.createUnmarshaller()
		    .unmarshal(new FileReader(persistenceModelFile));
	} catch (JAXBException | FileNotFoundException e) {
	    throw new RuntimeException("Unable to load persistence model using JAXB", e);
	}
	PersistenceDescriptor descriptor = executeInExceptionWrapper(PersistenceDescriptor::wrap, persistence);
	return new PersistenceFacade(descriptor);
    }

    public PersistenceUnitBuilder persistenceUnitBuilder(@NonNull String name) {
	assertNameContainsAllowableCharactersOnly(name);
	name = trimAndUppercaseFirstLetter(name);
	PersistenceUnitDescriptor persistenceUnitDescriptor = executeInExceptionWrapper(
		persistenceDescriptor::addNewPersistenceUnit, name);
	return new PersistenceUnitBuilder(persistenceUnitDescriptor);
    }

    private void assertNameContainsAllowableCharactersOnly(String name) {
	if (false == name.trim().matches("[a-zA-Z]+[a-zA-Z_0-9 \\-]*")) {
	    throw new IllegalArgumentException(
		    "Name must start with latin letter and contain only letters, digits, dashes, underscores or spaces");
	}
    }

    private String trimAndUppercaseFirstLetter(String name) {
	name = name.trim();
	name = name.replaceAll("[ ]+", " ");
	char firstLetter = name.charAt(0);
	if (false == Character.isUpperCase(firstLetter)) {
	    name = Character.toUpperCase(firstLetter) + name.substring(1);
	}
	return name;
    }

    private String trimAndLowercaseFirstLetter(String name) {
	name = name.trim();
	name = name.replaceAll("[ ]+", " ");
	char firstLetter = name.charAt(0);
	if (false == Character.isLowerCase(firstLetter)) {
	    name = Character.toLowerCase(firstLetter) + name.substring(1);
	}
	return name;
    }

    public AssociationBuilder associationBuilder(PersistenceUnitDescriptor container,
	    PersistenceUnitDescriptor element) {
	AssociationBuilder associationBuilder = new AssociationBuilder(container, element);
	return associationBuilder;
    }

    private AssociationDescriptor buildAssociation(AssociationBuilder builder) {
	if (builder.containerAttribute.getName() == null) {
	    initializeReferentialAttributeWithDefaultName(builder.container, builder.containerAttribute,
		    builder.element.getName());
	} else {
	    executeInExceptionWrapper(
		    () -> builder.container.addReferentialAttribute(builder.containerAttribute));
	}
	if (builder.elementAttribute.getName() == null) {
	    initializeReferentialAttributeWithDefaultName(builder.element, builder.elementAttribute,
		    builder.container.getName());
	} else {
	    executeInExceptionWrapper(
		    () -> builder.element.addReferentialAttribute(builder.elementAttribute));
	}

	return executeInExceptionWrapper(persistenceDescriptor::addNewAssociation, builder.association);
    }

    private void initializeReferentialAttributeWithDefaultName(PersistenceUnitDescriptor persistenceUnitDescriptor,
	    ReferentialAttributeModel referentialAttribute,
	    String referencedPersistenceUnitName) {
	String defaultAttributeName = trimAndLowercaseFirstLetter(referencedPersistenceUnitName);
	referentialAttribute.setName(defaultAttributeName);
	boolean isAttributeInitialized = false;
	for (int i = 1; i <= REFERENTIAL_ATTRIBUTE_DEFAULT_NAME_TRESHOLD && !isAttributeInitialized; i++) {
	    try {
		persistenceUnitDescriptor.addReferentialAttribute(referentialAttribute);
		isAttributeInitialized = true;
	    } catch (IllegalOperationException e) {
		log.warn("Unable to initialize referential attribute : {}", e.getMessage());
		referentialAttribute.setName(defaultAttributeName + i);
	    }
	}
	if (false == isAttributeInitialized) {
	    throw new IllegalStateException("Unable to initialize referential attribute using default name: "
		    + "tried " + REFERENTIAL_ATTRIBUTE_DEFAULT_NAME_TRESHOLD + " times and still failed");
	}
    }

    private static void executeInExceptionWrapper(RunnableDescriptorLogic logic) {
	try {
	    logic.run();
	} catch (IllegalOperationException e) {
	    throw new RuntimeException(e.getMessage(), e);
	}
    }

    private static <T, U> U executeInExceptionWrapper(FunctionDescriptorLogic<T, U> logic, T argument) {
	try {
	    return logic.apply(argument);
	} catch (IllegalOperationException e) {
	    throw new RuntimeException(e.getMessage(), e);
	}
    }

    @FunctionalInterface
    private static interface RunnableDescriptorLogic {
	void run() throws IllegalOperationException;
    }

    @FunctionalInterface
    private static interface FunctionDescriptorLogic<T, U> {
	U apply(T argument) throws IllegalOperationException;
    }

    public class PersistenceUnitBuilder {
	private PersistenceUnitDescriptor persistenceUnitDescriptor;

	private PersistenceUnitBuilder(PersistenceUnitDescriptor persistenceUnitDescriptor) {
	    this.persistenceUnitDescriptor = persistenceUnitDescriptor;
	}

	public PrimitiveAttributeBuilder withPrimitiveAttribute() {
	    return new PrimitiveAttributeBuilder();
	}

	public RepositoryOperationBuilder withRepositoryOperation() {
	    return new RepositoryOperationBuilder();
	}

	public PersistenceUnitDescriptor build() {
	    return persistenceUnitDescriptor;// TODO: here we have already ADDED unit to persistence: builder
					     // enhancement proposal
	}

	public class PrimitiveAttributeBuilder {

	    private PrimitiveAttributeModel primitiveAttributeModel;

	    private PrimitiveAttributeBuilder() {
		primitiveAttributeModel = new PrimitiveAttributeModel(UidUtil.getUId());
		primitiveAttributeModel.setType(PrimitiveType.STRING);
	    }

	    public PrimitiveAttributeBuilder type(PrimitiveType type) {
		primitiveAttributeModel.setType(type);
		return this;
	    }

	    public PrimitiveAttributeBuilder name(String name) {
		assertNameContainsAllowableCharactersOnly(name);
		primitiveAttributeModel.setName(trimAndLowercaseFirstLetter(name));
		return this;
	    }

	    public PrimitiveAttributeBuilder withConstraint(ConstraintType constraint) {
		primitiveAttributeModel.getConstraints().add(constraint);
		return this;
	    }

	    public PersistenceUnitBuilder build() {
		executeInExceptionWrapper(
			() -> persistenceUnitDescriptor.addNewPrimitiveAttribute(primitiveAttributeModel));
		return PersistenceUnitBuilder.this;
	    }
	}

	public class RepositoryOperationBuilder {

	    private RepositoryOperationModel repositoryOperationModel;

	    public RepositoryOperationBuilder() {
		repositoryOperationModel = new RepositoryOperationModel(UidUtil.getUId());
	    }

	    public RepositoryOperationBuilder name(String name) {
		assertNameContainsAllowableCharactersOnly(name);
		repositoryOperationModel.setName(trimAndLowercaseFirstLetter(name));
		return this;
	    }

	    public PersistenceUnitBuilder build() {
		executeInExceptionWrapper(
			() -> persistenceUnitDescriptor.addNewRepositoryOperation(repositoryOperationModel));
		return PersistenceUnitBuilder.this;
	    }
	}

    }

    public class AssociationBuilder {
	private PersistenceUnitDescriptor container;
	private PersistenceUnitDescriptor element;

	private AssociationModel association = new AssociationModel(UidUtil.getUId());
	private ReferentialAttributeModel containerAttribute = new ReferentialAttributeModel(UidUtil.getUId());
	private ReferentialAttributeModel elementAttribute = new ReferentialAttributeModel(UidUtil.getUId());

	private AssociationBuilder(PersistenceUnitDescriptor container, PersistenceUnitDescriptor element) {
	    this.container = container;
	    this.element = element;
	    association.setAggregationKind(AggregationKind.NONE);
	    association.setContainerAttribute(containerAttribute);
	    association.setElementAttribute(elementAttribute);
	}

	public AssociationDescriptor build() {
	    return PersistenceFacade.this.buildAssociation(this);
	}

	public ReferentialAttributeBuilder containerAttribute() {
	    return new ReferentialAttributeBuilder(containerAttribute);
	}

	public ReferentialAttributeBuilder elementAttribute() {
	    return new ReferentialAttributeBuilder(elementAttribute);
	}

	public AssociationBuilder aggregationKind(AggregationKind aggregationKind) {
	    association.setAggregationKind(aggregationKind);
	    return this;
	}

	public class ReferentialAttributeBuilder {

	    private ReferentialAttributeModel referentialAttribute;

	    public ReferentialAttributeBuilder(ReferentialAttributeModel referentialAttribute) {
		this.referentialAttribute = referentialAttribute;
		referentialAttribute.setNavigable(false);
		referentialAttribute.setArity(AttributeArity.AT_MOST_ONE);
		referentialAttribute.setOwnerType(OwnerType.ASSOCIATION);
	    }

	    public ReferentialAttributeBuilder name(String name) {
		assertNameContainsAllowableCharactersOnly(name);
		referentialAttribute.setName(trimAndLowercaseFirstLetter(name));
		return this;
	    }

	    public ReferentialAttributeBuilder arity(AttributeArity arity) {
		referentialAttribute.setArity(arity);
		return this;
	    }

	    public ReferentialAttributeBuilder navigable(boolean navigable) {
		referentialAttribute.setNavigable(navigable);
		return this;
	    }

	    public ReferentialAttributeBuilder ownerType(OwnerType ownerType) {
		referentialAttribute.setOwnerType(ownerType);
		return this;
	    }

	    public AssociationBuilder build() {
		return AssociationBuilder.this;
	    }
	}
    }
}
