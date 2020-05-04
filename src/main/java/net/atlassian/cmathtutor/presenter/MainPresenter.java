package net.atlassian.cmathtutor.presenter;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.fxservice.impl.SystemCallCreateStartledFrogProjectService;
import net.atlassian.cmathtutor.model.GlobalPropertiesModel;
import javafx.scene.control.ProgressBar;

@Slf4j
public class MainPresenter implements Initializable {

    @FXML
    TabPane root;
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

    private DirectoryChooser projectFolderChooser = new DirectoryChooser();
    private GlobalPropertiesModel globalProperties = new GlobalPropertiesModel();
    private Service<Void> createProjectService = new SystemCallCreateStartledFrogProjectService(globalProperties);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
	projectFolderChooser.setTitle("Choose the folder to generate project skeleton in");
	globalProperties.applicationNameProperty().bindBidirectional(applicationNameTextField.textProperty());
	globalProperties.rootPackageProperty().bindBidirectional(rootPackageTextField.textProperty());
	globalProperties.projectDescriptionProperty().bind(projectDescriptionTextField.textProperty());
	globalProperties.projectDestinationFolderProperty().addListener((observable, oldValue, newValue) -> {
	    if (newValue == null) {
		projectFolderLabel.setText("...");
	    } else {
		projectFolderLabel.setText(newValue.getName());
	    }
	});
	generateProjectSkeletonButton.disableProperty().bind(globalProperties.applicationNameProperty().isEmpty()
		.or(globalProperties.projectDescriptionProperty().isEmpty())
		.or(globalProperties.projectDestinationFolderProperty().isNull())
		.or(globalProperties.rootPackageProperty().isEmpty())
		.or(createProjectService.runningProperty()));

	applicationNameTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
	    if (Boolean.FALSE.equals(newValue)) {
		String appNameCamelCased = convertToCamelCase(globalProperties.getApplicationName());
		if (appNameCamelCased.matches("[A-Z][a-zA-Z0-9]*")) {
		    log.debug("Camelcased application name {} is valid", appNameCamelCased);
		    globalProperties.setApplicationName(appNameCamelCased);
		} else {
		    log.info("Camelcased application name {} is invalid", appNameCamelCased);
		    globalProperties.setApplicationName(StringUtils.EMPTY);
		}
	    }
	});

	rootPackageTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
	    if (Boolean.FALSE.equals(newValue)) {
		String rootPackageName = rootPackageTextField.getText().toLowerCase();
		if (rootPackageName.matches("[a-z]+([.][a-z]+)*")) {
		    log.debug("Root package name {} is valid", rootPackageName);
		    globalProperties.setRootPackage(rootPackageName);
		} else {
		    log.info("Root package name {} is invalid", rootPackageName);
		    globalProperties.setRootPackage(StringUtils.EMPTY);
		}
	    }
	});
    }

    private String convertToCamelCase(String applicationName) {
	return Stream.of(applicationName.split("([ .:,\\t\\r\\n]+|(?<=[a-z0-9])(?=[A-Z]))")).map(String::toLowerCase)
		.map(token -> {
		    String stringToReplace = "[" + token.charAt(0) + "]";
		    String replacement = String.valueOf(Character.toUpperCase(token.charAt(0)));
		    return token.replaceFirst(stringToReplace, replacement);
		})
		.collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
    }

    @FXML
    public void chooseProjectFolder() {
	File chosenProjectFolder = projectFolderChooser.showDialog(root.getScene().getWindow());
	globalProperties.setProjectDestinationFolder(chosenProjectFolder);
    }

    @FXML
    public void generateProjectSkeleton() {
	generateProjectProgressIndicator.progressProperty().bind(createProjectService.progressProperty());
	generateProjectTaskMessage.textProperty().bind(createProjectService.messageProperty());
	createProjectService.setOnSucceeded(event -> {
	    generateProjectSkeletonButton.setVisible(false);
	    log.debug("Create project service completed, progress {}", generateProjectProgressIndicator.getProgress());
	});
	createProjectService.setOnFailed(event -> {
	    log.error("Create project service failed", event.getSource().getException());
	});
	generateProjectProgressIndicator.setVisible(true);
	createProjectService.start();
    }
}
