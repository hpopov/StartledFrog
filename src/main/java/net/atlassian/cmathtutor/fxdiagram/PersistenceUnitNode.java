package net.atlassian.cmathtutor.fxdiagram;

import java.util.Optional;

import de.fxdiagram.core.XNode;
import de.fxdiagram.core.model.ModelElementImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import lombok.NoArgsConstructor;
import net.atlassian.cmathtutor.domain.persistence.descriptor.AbstractDescriptor;
import net.atlassian.cmathtutor.domain.persistence.descriptor.PersistenceUnitDescriptor;
import net.atlassian.cmathtutor.util.Lazy;
import net.atlassian.cmathtutor.view.diagram.PersistenceUnitNodeView;

@NoArgsConstructor
public class PersistenceUnitNode extends XNode {

    private StringProperty persistenceUnitDescriptorId = new SimpleStringProperty(this, "id");
    private PersistenceUnitDescriptor persistenceUnitDescriptor;
    private Lazy<PersistenceUnitNodeView> lazyPersistenceUnitNodeView = Lazy.of(PersistenceUnitNodeView::new);

    public PersistenceUnitNode(PersistenceUnitDescriptor persistenceUnitDescriptor) {
	setPersistenceUnitDescriptor(persistenceUnitDescriptor);
    }

    @Override
    protected Node createNode() {
	PersistenceUnitNodeView persistenceUnitNodeView = lazyPersistenceUnitNodeView.get();
	return persistenceUnitNodeView;
    }

    @Override
    public void doActivate() {
	super.doActivate();
	lazyPersistenceUnitNodeView.get().setPersistenceUnitDescriptor(persistenceUnitDescriptor);
    }

    @Override
    public void populate(ModelElementImpl modelElement) {
	super.populate(modelElement);
	modelElement.addProperty(persistenceUnitDescriptorId, String.class);
    }

    public PersistenceUnitDescriptor getPersistenceUnitDescriptor() {
	return persistenceUnitDescriptor;
    }

    public void setPersistenceUnitDescriptor(PersistenceUnitDescriptor persistenceUnitDescriptor) {
	this.persistenceUnitDescriptor = persistenceUnitDescriptor;
	persistenceUnitDescriptorId
		.setValue(Optional.ofNullable(persistenceUnitDescriptor).map(AbstractDescriptor::getId).orElse(null));
    }

    public StringProperty persistenceUnitDescriptorIdProperty() {
	return this.persistenceUnitDescriptorId;
    }

    public String getPersistenceUnitDescriptorId() {
	return this.persistenceUnitDescriptorIdProperty().get();
    }

    public void setPersistenceUnitDescriptorId(final String persistenceUnitDescriptorId) {
	this.persistenceUnitDescriptorIdProperty().set(persistenceUnitDescriptorId);
    }
}
