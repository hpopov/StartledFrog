package net.atlassian.cmathtutor.presenter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

public class LaunchPresenter implements Initializable {

    @FXML
    VBox root;
    @FXML
    ProgressIndicator progressIndicator;
    @FXML
    Label appStateLabel;
    @FXML
    Button stopApplicationButton;
    @FXML
    Button runApplicationButton;
    @FXML
    VBox logEntriesVBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    public void stopApplication() {
    }

    @FXML
    public void runApplication() {
    }
}
