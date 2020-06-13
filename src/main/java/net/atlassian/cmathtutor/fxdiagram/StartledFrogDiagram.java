package net.atlassian.cmathtutor.fxdiagram;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.fxdiagram.core.XDiagram;
import de.fxdiagram.core.XNode;
import de.fxdiagram.core.model.ModelElementImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener.Change;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.domain.persistence.descriptor.AbstractDescriptor;
import net.atlassian.cmathtutor.domain.persistence.descriptor.PersistenceDescriptor;

@Slf4j
@ToString
public class StartledFrogDiagram extends XDiagram {

    private StringProperty persistenceDescriptorId = new SimpleStringProperty(this, "id");
    private PersistenceDescriptor persistenceDescriptor;

    private Map<String, PersistenceUnitNode> idToPersistenceUnitNodes = new HashMap<>();

    public StartledFrogDiagram() {
	nodesProperty().addListener((Change<? extends XNode> change) -> {
	    log.debug("The diagram nodes are changed");
	    while (change.next()) {
		log.debug("The nodes are changed: wasAdded:{}, wasRemoved:{}", change.wasAdded(), change.wasRemoved());
		if (change.wasAdded()) {
		    getPersistenceUnitNodesStreamFromNodes(change.getAddedSubList())
			    .forEach(puNode -> {
				log.debug("Adding new node to diagram register: {} -> {}",
					puNode.getPersistenceUnitDescriptorId(), puNode);
				idToPersistenceUnitNodes.put(puNode.getPersistenceUnitDescriptorId(), puNode);
			    });
		}
		if (change.wasRemoved()) {
		    getPersistenceUnitNodesStreamFromNodes(change.getRemoved())
			    .forEach(
				    puNode -> idToPersistenceUnitNodes.remove(puNode.getPersistenceUnitDescriptorId()));
		}
	    }
	});
    }

    public void setPersistenceDescriptor(PersistenceDescriptor persistenceDescriptor) {
	this.persistenceDescriptor = persistenceDescriptor;
	persistenceDescriptorId
		.setValue(Optional.ofNullable(persistenceDescriptor).map(AbstractDescriptor::getId).orElse(null));
	getPersistenceUnitNodesStream()
		.forEach(puNode -> puNode.setPersistenceUnitDescriptor(persistenceDescriptor
			.getPersistenceUnitDescriptorById(puNode.getPersistenceUnitDescriptorId())));
	getAssociationConnectionsStream()
		.forEach(aConn -> aConn.setAssociationDescriptor(
			persistenceDescriptor.getAssociationDescriptorById(aConn.getAssociationDescriptorId())));
    }

    private Stream<PersistenceUnitNode> getPersistenceUnitNodesStream() {
	return getPersistenceUnitNodesStreamFromNodes(getNodes());
    }

    private Stream<PersistenceUnitNode> getPersistenceUnitNodesStreamFromNodes(List<? extends XNode> nodes) {
	return nodes.stream()
		.filter(xNode -> xNode instanceof PersistenceUnitNode)
		.map(PersistenceUnitNode.class::cast);
    }

    private Stream<AssociationConnection> getAssociationConnectionsStream() {
	return getConnections().filtered(conn -> conn instanceof AssociationConnection).stream()
		.map(AssociationConnection.class::cast);
    }

    public Collection<PersistenceUnitNode> getPersistenceUnitNodes() {
	return Collections.unmodifiableCollection(idToPersistenceUnitNodes.values());
    }

    public Collection<AssociationConnection> getAssociationConnections() {
	return getAssociationConnectionsStream().collect(Collectors.toList());
    }

    public PersistenceUnitNode getPersistenceUnitNodeById(String persistenceUnitDescriptorId) {
	return idToPersistenceUnitNodes.get(persistenceUnitDescriptorId);
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