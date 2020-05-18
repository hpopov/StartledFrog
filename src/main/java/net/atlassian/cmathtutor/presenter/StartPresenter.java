package net.atlassian.cmathtutor.presenter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.atlassian.cmathtutor.VplApplication;
import net.atlassian.cmathtutor.helper.ChangeListenerRegistryHelper;
import net.atlassian.cmathtutor.view.ProjectView;
import javafx.scene.layout.HBox;

public class StartPresenter implements Initializable {

    @FXML
    CreateProjectPresenter createProjectController;
    @FXML
    LoadProjectPresenter loadProjectController;
    @FXML
    HBox root;

    private ChangeListenerRegistryHelper listenerRegistryHelper = new ChangeListenerRegistryHelper();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
	createProjectController.readyToStartModellingPropertyProperty()
		.or(loadProjectController.readyToStartModellingPropertyProperty())
		.addListener(listenerRegistryHelper.registerChangeListener((observable, oldValue, newValue) -> {
		    if (newValue) {
			switchToMainWindow();
		    }
		}));
    }

    private void switchToMainWindow() {
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
