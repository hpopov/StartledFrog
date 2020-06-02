package net.atlassian.cmathtutor.presenter;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.fxservice.impl.SystemCallCreateStartledFrogProjectService;
import net.atlassian.cmathtutor.helper.ChangeListenerRegistryHelper;
import net.atlassian.cmathtutor.model.CreateProjectProperties;
import net.atlassian.cmathtutor.model.Project;
import net.atlassian.cmathtutor.service.ConfigurationDomainService;
import net.atlassian.cmathtutor.service.PersistenceDomainService;
import net.atlassian.cmathtutor.service.ProjectService;
import net.atlassian.cmathtutor.util.CaseUtil;
import net.atlassian.cmathtutor.util.FileNameChangeListener;

@Slf4j
public class CreateProjectPresenter implements Initializable {

    @FXML
    Label projectFolderLabel;
    @FXML
    Button chooseProjectFolderButton;
    @FXML
    TextField applicationNameTextField;
    @FXML
    TextField rootPackageTextField;
    @FXML
    TextField projectDescriptionTextField;
    @FXML
    Button generateProjectSkeletonButton;
    @FXML
    ProgressBar generateProjectProgressIndicator;
    @FXML
    Label generateProjectTaskMessage;
    @FXML
    HBox progressHBox;

    @Inject
    private ProjectService projectService;
    @Inject
    private PersistenceDomainService persistenceDomainService;
    @Inject
    private ConfigurationDomainService configurationDomainService;

    private DirectoryChooser projectFolderChooser = new DirectoryChooser();
    private CreateProjectProperties createProjectProperties = new CreateProjectProperties();
    private Service<Project> createProjectService;
    private ChangeListenerRegistryHelper listenerRegistryHelper = new ChangeListenerRegistryHelper();
    private BooleanProperty readyToStartModellingProperty = new SimpleBooleanProperty(false);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
	createProjectService = new SystemCallCreateStartledFrogProjectService(createProjectProperties, projectService,
		persistenceDomainService, configurationDomainService);
	projectFolderChooser.setTitle("Choose the folder to generate project skeleton in");
	createProjectProperties.applicationNameProperty().bindBidirectional(applicationNameTextField.textProperty());
	createProjectProperties.rootPackageProperty().bindBidirectional(rootPackageTextField.textProperty());
	createProjectProperties.projectDescriptionProperty().bind(projectDescriptionTextField.textProperty());
	createProjectProperties.projectDestinationFolderProperty()
		.addListener(listenerRegistryHelper
			.registerChangeListener(new FileNameChangeListener(projectFolderLabel.textProperty())));
	generateProjectSkeletonButton.disableProperty().bind(createProjectProperties.applicationNameProperty().isEmpty()
		.or(createProjectProperties.projectDescriptionProperty().isEmpty())
		.or(createProjectProperties.projectDestinationFolderProperty().isNull())
		.or(createProjectProperties.rootPackageProperty().isEmpty())
		.or(createProjectService.runningProperty()));

	applicationNameTextField.focusedProperty()
		.addListener(listenerRegistryHelper.registerChangeListener((observable, oldValue, newValue) -> {
		    if (Boolean.FALSE.equals(newValue)) {
			String appNameCamelCased = convertToCamelCase(createProjectProperties.getApplicationName());
			if (appNameCamelCased.matches("[A-Z][a-zA-Z0-9]*")) {
			    log.debug("Camelcased application name {} is valid", appNameCamelCased);
			    createProjectProperties.setApplicationName(appNameCamelCased);
			} else {
			    log.info("Camelcased application name {} is invalid", appNameCamelCased);
			    createProjectProperties.setApplicationName(StringUtils.EMPTY);
			}
		    }
		}));

	rootPackageTextField.focusedProperty()
		.addListener(listenerRegistryHelper.registerChangeListener((observable, oldValue, newValue) -> {
		    if (Boolean.FALSE.equals(newValue)) {
			String rootPackageName = rootPackageTextField.getText().toLowerCase();
			if (rootPackageName.matches("[a-z]+([.][a-z]+)*")) {
			    log.debug("Root package name {} is valid", rootPackageName);
			    createProjectProperties.setRootPackage(rootPackageName);
			} else {
			    log.info("Root package name {} is invalid", rootPackageName);
			    createProjectProperties.setRootPackage(StringUtils.EMPTY);
			}
		    }
		}));
    }

    private String convertToCamelCase(String applicationName) {
	return CaseUtil.toCapitalizedCamelCase(applicationName);
    }

    @FXML
    public void chooseProjectFolder() {
	File chosenProjectFolder = projectFolderChooser.showDialog(projectFolderLabel.getScene().getWindow());
	createProjectProperties.setProjectDestinationFolder(chosenProjectFolder);
    }

    @FXML
    public void generateProjectSkeleton() {
	generateProjectProgressIndicator.progressProperty().bind(createProjectService.progressProperty());
	generateProjectTaskMessage.textProperty().bind(createProjectService.messageProperty());
	createProjectService.setOnSucceeded(event -> {
	    generateProjectSkeletonButton.setVisible(false);
	    log.debug("Create project service completed, progress {}", generateProjectProgressIndicator.getProgress());
	    readyToStartModellingProperty.set(true);
	});
	createProjectService.setOnFailed(event -> {
	    log.error("Create project service failed", event.getSource().getException());
	});
	progressHBox.setVisible(true);
	createProjectService.start();
    }

    public final ReadOnlyBooleanProperty readyToStartModellingPropertyProperty() {
	return this.readyToStartModellingProperty;
    }

    public final boolean isReadyToStartModellingProperty() {
	return this.readyToStartModellingPropertyProperty().get();
    }

}
