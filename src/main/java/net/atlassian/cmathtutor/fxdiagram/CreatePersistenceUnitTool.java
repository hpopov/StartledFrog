package net.atlassian.cmathtutor.fxdiagram;

import de.fxdiagram.core.XRoot;
import de.fxdiagram.core.command.AnimationCommand;
import de.fxdiagram.core.tools.XDiagramTool;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.domain.persistence.descriptor.PersistenceUnitDescriptor;
import net.atlassian.cmathtutor.presenter.NewPersistenceUnitPresenter;
import net.atlassian.cmathtutor.view.NewPersistenceUnitView;

@Slf4j
public class CreatePersistenceUnitTool implements XDiagramTool {

    private XRoot xRoot;
    private EventHandler<MouseEvent> eventHandler;
    private StartledFrogDiagram diagram;

    private ImageCursor createPersistenceUnitCursor = new ImageCursor(new Image("/images/entity.png"));

    public CreatePersistenceUnitTool(XRoot xRoot) {
	this.xRoot = xRoot;
	diagram = (StartledFrogDiagram) xRoot.getDiagram();
    }

    @Override
    public boolean activate() {
	log.debug("Activating...");
	eventHandler = this::handleMouseEvent;
	xRoot.getScene().addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);
	xRoot.getScene().setCursor(createPersistenceUnitCursor);
	return true;
    }

    private void handleMouseEvent(MouseEvent event) {
	log.debug("Mouse clicked at {},{}", event.getX(), event.getY());
	PersistenceUnitDescriptor persistenceUnitDescriptor = createPersistenceUnitDescriptor();
//	try {
//	    persistenceUnitDescriptor = diagram.getPersistenceDescriptor().addNewPersistenceUnit("New Unit!");
//	} catch (IllegalOperationException e) {
//	    log.error("Unable to create new unit: {}", e.getMessage());
//	    return;
//	}
	if (persistenceUnitDescriptor == null) {
	    log.info("Persistence unit descriptor has not been created. Nothing to do");
	    return;
	}
	PersistenceUnitNode persistenceUnitNode = new PersistenceUnitNode(persistenceUnitDescriptor);
	persistenceUnitNode.setLayoutX(event.getX());
	persistenceUnitNode.setLayoutY(event.getY());
	AnimationCommand command = StartledFrogAddRemoveCommand.newStartledFrogAddCommand(diagram, persistenceUnitNode);
	xRoot.getCommandStack().execute(command);
	xRoot.restoreDefaultTool();
    }

    private PersistenceUnitDescriptor createPersistenceUnitDescriptor() {
	NewPersistenceUnitView newPersistenceUnitView = new NewPersistenceUnitView();
	NewPersistenceUnitPresenter presenter = newPersistenceUnitView.getPresenter();
	presenter.setPersistenceDescriptor(diagram.getPersistenceDescriptor());
	Stage stage = new Stage(StageStyle.DECORATED);
	stage.initModality(Modality.WINDOW_MODAL);
	Scene scene = new Scene(newPersistenceUnitView.getView());
	stage.setScene(scene);
	stage.initOwner(xRoot.getScene().getWindow());
	stage.showAndWait();
	return presenter.getPersistenceUnitDescriptor();
    }

    @Override
    public boolean deactivate() {
	log.debug("Deactivating...");
	xRoot.getScene().removeEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);
	xRoot.getScene().setCursor(Cursor.DEFAULT);
	return true;
    }

}
