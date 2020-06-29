package net.atlassian.cmathtutor.presenter;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.fxservice.LoadStartledFrogProjectService;
import net.atlassian.cmathtutor.helper.ChangeListenerRegistryHelper;
import net.atlassian.cmathtutor.model.LoadProjectProperties;
import net.atlassian.cmathtutor.model.Project;
import net.atlassian.cmathtutor.service.ProjectService;
import net.atlassian.cmathtutor.util.FileNameChangeListener;

@Slf4j
public class LoadProjectPresenter implements Initializable {

    @FXML
    Button loadProjectButton;
    @FXML
    ProgressBar loadProgressBar;
    @FXML
    Label sfVersionLabel;
    @FXML
    Label rootPackageLabel;
    @FXML
    Label applicationNameLabel;
    @FXML
    Label projectFileNameLabel;
    @FXML
    Button startModellingButton;

    @Inject
    private ProjectService projectService;

    private FileChooser projectFileChooser = new FileChooser();
    private LoadProjectProperties loadProjectProperties = new LoadProjectProperties();
    private ChangeListenerRegistryHelper listenerRegistryHelper = new ChangeListenerRegistryHelper();
    private ObjectProperty<Project> loadedProjectProperty = new SimpleObjectProperty<>();
    private BooleanProperty readyToStartModellingProperty = new SimpleBooleanProperty(false);
    private LoadStartledFrogProjectService loadService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadService = new LoadStartledFrogProjectService(loadProjectProperties.startledFrogProjectFileProperty(),
                projectService);
        loadService.setOnScheduled(event -> loadProgressBar.setVisible(true));
        loadService.setOnSucceeded(event -> {
            Project project = (Project) event.getSource().getValue();
            loadProjectProperties.setApplicationName(project.getApplicationName());
            loadProjectProperties.setRootPackage(project.getRootPackage());
            loadProjectProperties.setStartledFrogVersion(project.getVersion());
            loadedProjectProperty.set(project);
        });
        loadService.setOnFailed(event -> {
            log.warn("LoadService execution failed");
            loadProgressBar.setVisible(false);
        });
        loadProgressBar.progressProperty().bind(loadService.progressProperty());
        projectFileChooser.getExtensionFilters().add(
                new ExtensionFilter("Startled Frog project", "*" + projectService.getProjectExtension()));
        projectFileChooser.setTitle("Choose Startled Frog project");
        loadProjectProperties.startledFrogProjectFileProperty().addListener(listenerRegistryHelper
                .registerChangeListener(new FileNameChangeListener(projectFileNameLabel.textProperty())));
        applicationNameLabel.textProperty().bind(loadProjectProperties.applicationNameProperty());
        rootPackageLabel.textProperty().bind(loadProjectProperties.rootPackageProperty());
        sfVersionLabel.textProperty().bind(loadProjectProperties.startledFrogVersionProperty().asString());
        loadProjectButton.disableProperty().bind(
                loadProjectProperties.startledFrogProjectFileProperty().isNull().or(loadService.runningProperty()));
        startModellingButton.disableProperty().bind(loadedProjectProperty.isNull());
    }

    @FXML
    public void selectProjectFile() {
        File projectFile = projectFileChooser.showOpenDialog(loadProjectButton.getScene().getWindow());
        if (projectFile != null) {
            loadProjectProperties.setStartledFrogProjectFile(projectFile);
        }
    }

    @FXML
    public void loadProject() {
        log.debug("Loading project using service...");
        loadService.restart();
    }

    @FXML
    public void startModelling() {
        projectService.setCurrentProject(loadedProjectProperty.get());
        log.info("The project is selected and ready");
        readyToStartModellingProperty.set(true);
    }

    public final ReadOnlyBooleanProperty readyToStartModellingPropertyProperty() {
        return this.readyToStartModellingProperty;
    }

    public final boolean isReadyToStartModellingProperty() {
        return this.readyToStartModellingPropertyProperty().get();
    }
}
