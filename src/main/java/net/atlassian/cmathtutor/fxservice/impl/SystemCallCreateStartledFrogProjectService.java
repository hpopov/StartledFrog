package net.atlassian.cmathtutor.fxservice.impl;

import java.io.File;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.fxservice.AbstractCreateStartledFrogProjectService;
import net.atlassian.cmathtutor.model.GlobalPropertiesModel;

@Slf4j
public class SystemCallCreateStartledFrogProjectService extends AbstractCreateStartledFrogProjectService {

    private static final int PROCESS_MIN_TIMEOUT = 50;

    public SystemCallCreateStartledFrogProjectService(GlobalPropertiesModel globalProperties) {
	super(globalProperties);
    }

    @Override
    protected void downloadAndUnzipSpringBootSkeletonProject(String downloadSpringBootProjectUrl) throws Exception {
	String zippedAppName = globalProperties.getApplicationName() + ".zip";
	String downloadSpringBootSkeletonCommand = "curl -o " + zippedAppName + " " + downloadSpringBootProjectUrl;
	
	Process curlProcess = Runtime.getRuntime().exec(downloadSpringBootSkeletonCommand, null,
		globalProperties.getProjectDestinationFolder());
	int curlProcessDurationInMilliseconds = 0;
	int unzipProcessDurationInMilliseconds = 0;
	while (false == curlProcess.waitFor(PROCESS_MIN_TIMEOUT, TimeUnit.MILLISECONDS)) {
	    curlProcessDurationInMilliseconds += PROCESS_MIN_TIMEOUT;
	}
	Process unzipProcess = Runtime.getRuntime().exec("tar -xf " + zippedAppName, null,
		globalProperties.getProjectDestinationFolder());
	while (false == unzipProcess.waitFor(PROCESS_MIN_TIMEOUT, TimeUnit.MILLISECONDS)) {
	    unzipProcessDurationInMilliseconds += PROCESS_MIN_TIMEOUT;
	}
	log.debug("curlProcess took {}ms, unzipProcess took {}ms to complete", curlProcessDurationInMilliseconds,
		unzipProcessDurationInMilliseconds);
	File projectZipFile = globalProperties.getProjectDestinationFolder().toPath().resolve(zippedAppName).toFile();
	if (projectZipFile.delete()) {
	    log.debug("Obsolete project zip file {} has been successfully deleted", projectZipFile);
	}
    }

}
