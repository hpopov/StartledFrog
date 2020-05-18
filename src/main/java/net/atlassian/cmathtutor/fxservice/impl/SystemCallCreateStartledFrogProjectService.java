package net.atlassian.cmathtutor.fxservice.impl;

import java.io.File;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.fxservice.AbstractCreateStartledFrogProjectService;
import net.atlassian.cmathtutor.model.CreateProjectProperties;
import net.atlassian.cmathtutor.service.ProjectService;

@Slf4j
public class SystemCallCreateStartledFrogProjectService extends AbstractCreateStartledFrogProjectService {

    private static final int PROCESS_MIN_TIMEOUT = 50;

    public SystemCallCreateStartledFrogProjectService(CreateProjectProperties globalProperties,
	    ProjectService projectService) {
	super(globalProperties, projectService);
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

}
