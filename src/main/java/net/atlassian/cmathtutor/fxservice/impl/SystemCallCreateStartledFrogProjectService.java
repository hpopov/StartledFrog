package net.atlassian.cmathtutor.fxservice.impl;

import java.io.File;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.domain.configuration.model.GlobalConfigurationModel;
import net.atlassian.cmathtutor.domain.persistence.model.PersistenceModel;
import net.atlassian.cmathtutor.fxservice.AbstractCreateStartledFrogProjectService;
import net.atlassian.cmathtutor.model.CreateProjectProperties;
import net.atlassian.cmathtutor.service.ConfigurationDomainService;
import net.atlassian.cmathtutor.service.PersistenceDomainService;
import net.atlassian.cmathtutor.service.ProjectService;

@Slf4j
public class SystemCallCreateStartledFrogProjectService extends AbstractCreateStartledFrogProjectService {

    private static final int PROCESS_MIN_TIMEOUT = 50;

    private PersistenceDomainService persistenceDomainService;
    private ConfigurationDomainService configurationDomainService;

    public SystemCallCreateStartledFrogProjectService(CreateProjectProperties globalProperties,
	    ProjectService projectService, PersistenceDomainService persistenceDomainService,
	    ConfigurationDomainService configurationDomainService) {
	super(globalProperties, projectService);
	this.persistenceDomainService = persistenceDomainService;
	this.configurationDomainService = configurationDomainService;

    }

    @Override
    protected void downloadAndUnzipSpringBootSkeletonProject(String downloadSpringBootProjectUrl) throws Exception {
	String zippedAppName = createProjectProperties.getApplicationName() + ".zip";
	String downloadSpringBootSkeletonCommand = "curl -o " + zippedAppName + " " + downloadSpringBootProjectUrl;

	Process curlProcess = Runtime.getRuntime().exec(downloadSpringBootSkeletonCommand, null,
		createProjectProperties.getProjectDestinationFolder());
	int curlProcessDurationInMilliseconds = 0;
	int unzipProcessDurationInMilliseconds = 0;
	while (false == curlProcess.waitFor(PROCESS_MIN_TIMEOUT, TimeUnit.MILLISECONDS)) {
	    curlProcessDurationInMilliseconds += PROCESS_MIN_TIMEOUT;
	}
	Process unzipProcess = Runtime.getRuntime().exec("tar -xf " + zippedAppName, null,
		createProjectProperties.getProjectDestinationFolder());
	while (false == unzipProcess.waitFor(PROCESS_MIN_TIMEOUT, TimeUnit.MILLISECONDS)) {
	    unzipProcessDurationInMilliseconds += PROCESS_MIN_TIMEOUT;
	}
	log.debug("curlProcess took {}ms, unzipProcess took {}ms to complete", curlProcessDurationInMilliseconds,
		unzipProcessDurationInMilliseconds);
	File projectZipFile = createProjectProperties.getProjectDestinationFolder().toPath().resolve(zippedAppName)
		.toFile();
	if (projectZipFile.delete()) {
	    log.debug("Obsolete project zip file {} has been successfully deleted", projectZipFile);
	}
    }

    @Override
    protected void createProjectDefaultModels() {
	log.info("persistence model is null. Going to recreate it...");
//	PersistenceModel persistenceModel = createSomePersistenceModel();
	PersistenceModel persistenceModel = persistenceDomainService.initializeNewPersistenceModel();
	persistenceDomainService.persistModel(persistenceModel);
	GlobalConfigurationModel configurationModel = configurationDomainService.initializeDefaultConfigurationModel();
	amendConfigurationModelDockerIp(configurationModel);
	configurationDomainService.persistModel(configurationModel);
    }

//    private PersistenceModel createSomePersistenceModel() {
//	PersistenceFacade persistenceFacade = PersistenceFacade.newInstance();
//	PersistenceUnitDescriptor bulb = persistenceFacade.persistenceUnitBuilder("bulb")
//		.withPrimitiveAttribute()
//		.name("power")
//		.type(PrimitiveType.INTEGER)
//		.withConstraint(ConstraintType.NON_NULL)
//		.build()
//		.build();
//	PersistenceUnitDescriptor chandelier = persistenceFacade.persistenceUnitBuilder("chandelier")
//		.withPrimitiveAttribute()
//		.name("color")
//		.type(PrimitiveType.STRING)
//		.build()
//		.build();
//	persistenceFacade.associationBuilder(chandelier, bulb)
//		.aggregationKind(AggregationKind.COMPOSITE)
//		.containerAttribute()
//		.arity(AttributeArity.AT_LEAST_ZERO)
//		.navigable(true)
//		.ownerType(OwnerType.CLASSIFIER)
//		.build()
//		.elementAttribute()
//		.arity(AttributeArity.AT_MOST_ONE)
//		.navigable(true)
//		.ownerType(OwnerType.ASSOCIATION)
//		.build()
//		.build();
//	return persistenceFacade.getWrappedPersistence();
//    }

    private void amendConfigurationModelDockerIp(GlobalConfigurationModel configurationModel) {
	configurationModel.setDockerMachineIp("192.168.99.102");// TODO
    }

}
