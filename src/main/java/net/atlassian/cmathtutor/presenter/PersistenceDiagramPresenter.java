package net.atlassian.cmathtutor.presenter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import de.fxdiagram.core.XDiagram;
import de.fxdiagram.core.XNode;
import de.fxdiagram.core.XRoot;
import de.fxdiagram.core.model.DomainObjectProvider;
import de.fxdiagram.core.model.ModelLoad;
import de.fxdiagram.core.tools.actions.CenterAction;
import de.fxdiagram.core.tools.actions.DeleteAction;
import de.fxdiagram.core.tools.actions.LoadAction;
import de.fxdiagram.core.tools.actions.RedoAction;
import de.fxdiagram.core.tools.actions.RevealAction;
import de.fxdiagram.core.tools.actions.SaveAction;
import de.fxdiagram.core.tools.actions.SelectAllAction;
import de.fxdiagram.core.tools.actions.UndoAction;
import de.fxdiagram.core.tools.actions.ZoomToFitAction;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.domain.persistence.descriptor.IllegalOperationException;
import net.atlassian.cmathtutor.domain.persistence.descriptor.PersistenceDescriptor;
import net.atlassian.cmathtutor.domain.persistence.descriptor.PersistenceUnitDescriptor;
import net.atlassian.cmathtutor.domain.persistence.model.PersistenceModel;
import net.atlassian.cmathtutor.fxdiagram.PersistenceUnitNode;
import net.atlassian.cmathtutor.fxdiagram.StartledFrogDiagram;
import net.atlassian.cmathtutor.service.PersistenceDomainService;
import net.atlassian.cmathtutor.util.AutoDisposableListener;

@Slf4j
@Getter
public class PersistenceDiagramPresenter implements Initializable {

    @FXML
    XRoot root;

    @Inject
    private PersistenceDomainService persistenceService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
	PersistenceModel persistenceModel = persistenceService.loadPersistenceModel();
	File file = persistenceService.getPersistenceDiagramFile();
	PersistenceDescriptor wrappedModel;
	try {
	    wrappedModel = PersistenceDescriptor.wrap(persistenceModel);
	} catch (IllegalOperationException e) {
	    throw new IllegalStateException(e);
	}
	StartledFrogDiagram diagram;
	if (file.exists()) {
	    diagram = loadDiagramFromFile(file, wrappedModel);
	} else {
	    log.warn("FXDiagram file does not exist. Creating entirely new diagram..");
	    diagram = new StartledFrogDiagram();
	    diagram.setPersistenceDescriptor(wrappedModel);
	    root.setRootDiagram(diagram);
	}
	Set<String> assignedPersistenceUnitDescriptorIds = diagram.getPersistenceUnitNodes().stream()
		.map(PersistenceUnitNode::getPersistenceUnitDescriptorId)
		.collect(Collectors.toSet());
	wrappedModel.getPersistenceUnitDescriptors().stream()
		.filter(pud -> !assignedPersistenceUnitDescriptorIds.contains(pud.getId()))
		.forEach(pud -> createNewPersistenceUnitNode(diagram, pud));

//	attemptToUseMapping(diagram);
//	OpenableDiagramNode persistenceView = new OpenableDiagramNode("Persistence View");
//	persistenceView.setLayoutX(300);
//	persistenceView.setLayoutY(200);
//	XDiagram persistenceDiagram = new XDiagram();
//	persistenceDiagram.getNodes().add(new SimpleNode("Some node"));
//	persistenceView.setInnerDiagram(persistenceDiagram);
//	OpenableDiagramNode configView = new OpenableDiagramNode("Configuration View");
//	configView.setLayoutX(600);
//	configView.setLayoutY(400);
//	XDiagram configDiagram = new XDiagram();
//	configView.setInnerDiagram(configDiagram);
//	diagram.getNodes().addAll(persistenceView, configView);
//	XConnection connection = new XConnection(persistenceView, configView);
//	connection.setKind(XConnection.Kind.RECTILINEAR);
//	diagram.getConnections().add(connection);

	root.getDiagramActionRegistry().operator_add(Arrays.asList(
		new CenterAction(),
//		new ExitAction(),
		new DeleteAction(),
//		new LayoutAction(LayoutType.DOT), <-- is throwing java.lang.NoClassDefFoundError: org/eclipse/emf/ecore/EObject
		new RedoAction(),
		new UndoAction(),
		new RevealAction(),
		new LoadAction(),
		new SaveAction(),
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

    private StartledFrogDiagram loadDiagramFromFile(File file, PersistenceDescriptor persistenceDescriptor) {
	Object node = loadDiagram(file);
	if (false == node instanceof XRoot) {
	    throw new IllegalStateException("Loaded diagram node must be successor of XRoot!");
	}
	ObservableList<DomainObjectProvider> domainObjectProviders = ((XRoot) node).getDomainObjectProviders();
	root.replaceDomainObjectProviders(domainObjectProviders);
	XDiagram diagram = ((XRoot) node).getDiagram();
	if (false == diagram instanceof StartledFrogDiagram) {
	    throw new IllegalStateException("Loaded diagram must be startledFrog diagram!");
	}
	StartledFrogDiagram frogDiagram = (StartledFrogDiagram) diagram;
	log.info("Loaded startled frog diagram {}", diagram);
	String persistenceDescriptorId = frogDiagram.getPersistenceDescriptorId();
	if (false == persistenceDescriptor.getId().equals(persistenceDescriptorId)) {
	    throw new IllegalStateException("loaded descriptor id and model id must be equal!");
	}
	frogDiagram.setPersistenceDescriptor(persistenceDescriptor);
	root.setRootDiagram(frogDiagram);
	String path = file.getPath();
	root.setFileName(path);
	return frogDiagram;
    }

    private Object loadDiagram(@NonNull File file) {
	ModelLoad modelLoad = new ModelLoad();
	FileInputStream fileInputStream;
	try {
	    fileInputStream = new FileInputStream(file);
	    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
	    return modelLoad.load(inputStreamReader);
	} catch (FileNotFoundException | UnsupportedEncodingException e) {
	    throw new IllegalStateException(e);
	}
    }

}
