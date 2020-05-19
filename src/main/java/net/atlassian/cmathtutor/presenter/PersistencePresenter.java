package net.atlassian.cmathtutor.presenter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;

public class PersistencePresenter implements Initializable {

    @FXML
    Node root;

    @FXML
    PersistenceDiagramPresenter persistenceDiagramController;

    @Override
    public void initialize(URL var1, ResourceBundle var2) {
//	PersistenceDiagramView.modifyView(persistenceDiagramController.getRoot());
    }
}
