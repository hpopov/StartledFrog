package net.atlassian.cmathtutor.fxdiagram;

import de.fxdiagram.core.XRoot;
import de.fxdiagram.core.command.AnimationCommand;
import de.fxdiagram.core.tools.XDiagramTool;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.domain.persistence.descriptor.AssociationDescriptor;
import net.atlassian.cmathtutor.presenter.NewAssociationConnectionPresenter;
import net.atlassian.cmathtutor.view.NewAssociationConnectionView;

@Slf4j
public class CreateAssociationConnectionTool extends AbstractCreateShapeTool implements XDiagramTool {

    private EventHandler<MouseEvent> eventHandler = this::handleMouseEvent;

    private ImageCursor createAssociationConnectionCursor = new ImageCursor(new Image("/images/Associations.png"));

    public CreateAssociationConnectionTool(XRoot xRoot) {
	super(xRoot);
    }

    @Override
    public boolean activate() {
	super.activate();
	log.debug("Activating...");
	xRoot.getScene().addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);
	xRoot.getScene().setCursor(createAssociationConnectionCursor);
	return true;
    }

    private void handleMouseEvent(MouseEvent event) {
	if (MouseButton.PRIMARY != event.getButton()) {
	    return;
	}
	PersistenceUnitNode targetPersistenceUnitNode = getTargetPersistenceUnitNode(event);
	log.debug("Target persistence unit node is: {}", targetPersistenceUnitNode);
	AssociationNodesContainer nodesContainer = AssociationNodesContainer.add(targetPersistenceUnitNode);
	if (nodesContainer == null) {
	    log.debug("Selected first node only, waiting for the second one");
	    return;
	}
	log.info("Both nodes are selected for association creation: {}", nodesContainer);
	AssociationDescriptor associationDescriptor = createAssociationDescriptor(nodesContainer);
	if (associationDescriptor == null) {
	    log.info("Association descriptor has not been created. Nothing to do");
	    return;
	}
	AssociationConnection associationConnection = new AssociationConnection(nodesContainer.getContainerNode(),
		nodesContainer.getElementNode(),
		associationDescriptor);
	AnimationCommand command = StartledFrogAddRemoveCommand.newStartledFrogAddCommand(diagram,
		associationConnection);
	xRoot.getCommandStack().execute(command);
	xRoot.restoreDefaultTool();
    }

    private PersistenceUnitNode getTargetPersistenceUnitNode(MouseEvent event) {
	EventTarget target = event.getTarget();
	log.debug("clicked on target: {}", target);
	if (target instanceof Node) {
	    return getTargetPersistenceUnitNode((Node) target);
	}
	return null;
    }

    private PersistenceUnitNode getTargetPersistenceUnitNode(Node node) {
	if (node == xRoot) {
	    return null;
	}
	if (node instanceof PersistenceUnitNode) {
	    return (PersistenceUnitNode) node;
	}
	return getTargetPersistenceUnitNode(node.getParent());
    }

    private AssociationDescriptor createAssociationDescriptor(AssociationNodesContainer nodesContainer) {
	NewAssociationConnectionView newAssociationConnectionView = new NewAssociationConnectionView();
	NewAssociationConnectionPresenter presenter = newAssociationConnectionView.getPresenter();
	presenter.setPersistenceDescriptor(diagram.getPersistenceDescriptor());
	presenter.setContainerNode(nodesContainer.getContainerNode());
	presenter.setElementNode(nodesContainer.getElementNode());
	Stage stage = new Stage(StageStyle.DECORATED);
	stage.initModality(Modality.WINDOW_MODAL);
	Scene scene = new Scene(newAssociationConnectionView.getView());
	stage.setScene(scene);
	stage.initOwner(xRoot.getScene().getWindow());
	stage.showAndWait();
	return presenter.getAssociationDescriptor();
    }

    @Override
    public boolean deactivate() {
	super.deactivate();
	log.debug("Deactivating...");
	xRoot.getScene().removeEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);
	xRoot.getScene().setCursor(Cursor.DEFAULT);
	return true;
    }

    @ToString
    @NoArgsConstructor
    @Getter
    private static class AssociationNodesContainer {
	private PersistenceUnitNode containerNode;
	private PersistenceUnitNode elementNode;

	private static AssociationNodesContainer currentContainer = new AssociationNodesContainer();

	public static AssociationNodesContainer add(PersistenceUnitNode node) {
	    AssociationNodesContainer currentContainer = AssociationNodesContainer.currentContainer;
	    if (currentContainer.addNode(node)) {
		AssociationNodesContainer.currentContainer = new AssociationNodesContainer();
		return currentContainer;
	    }
	    return null;
	}

	private boolean addNode(PersistenceUnitNode node) {
	    if (containerNode == null) {
		containerNode = node;
		return false;
	    }
	    if (elementNode == null) {
		elementNode = node;
		return true;
	    }
	    return true;
	}
    }

}
