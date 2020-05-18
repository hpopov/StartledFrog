package net.atlassian.cmathtutor.view;

import java.util.Arrays;
import java.util.Objects;

import com.airhacks.afterburner.views.FXMLView;

import de.fxdiagram.core.XDiagram;
import de.fxdiagram.core.XRoot;
import de.fxdiagram.core.tools.actions.CenterAction;
import de.fxdiagram.core.tools.actions.DeleteAction;
import de.fxdiagram.core.tools.actions.RedoAction;
import de.fxdiagram.core.tools.actions.RevealAction;
import de.fxdiagram.core.tools.actions.SelectAllAction;
import de.fxdiagram.core.tools.actions.UndoAction;
import de.fxdiagram.core.tools.actions.ZoomToFitAction;
import javafx.scene.Parent;
import net.atlassian.cmathtutor.util.AutoDisposableListener;

public class PersistenceDiagramView extends FXMLView {

    @Override
    public Parent getView() {
	XRoot parent = (XRoot) super.getView();
	return modifyView(parent);
    }

    public static Parent modifyView(XRoot xRoot) {
	XDiagram diagram = new XDiagram();

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

	xRoot.setRootDiagram(diagram);
	xRoot.getDiagramActionRegistry().operator_add(Arrays.asList(
		new CenterAction(),
//		new ExitAction(),
		new DeleteAction(),
//		new LayoutAction(LayoutType.DOT), <-- is throwing java.lang.NoClassDefFoundError: org/eclipse/emf/ecore/EObject
		new RedoAction(),
		new UndoAction(),
		new RevealAction(),
//			new LoadAction,
//			new SaveAction,
		new SelectAllAction(),
		new ZoomToFitAction()// ,
//			new NavigatePreviousAction,
//			new NavigateNextAction,
//			new OpenAction,
//			new CloseAction,
//			new UndoRedoPlayerAction
	));
	xRoot.sceneProperty().addListener(new AutoDisposableListener<>(Objects::nonNull, () -> xRoot.activate()));

	return xRoot;
    }

//    private static void attemptToUseMapping(XDiagram diagram) {
//	XNode simpleNode = new SimpleNode(new DomainObjectDescriptor() {
//
//	    @Override
//	    public void postLoad() {
//		log.debug("DomainObjectDescriptor#postLoad was invoked");
//	    }
//
//	    @Override
//	    public void populate(ModelElementImpl modelElementImpl) {
//	    }
//
//	    @Override
//	    public String getName() {
//		return "DoD name";
//	    }
//	});
//
//	StartledFrogConfig mappingConfig = new StartledFrogConfig();
//	mappingConfig.setLabel("Default Startled Frog");
//	XDiagramConfig.Registry.getInstance().addConfig(mappingConfig);
//	IMappedElementDescriptorProvider provider = mappingConfig.getDomainObjectProvider();
//	Object domainObject = new CreateProjectProperties();
//	AbstractMapping<?> mapping = mappingConfig.getMappingByID(StartledFrogConfig.DEFAULT_MAPPING);
//	BaseClassNode<Object> baseClassNode = new BaseClassNode<Object>(
//		provider.createMappedElementDescriptor(domainObject, mapping));
//
//	diagram.getNodes().add(simpleNode);
//	diagram.getNodes().add(baseClassNode);
//    }

}
