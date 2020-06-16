package net.atlassian.cmathtutor.presenter;

import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import de.fxdiagram.core.XNode;
import de.fxdiagram.core.XRoot;
import de.fxdiagram.core.tools.actions.CenterAction;
import de.fxdiagram.core.tools.actions.RedoAction;
import de.fxdiagram.core.tools.actions.RevealAction;
import de.fxdiagram.core.tools.actions.SelectAllAction;
import de.fxdiagram.core.tools.actions.UndoAction;
import de.fxdiagram.core.tools.actions.ZoomToFitAction;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.domain.persistence.descriptor.AssociationDescriptor;
import net.atlassian.cmathtutor.domain.persistence.descriptor.IllegalOperationException;
import net.atlassian.cmathtutor.domain.persistence.descriptor.PersistenceDescriptor;
import net.atlassian.cmathtutor.domain.persistence.descriptor.PersistenceUnitDescriptor;
import net.atlassian.cmathtutor.domain.persistence.model.PersistenceModel;
import net.atlassian.cmathtutor.fxdiagram.AssociationConnection;
import net.atlassian.cmathtutor.fxdiagram.PersistenceUnitNode;
import net.atlassian.cmathtutor.fxdiagram.StartledFrogDeleteAction;
import net.atlassian.cmathtutor.fxdiagram.StartledFrogDiagram;
import net.atlassian.cmathtutor.fxdiagram.StartledFrogSaveAction;
import net.atlassian.cmathtutor.service.PersistenceDomainService;
import net.atlassian.cmathtutor.util.AutoDisposableListener;

@Slf4j
@Getter
public class PersistenceDiagramPresenter implements Initializable {

    @FXML
    XRoot root;

    @Inject
    private PersistenceDomainService persistenceService;

    private PersistenceModel persistenceModel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
	persistenceModel = persistenceService.loadPersistenceModel();
	PersistenceDescriptor wrappedModel;
	try {
	    wrappedModel = PersistenceDescriptor.wrap(persistenceModel);
	} catch (IllegalOperationException e) {
	    throw new IllegalStateException(e);
	}
	StartledFrogDiagram diagram = persistenceService.getProjectDiagram(root, wrappedModel);

	Set<String> assignedPersistenceUnitDescriptorIds = diagram.getPersistenceUnitNodes().stream()
		.map(PersistenceUnitNode::getPersistenceUnitDescriptorId)
		.collect(Collectors.toSet());
	wrappedModel.getPersistenceUnitDescriptors().stream()
		.filter(pud -> !assignedPersistenceUnitDescriptorIds.contains(pud.getId()))
		.forEach(pud -> createNewPersistenceUnitNode(diagram, pud));

	Set<String> assignedAssociationDescriptorIds = diagram.getAssociationConnections().stream()
		.map(AssociationConnection::getAssociationDescriptorId)
		.peek(id -> log.debug("Assigned association connection ID is {}", id))
		.collect(Collectors.toSet());
	wrappedModel.getAssociationDescriptors().stream()
		.filter(ad -> !assignedAssociationDescriptorIds.contains(ad.getId()))
		.forEach(ad -> createNewAssociationConnection(diagram, ad));

	root.getDiagramActionRegistry().operator_add(Arrays.asList(
		new CenterAction(),
//		new ExitAction(),
		new StartledFrogDeleteAction(),
//		new LayoutAction(LayoutType.DOT), <-- is throwing java.lang.NoClassDefFoundError: org/eclipse/emf/ecore/EObject
		new RedoAction(),
		new UndoAction(),
		new RevealAction(),
//		new LoadAction(),
		new StartledFrogSaveAction(persistenceService, persistenceModel),
		new SelectAllAction(),
		new ZoomToFitAction()// ,
//			new NavigatePreviousAction,
//			new NavigateNextAction,
//			new OpenAction,
//			new CloseAction,
//			new UndoRedoPlayerAction
	));
	root.sceneProperty().addListener(new AutoDisposableListener<>(Objects::nonNull, () -> root.activate()));
    }

    private void createNewPersistenceUnitNode(StartledFrogDiagram diagram, PersistenceUnitDescriptor descriptor) {
	XNode persistenceunitNode = new PersistenceUnitNode(descriptor);
	diagram.getNodes().add(persistenceunitNode);
    }

    private void createNewAssociationConnection(StartledFrogDiagram diagram,
	    AssociationDescriptor associationDescriptor) {
	log.info("Creating new association connection: id ({}), {}->{}", associationDescriptor.getId(),
		associationDescriptor.getElementAttribute(), associationDescriptor.getContainerAttribute());
	XNode sourceNode = diagram.getPersistenceUnitNodeById(
		associationDescriptor.getContainerAttribute().getParentClassifier().getId());
	XNode targetNode = diagram
		.getPersistenceUnitNodeById(associationDescriptor.getElementAttribute().getParentClassifier().getId());
	if (sourceNode == null || targetNode == null) {
	    throw new IllegalStateException(
		    "Both source and target nodes must exist at diagram before association creation");
	}
	AssociationConnection connection = new AssociationConnection(sourceNode, targetNode, associationDescriptor);
	diagram.getConnections().add(connection);
    }
}
