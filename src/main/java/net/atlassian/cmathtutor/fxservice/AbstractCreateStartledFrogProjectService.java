package net.atlassian.cmathtutor.fxservice;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.ProjectBuildFramework;
import net.atlassian.cmathtutor.model.CreateProjectProperties;
import net.atlassian.cmathtutor.model.Project;
import net.atlassian.cmathtutor.service.ProjectService;

@Slf4j
@AllArgsConstructor
public abstract class AbstractCreateStartledFrogProjectService extends Service<Project> {

    protected CreateProjectProperties createProjectProperties;
    private ProjectService projectService;

    @Override
    protected Task<Project> createTask() {
	return new Task<Project>() {

	    @Override
	    protected Project call() throws Exception {
		updateProgress(0, 1);
		updateMessage("Preparing HTTP request");
		log.debug("Start {} execution with input {}", getClass().getSimpleName(), createProjectProperties);
		String downloadSpringBootProjectUrl = new StringBuilder(
			"https://start.spring.io/starter.zip?type=maven-project")
				.append("&language=java&bootVersion=2.2.6.RELEASE")
				.append("&baseDir=" + createProjectProperties.getApplicationName())
				.append("&groupId=" + createProjectProperties.getRootPackage())
				.append("&artifactId=" + createProjectProperties.getApplicationName())
				.append("&name=" + createProjectProperties.getApplicationName())
				.append("&description=" + urlEncode(createProjectProperties.getProjectDescription()))
				.append("&packageName=" + createProjectProperties.getRootPackage())
				.append("&packaging=jar&javaVersion=1.8")
				.append("&dependencies=lombok,web,data-jpa,liquibase,mysql,data-rest,data-rest-hal")
				.toString();
		try {
		    updateProgress(0.1, 1);
		    updateMessage("Downloading project skeleton");
		    downloadAndUnzipSpringBootSkeletonProject(downloadSpringBootProjectUrl);
		} catch (Exception e) {
		    updateMessage("Error occured");
		    log.error("Failed to download & unzip Spring Boot skeleton project", e);
		    throw e;
		}
		updateProgress(0.6, 1);
		updateMessage("Amending pom file");
		File projectRoot = createProjectProperties.getProjectDestinationFolder().toPath()
			.resolve(createProjectProperties.getApplicationName()).toFile();
		Project project = Project.builder()
			.applicationName(createProjectProperties.getApplicationName())
			.projectBuildFramework(ProjectBuildFramework.MAVEN)
			.projectFolder(projectRoot)
			.rootPackage(createProjectProperties.getRootPackage())
			.build();
		alterProjectPom(project);
		updateProgress(0.7, 1);
		updateMessage("Creating Startled Frog auxiliary files");
		projectService.setCurrentProject(project);
		projectService.persistCurrentProject();
		updateProgress(0.8, 1);
		updateMessage("Creating default models");
		createProjectDefaultModels();
		updateProgress(1, 1);
		updateMessage("Completed successfully");
		return project;
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

    private void alterProjectPom(Project project)
	    throws ParserConfigurationException, SAXException, IOException, TransformerException {
	File pomFile = project.getProjectFolder().toPath()
		.resolve(project.getProjectBuildFramework().getBuildFileName()).toFile();
	DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
	Document document = documentBuilder.parse(pomFile);

	Node halBrowserArtifactIdNode = getNodeByTextContent(document.getElementsByTagName("artifactId"),
		"spring-data-rest-hal-browser");
	if (halBrowserArtifactIdNode == null) {
	    log.warn("Referenced build file {} doesn't contain hal-browser artifactId", pomFile);
	    // TODO: just add explorer node instead...
	    throw new IllegalStateException("Referenced build file doesn't contain hal-browser artifactId");
	}
	halBrowserArtifactIdNode.setTextContent("spring-data-rest-hal-explorer");

	Node rootNode = document.getFirstChild();
	Node propertiesNode = getNodeByParentNode(document.getElementsByTagName("properties"), rootNode);
	Element versionNode = document.createElement("startled-frog.version");
	versionNode.appendChild(document.createTextNode(ProjectService.CURRENT_DEPENDENCY_VERSION));
	propertiesNode.appendChild(versionNode);

	String repositories = "repositories";
	Node repositoriesNode = getNodeByParentNode(document.getElementsByTagName(repositories), rootNode);
	if (repositoriesNode == null) {
	    repositoriesNode = document.createElement(repositories);
	    Node parentNode = getNodeByParentNode(document.getElementsByTagName("parent"), rootNode);
	    if (parentNode == null) {
		throw new IllegalStateException("Spring Boot's build pom must contain 'parent' node");
	    }
	    rootNode.insertBefore(repositoriesNode, parentNode);
	}
	Node repositoryNode = document.createElement("repository");
	Node idNode = document.createElement("id");
	idNode.appendChild(document.createTextNode("startled-frog-repository"));
	repositoryNode.appendChild(idNode);
	Node nameNode = document.createElement("name");
	nameNode.appendChild(document.createTextNode("Repository with Startled Frog specific dependencies"));
	repositoryNode.appendChild(nameNode);
	Node urlNode = document.createElement("url");
	urlNode.appendChild(document.createTextNode("http://raw.github.com/Tordek947/Dependencies/repository"));
	repositoryNode.appendChild(urlNode);
	repositoriesNode.appendChild(repositoryNode);

	Node dependenciesNode = getNodeByParentNode(document.getElementsByTagName("dependencies"), rootNode);
	if (dependenciesNode == null) {
	    throw new IllegalStateException("Spring Boot's build pom must contain 'dependencies' node");
	}
	Node dependencyNode = document.createElement("dependency");
	Element groupIdNode = document.createElement("groupId");
	groupIdNode.appendChild(document.createTextNode("ua.cmathtutor.startledfrog"));
	dependencyNode.appendChild(groupIdNode);
	Node artifactIdNode = document.createElement("artifactId");
	artifactIdNode.appendChild(document.createTextNode("SpringBoot-dependencies"));
	dependencyNode.appendChild(artifactIdNode);
	Node versionUsageNode = document.createElement("version");
	versionUsageNode.appendChild(document.createTextNode("${startled-frog.version}"));
	dependencyNode.appendChild(versionUsageNode);
	dependenciesNode.appendChild(dependencyNode);

	TransformerFactory transformerFactory = TransformerFactory.newInstance();
	Transformer transformer = transformerFactory.newTransformer();
	DOMSource domSource = new DOMSource(document);
	StreamResult streamResult = new StreamResult(pomFile);
	transformer.transform(domSource, streamResult);
	log.info("Project {} pom transformation completed successfully", project.getApplicationName());
    }

    protected abstract void createProjectDefaultModels();

    private static Node getNodeByTextContent(NodeList nodes, String textContent) {
	for (int i = nodes.getLength() - 1; i >= 0; i--) {
	    if (nodes.item(i).getTextContent().equals(textContent)) {
		return nodes.item(i);
	    }
	}
	return null;
    }

    private static Node getNodeByParentNode(NodeList nodes, Node parentNode) {
	for (int i = nodes.getLength() - 1; i >= 0; i--) {
	    if (nodes.item(i).getParentNode().isSameNode(parentNode)) {
		return nodes.item(i);
	    }
	}
	return null;
    }
}
