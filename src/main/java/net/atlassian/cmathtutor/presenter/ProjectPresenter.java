package net.atlassian.cmathtutor.presenter;

import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;

import javax.inject.Inject;

import de.fxdiagram.core.XRoot;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.Event;
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
import net.atlassian.cmathtutor.domain.configuration.model.GlobalConfigurationModel;
import net.atlassian.cmathtutor.domain.persistence.descriptor.IllegalOperationException;
import net.atlassian.cmathtutor.domain.persistence.model.PersistenceModel;
import net.atlassian.cmathtutor.domain.persistence.translate.PersistenceModelTranslator;
import net.atlassian.cmathtutor.fxdiagram.CreateAssociationConnectionTool;
import net.atlassian.cmathtutor.fxdiagram.CreatePersistenceUnitTool;
import net.atlassian.cmathtutor.helper.ChangeListenerRegistryHelper;
import net.atlassian.cmathtutor.model.Project;
import net.atlassian.cmathtutor.service.ConfigurationDomainService;
import net.atlassian.cmathtutor.service.PersistenceDomainService;
import net.atlassian.cmathtutor.service.ProjectService;
import net.atlassian.cmathtutor.util.AutoDisposableListener;
import net.atlassian.cmathtutor.view.PersistenceDiagramView;

@Slf4j
public class ProjectPresenter implements Initializable {

    private static final int SPLIT_PANE_BORDER_WIDTH = 5;
    private static final double SPLIT_PANE_FIRST_WIDTH_COEFFICIENT = 0.17;
    private static final double MENU_BAR_BORDERS = 2;
    private static final double SCREEN_Y_CORRECTION = -4;
    private static final double SCREEN_X_CORRECTION = -6;

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
    VBox launch;
    @FXML
    MenuBar menuBar;
    @FXML
    TilePane persistenceToolbarTilePane;
    @FXML
    Button launchViewButton;

    @FXML
    ConfigurationPresenter configurationController;

    @Inject
    private ProjectService projectService;
    @Inject
    private PersistenceDomainService persistenceDomainService;
    @Inject
    private ConfigurationDomainService configurationDomainService;

    private PersistenceModel persistenceModel;
    private GlobalConfigurationModel configurationModel;

    private ChangeListenerRegistryHelper changeListenerRegistryHelper = new ChangeListenerRegistryHelper();
    private Stage stage;
    private XRoot xRoot;
    private CreatePersistenceUnitTool createPersistenceUnitTool;
    private CreateAssociationConnectionTool createAssociationTool;

    @Override
    public void initialize(URL var1, ResourceBundle var2) {
	for (Button button : Arrays.asList(persistentViewButton, configurationViewButton, launchViewButton)) {
	    button.prefWidthProperty().bind(viewButtonsContainer.widthProperty());
	    button.prefHeightProperty().bind(button.widthProperty());
	}
	for (VBox view : Arrays.asList(configuration, launch)) {
	    view.prefWidthProperty().bind(viewPane.widthProperty());
	    view.prefHeightProperty().bind(viewPane.heightProperty());
	}

	viewPane.sceneProperty().addListener(
		new AutoDisposableListener<>(Objects::nonNull, () -> Platform.runLater(this::initializeStage)));
    }

    private void initializeStage() {
	Scene parentScene = viewPane.getScene();
	stage = new Stage(StageStyle.UNDECORATED);
	stage.initModality(Modality.NONE);
	PersistenceDiagramView view = new PersistenceDiagramView();
	xRoot = (XRoot) view.getView();
	Scene persistentViewScene = new Scene(xRoot);
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

	PersistenceDiagramPresenter presenter = view.getPresenter();
	persistenceModel = presenter.getPersistenceModel();// TODO: consider extracting these lines to
							   // fx service
	configurationModel = configurationController.getGlobalConfigurationModel();
//	configurationModel = configurationDomainService.loadConfigurationModel();
//	StartledFrogDiagram diagram = (StartledFrogDiagram) xRoot.getDiagram();
	createPersistenceUnitTool = new CreatePersistenceUnitTool(xRoot);
	createAssociationTool = new CreateAssociationConnectionTool(xRoot);
    }

    @FXML
    public void switchToPersistentView() {
	configuration.setVisible(false);
	launch.setVisible(false);
	stage.show();
	persistenceToolbarTilePane.setVisible(true);
    }

    @FXML
    public void switchToConfigurationView() {
	stage.hide();
	launch.setVisible(false);
	configuration.setVisible(true);
	persistenceToolbarTilePane.setVisible(false);
    }

    @FXML
    public void switchToLaunchView() {
	stage.hide();
	launch.setVisible(true);
	configuration.setVisible(false);
	persistenceToolbarTilePane.setVisible(false);
    }

    @FXML
    public void saveModels() {
	if (persistenceModel == null) {
	    log.warn("persistence model is null. Going to load it...");
	    persistenceModel = persistenceDomainService.loadPersistenceModel();
	}
	persistenceDomainService.persistModel(persistenceModel);
	if (configurationModel == null) {
	    log.warn("global configuration model is null. Going to load it...");
	    configurationModel = configurationDomainService.loadConfigurationModel();
	}
	configurationDomainService.persistModel(configurationModel);
    }

    @FXML
    public void generateProgram() {
	// FIXME
	if (persistenceModel == null || configurationModel == null) {
	    saveModels();
	}
	Service<Void> service = new Service<Void>() {

	    @Override
	    protected Task<Void> createTask() {
		return new Task<Void>() {

		    @Override
		    protected Void call() throws Exception {
			Project currentProject = projectService.getCurrentProject();
			PersistenceModelTranslator translator = new PersistenceModelTranslator(currentProject);
			translator.translate(persistenceModel);
			persistenceDomainService.persistLiquibaseChangeLog(translator.getTranslatedChangeLog());
			persistenceDomainService.rewriteTranslatedClasses(translator.getTranslatedClasses());
			configurationDomainService.rewriteConfigurationProperties(configurationModel);
			return null;
		    }
		};
	    }
	};
	service.setOnScheduled(event -> log.info("Programm has been generated sucessfully"));
	service.setOnFailed(event -> log.error("Programm generation failed: {}", event.getSource().getException()));
	service.start();
    }

    @FXML
    public void runGeneratedProgram() {
//	Runtime runtime = Runtime.getRuntime();
//	runtime.
    }

    @FXML
    public void exitStartledFrog() {
    }

    @FXML
    public void onCloseMenuValidation(Event event) {
	log.debug("onCloseMenu validation triggered");
    }

    @FXML
    public void selectNewPersistenceUnitTool() throws IllegalOperationException {
//	CarusselChoice graphics = new CarusselChoice();
//	NodeChooser tool = new NodeChooser(diagram, new Point2D(200, 200), graphics, false);
//	tool.addChoice(new PersistenceUnitNode(diagram.getPersistenceDescriptor().addNewPersistenceUnit("New Unit")));
	xRoot.setCurrentTool(createPersistenceUnitTool);
    }

    @FXML
    public void selectNewAssociationTool() {
	xRoot.setCurrentTool(createAssociationTool);
    }

}
