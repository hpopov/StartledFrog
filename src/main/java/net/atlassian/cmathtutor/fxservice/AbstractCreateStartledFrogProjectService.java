package net.atlassian.cmathtutor.fxservice;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.model.GlobalPropertiesModel;

@Slf4j
@AllArgsConstructor
public abstract class AbstractCreateStartledFrogProjectService extends Service<Void> {

    protected GlobalPropertiesModel globalProperties;

    @Override
    protected Task<Void> createTask() {
	return new Task<Void>() {

	    @Override
	    protected Void call() throws Exception {
		updateProgress(0, 1);
		updateMessage("Preparing HTTP request");
		log.debug("Start {} execution with input {}", getClass().getSimpleName(), globalProperties);
		String downloadSpringBootProjectUrl = new StringBuilder(
			"https://start.spring.io/starter.zip?type=maven-project")
				.append("&language=java&bootVersion=2.2.6.RELEASE")
				.append("&baseDir=" + globalProperties.getApplicationName())
				.append("&groupId=" + globalProperties.getRootPackage())
				.append("&artifactId=" + globalProperties.getApplicationName())
				.append("&name=" + globalProperties.getApplicationName())
				.append("&description=" + urlEncode(globalProperties.getProjectDescription()))
				.append("&packageName=com.example.demo")
				.append("&packaging=jar&javaVersion=1.8&dependencies=lombok,web,data-jpa,liquibase,mysql")
				.toString();
		try {
		    updateProgress(0.5, 1);
		    updateMessage("Downloading project skeleton");
		    downloadAndUnzipSpringBootSkeletonProject(downloadSpringBootProjectUrl);
		} catch (Exception e) {
		    updateMessage("Error occured");
		    log.error("Failed to download & unzip Spring Boot skeleton project", e);
		    throw e;
		}
		updateProgress(1, 1);
		updateMessage("Completed successfully");
		return null;
	    }
	};
    }

    private String urlEncode(String value) {
	try {
	    return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
	} catch (UnsupportedEncodingException e) {
	    log.error("Unable to encode value {} because of {}, returning empty string...", value, e.getMessage());
	    return StringUtils.EMPTY;
	}
    }

    protected abstract void downloadAndUnzipSpringBootSkeletonProject(String downloadSpringBootProjectUrl)
	    throws Exception;
}
