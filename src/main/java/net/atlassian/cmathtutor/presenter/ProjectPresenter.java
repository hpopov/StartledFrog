package net.atlassian.cmathtutor.presenter;

import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.VplApplication;
import net.atlassian.cmathtutor.helper.ChangeListenerRegistryHelper;
import net.atlassian.cmathtutor.util.AutoDisposableListener;
import net.atlassian.cmathtutor.view.PersistenceDiagramView;

@Slf4j
public class ProjectPresenter implements Initializable {

    private static final int SPLIT_PANE_BORDER_WIDTH = 5;
    private static final double SPLIT_PANE_FIRST_WIDTH_COEFFICIENT = 0.1;
    private static final double MENU_BAR_BORDERS = 2;
    private static final double SCREEN_Y_CORRECTION = -4;
    private static final double SCREEN_X_CORRECTION = +7;

    @FXML
    StackPane viewPane;
    @FXML
    Button configurationViewButton;
    @FXML
    Button persistentViewButton;
    @FXML
    VBox viewButtonsContainer;
    @FXML
    VBox configuration;
    @FXML
    MenuBar menuBar;
    @FXML
    TilePane persistenceToolbarTilePane;

    private ChangeListenerRegistryHelper changeListenerRegistryHelper = new ChangeListenerRegistryHelper();
    private Stage stage;
//    private XRoot xRoot;

    @Override
    public void initialize(URL var1, ResourceBundle var2) {
	for (Button button : Arrays.asList(persistentViewButton, configurationViewButton)) {
	    button.prefWidthProperty().bind(viewButtonsContainer.widthProperty());
	    button.prefHeightProperty().bind(button.widthProperty());
	}

	viewPane.sceneProperty().addListener(
		new AutoDisposableListener<>(Objects::nonNull, () -> Platform.runLater(this::initializeStage)));
    }

    @FXML
    public void switchToPersistentView() {
	configuration.setVisible(false);
	stage.show();
	persistenceToolbarTilePane.setVisible(true);
    }

    private void initializeStage() {
	Scene parentScene = viewPane.getScene();
	stage = new Stage(StageStyle.UNDECORATED);
	stage.initModality(Modality.NONE);
	PersistenceDiagramView view = new PersistenceDiagramView();
//	xRoot = (XRoot) view.getView();
	Scene persistentViewScene = new Scene(view.getView());
	PerspectiveCamera perspectiveCamera = new PerspectiveCamera();
	persistentViewScene.setCamera(perspectiveCamera);
	stage.setScene(persistentViewScene);
	stage.setTitle(VplApplication.MAIN_TITLE);
	log.debug("Current persistentViewScene is {}", viewPane.getScene());
	Window window = parentScene.getWindow();
	log.debug("window coordinates: ({}, {})", window.getX(), window.getY());
	final double mainTitleHeight = window.getHeight() - parentScene.getHeight();
	log.debug("Window title height is {}", mainTitleHeight);
	stage.setX(window.getX() + parentScene.getWidth() * SPLIT_PANE_FIRST_WIDTH_COEFFICIENT + SPLIT_PANE_BORDER_WIDTH
		+ SCREEN_X_CORRECTION);
	stage.setY(window.getY() + menuBar.getHeight() + MENU_BAR_BORDERS + mainTitleHeight + SCREEN_Y_CORRECTION);
	log.debug("viewPane sizes: {}x{}", viewPane.getWidth(), viewPane.getHeight());
	stage.setWidth(viewPane.getWidth());
	stage.setHeight(viewPane.getHeight());
	window.xProperty().addListener(changeListenerRegistryHelper.registerChangeListener((x, oldX, newX) -> {
	    stage.setX(newX.doubleValue() + parentScene.getWidth() * SPLIT_PANE_FIRST_WIDTH_COEFFICIENT
		    + SPLIT_PANE_BORDER_WIDTH
		    + SCREEN_X_CORRECTION);
	}));
	window.yProperty().addListener(changeListenerRegistryHelper.registerChangeListener((y, oldY, newY) -> {
	    stage.setY(newY.doubleValue() + menuBar.getHeight() + MENU_BAR_BORDERS + mainTitleHeight
		    + SCREEN_Y_CORRECTION);
	}));
	stage.initOwner(window);
    }

    @FXML
    public void switchToConfigurationView() {
	stage.hide();
	configuration.setVisible(true);
	persistenceToolbarTilePane.setVisible(false);
    }

    @FXML
    public void addNewDiagram() {
//	SimpleNode newNode = new SimpleNode("New node!");
//	newNode.setLayoutX(300);
//	newNode.setLayoutY(300);
//	AddRemoveCommand addNewNodeCommand = AddRemoveCommand.newAddCommand(xRoot.getDiagram(), newNode);
//	xRoot.getCommandStack().execute(addNewNodeCommand);
    }

}
