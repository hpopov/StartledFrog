package net.atlassian.cmathtutor.fxdiagram;

import de.fxdiagram.core.XRoot;
import de.fxdiagram.core.tools.XDiagramTool;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public abstract class AbstractCreateShapeTool implements XDiagramTool {

    protected XRoot xRoot;
    protected StartledFrogDiagram diagram;
    private EventHandler<MouseEvent> eventHandler = this::inactivateToolOnRightClick;

    public AbstractCreateShapeTool(XRoot xRoot) {
	this.xRoot = xRoot;
	diagram = (StartledFrogDiagram) xRoot.getDiagram();
    }

    @Override
    public boolean activate() {
	xRoot.getScene().addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);
	return true;
    }

    private void inactivateToolOnRightClick(MouseEvent event) {
	if (event.getButton() == MouseButton.SECONDARY) {
	    xRoot.restoreDefaultTool();
	    event.consume();
	}
    }

    @Override
    public boolean deactivate() {
	xRoot.getScene().removeEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);
	return true;
    }
}
