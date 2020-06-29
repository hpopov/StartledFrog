package net.atlassian.cmathtutor.presenter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.VplApplication;
import net.atlassian.cmathtutor.helper.ChangeListenerRegistryHelper;
import net.atlassian.cmathtutor.view.ProjectView;

@Slf4j
public class StartPresenter implements Initializable {

    @FXML
    CreateProjectPresenter createProjectController;
    @FXML
    LoadProjectPresenter loadProjectController;
    @FXML
    HBox root;
    // private BooleanBinding orBinding;

    private ChangeListenerRegistryHelper listenerRegistryHelper = new ChangeListenerRegistryHelper();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // For some reason it simply doesn't work...
        // orBinding =
        // loadProjectController.readyToStartModellingPropertyProperty()
        // .or(createProjectController.readyToStartModellingPropertyProperty());
        ChangeListener<? super Boolean> changeListener = listenerRegistryHelper
                .registerChangeListener((observable, oldValue, newValue) -> {
                    log.debug("New value receieved, start presenter is ready? {}", newValue);
                    if (newValue) {
                        switchToMainWindow();
                    }
                });
        loadProjectController.readyToStartModellingPropertyProperty().addListener(changeListener);
        createProjectController.readyToStartModellingPropertyProperty().addListener(changeListener);
    }

    private void switchToMainWindow() {
        listenerRegistryHelper = null;
        Stage stage = new Stage();
        stage.initModality(Modality.NONE);
        stage.setResizable(false);
        ProjectView view = new ProjectView();
        Scene scene = new Scene(view.getView());
        PerspectiveCamera perspectiveCamera = new PerspectiveCamera();
        scene.setCamera(perspectiveCamera);
        stage.setScene(scene);
        stage.setTitle(VplApplication.MAIN_TITLE);
        stage.show();
        root.getScene().getWindow().hide();
    }
}
