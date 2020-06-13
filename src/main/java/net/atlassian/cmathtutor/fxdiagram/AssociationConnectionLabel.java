package net.atlassian.cmathtutor.fxdiagram;

import de.fxdiagram.core.XConnection;
import de.fxdiagram.core.XConnectionLabel;
import de.fxdiagram.core.model.ModelElementImpl;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.text.Font;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AssociationConnectionLabel extends XConnectionLabel {

    private static final int FONT_SIZE = 18;
    private BooleanProperty container = new SimpleBooleanProperty(this, "isContainer");

    public AssociationConnectionLabel(XConnection connection, boolean isContainer) {
	super(connection);
	this.container.set(isContainer);
	if (isContainer) {
	    setPosition(0.8);
	} else {
	    setPosition(0.2);
	}
	getText().setFont(Font.font(getText().getFont().getName(), FONT_SIZE));
    }

    @Override
    public void postLoad() {
	super.postLoad();
	getText().setFont(Font.font(getText().getFont().getName(), FONT_SIZE));
    }

    @Override
    public void populate(ModelElementImpl modelElement) {
	super.populate(modelElement);
	modelElement.addProperty(container, Boolean.class);
    }

    public BooleanProperty containerProperty() {
	return this.container;
    }

    public boolean isContainer() {
	return this.containerProperty().get();
    }

    public void setContainer(final boolean isContainer) {
	this.containerProperty().set(isContainer);
    }

}
