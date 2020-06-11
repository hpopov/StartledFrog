package net.atlassian.cmathtutor.fxdiagram;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.fxdiagram.core.XDiagram;
import de.fxdiagram.core.model.ModelElementImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.atlassian.cmathtutor.domain.persistence.descriptor.AbstractDescriptor;
import net.atlassian.cmathtutor.domain.persistence.descriptor.PersistenceDescriptor;

@ToString
@NoArgsConstructor
public class StartledFrogDiagram extends XDiagram {

    private StringProperty persistenceDescriptorId = new SimpleStringProperty(this, "id");
    private PersistenceDescriptor persistenceDescriptor;

    public void setPersistenceDescriptor(PersistenceDescriptor persistenceDescriptor) {
	this.persistenceDescriptor = persistenceDescriptor;
	persistenceDescriptorId
		.setValue(Optional.ofNullable(persistenceDescriptor).map(AbstractDescriptor::getId).orElse(null));
	getPersistenceUnitNodesStream()
		.peek(puNode -> puNode.setPersistenceUnitDescriptor(persistenceDescriptor
			.getPersistenceUnitDescriptorById(puNode.getPersistenceUnitDescriptorId())))
		.collect(Collectors.toList());
    }

    private Stream<PersistenceUnitNode> getPersistenceUnitNodesStream() {
	return getNodes().filtered(node -> node instanceof PersistenceUnitNode).stream()
		.map(PersistenceUnitNode.class::cast);
    }

    public Collection<PersistenceUnitNode> getPersistenceUnitNodes() {
	return getPersistenceUnitNodesStream().collect(Collectors.toList());
    }

    @Override
    public void populate(ModelElementImpl modelElement) {
	super.populate(modelElement);
	modelElement.addProperty(persistenceDescriptorId, String.class);
    }

    public StringProperty persistenceDescriptorIdProperty() {
	return this.persistenceDescriptorId;
    }

    public String getPersistenceDescriptorId() {
	return this.persistenceDescriptorIdProperty().get();
    }

    public void setPersistenceDescriptorId(final String persistenceDescriptorId) {
	this.persistenceDescriptorIdProperty().set(persistenceDescriptorId);
    }
}