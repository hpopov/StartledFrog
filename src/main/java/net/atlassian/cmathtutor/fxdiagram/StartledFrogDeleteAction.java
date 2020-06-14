package net.atlassian.cmathtutor.fxdiagram;

import java.util.ArrayList;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimaps;

import de.fxdiagram.core.XConnection;
import de.fxdiagram.core.XConnection.Kind;
import de.fxdiagram.core.XControlPoint;
import de.fxdiagram.core.XDiagram;
import de.fxdiagram.core.XNode;
import de.fxdiagram.core.XRoot;
import de.fxdiagram.core.XShape;
import de.fxdiagram.core.command.AbstractAnimationCommand;
import de.fxdiagram.core.command.CommandStack;
import de.fxdiagram.core.command.ParallelAnimationCommand;
import de.fxdiagram.core.command.RemoveControlPointCommand;
import de.fxdiagram.core.command.ResetConnectionCommand;
import de.fxdiagram.core.extensions.CoreExtensions;
import de.fxdiagram.core.tools.actions.DeleteAction;
import javafx.collections.ObservableList;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.domain.persistence.descriptor.AssociationDescriptor;
import net.atlassian.cmathtutor.domain.persistence.descriptor.PersistenceUnitDescriptor;

@Slf4j
@NoArgsConstructor
public class StartledFrogDeleteAction extends DeleteAction {

    // TODO: refactor and beautify
    @Override
    public void perform(XRoot root) {
	if (false == root.getRootDiagram() instanceof StartledFrogDiagram) {
	    log.debug("Specified xRoot doesn't have startledFrog root diagram. Fallback to super...");
	    super.perform(root);
	    return;
	}
	log.debug("Root diagram is StartledFrogDiagram. Performing actions..");
	Iterable<XShape> _currentSelection = root.getCurrentSelection();
	Set<XShape> elements = IterableExtensions.toSet(_currentSelection);
	Iterable<XNode> _filter = Iterables.filter(elements, XNode.class);
	Iterable<XNode> nodes = this.getAllContainedNodes(_filter);
	Function1<XNode, ObservableList<XConnection>> _function = (it) -> {
	    return it.getIncomingConnections();
	};
	Iterable<ObservableList<XConnection>> _map = IterableExtensions.map(nodes, _function);
	Iterable<XConnection> _flatten = Iterables.concat(_map);
	Function1<XNode, ObservableList<XConnection>> _function_1 = (it) -> {
	    return it.getOutgoingConnections();
	};
	Iterable<ObservableList<XConnection>> _map_1 = IterableExtensions.map(nodes, _function_1);
	Iterable<XConnection> _flatten_1 = Iterables.concat(_map_1);
	Iterable<XConnection> _plus = Iterables.concat(_flatten, _flatten_1);
	Function1<XShape, Boolean> _function_2 = (it) -> {
	    return it instanceof XNode || it instanceof XConnection;
	};
	Iterable<XShape> _filter_1 = IterableExtensions.filter(elements, _function_2);
	Iterable<XShape> _plus_1 = Iterables.concat(_plus, _filter_1);
	Set<XShape> deleteThem = IterableExtensions.toSet(_plus_1);
	Iterable<XControlPoint> _filter_2 = Iterables.filter(elements, XControlPoint.class);
	Function<XControlPoint, XConnection> _function_3 = (it) -> {
	    return this.getConnection(it);
	};
	ImmutableListMultimap<XConnection, XControlPoint> connection2controlPoints = Multimaps.index(_filter_2,
		_function_3);
	ArrayList<AbstractAnimationCommand> connectionMorphCommands = CollectionLiterals
		.newArrayList(new AbstractAnimationCommand[0]);
	ImmutableSet<XConnection> _keySet = connection2controlPoints.keySet();
	Consumer<XConnection> _function_4 = (connection) -> {
	    boolean _contains = elements.contains(connection);
	    boolean _not = !_contains;
	    if (_not) {
		ImmutableList<XControlPoint> controlPoints = connection2controlPoints.get(connection);
		Kind _kind = connection.getKind();
		boolean _equals = Objects.equal(_kind, Kind.RECTILINEAR);
		if (_equals) {
		    ResetConnectionCommand _resetConnectionCommand = new ResetConnectionCommand(connection);
		    connectionMorphCommands.add(_resetConnectionCommand);
		} else {
		    RemoveControlPointCommand _removeControlPointCommand = new RemoveControlPointCommand(connection,
			    controlPoints);
		    connectionMorphCommands.add(_removeControlPointCommand);
		}
	    }

	};
	_keySet.forEach(_function_4);
	Function<XShape, XDiagram> _function_5 = (it) -> {
	    return CoreExtensions.getDiagram(it);
	};
	ImmutableListMultimap<XDiagram, XShape> diagram2shape = Multimaps.index(deleteThem, _function_5);
	ImmutableSet<XDiagram> _keySet_1 = diagram2shape.keySet();
	Function1<XDiagram, StartledFrogAddRemoveCommand> _function_6 = (diagram) -> {
	    ImmutableList<XShape> _get = diagram2shape.get(diagram);
	    // TODO: change it, now it works only because xRoot contain the only one
	    // StartledFrogDiagram!
	    return StartledFrogAddRemoveCommand.newStartledFrogRemoveCommand((StartledFrogDiagram) diagram,
		    (XShape[]) Conversions.unwrapArray(_get, XShape.class));
	};
	Iterable<StartledFrogAddRemoveCommand> removeCommands = IterableExtensions.map(_keySet_1, _function_6);
	CommandStack _commandStack = root.getCommandStack();
	ParallelAnimationCommand _parallelAnimationCommand = new ParallelAnimationCommand();
	Procedure1<ParallelAnimationCommand> _function_7 = (it) -> {
	    it.operator_add(removeCommands);
	    boolean _isEmpty = connectionMorphCommands.isEmpty();
	    boolean _not = !_isEmpty;
	    if (_not) {
		it.operator_add(connectionMorphCommands);
	    }
	};
	ParallelAnimationCommand _doubleArrow = (ParallelAnimationCommand) ObjectExtensions
		.operator_doubleArrow(_parallelAnimationCommand, _function_7);
	_commandStack.execute(_doubleArrow);

	
	ImmutableList<XShape> removedShapes = diagram2shape.get(root.getRootDiagram());
	StreamSupport.stream(Iterables.filter(removedShapes, AssociationConnection.class).spliterator(), false)
		.map(AssociationConnection::getAssociationDescriptor)
		.filter(java.util.Objects::nonNull)
		.distinct()
		.peek(ad -> log.debug("collected association descriptor: {}", ad))
		.forEach(AssociationDescriptor::detachFromParent);
	StreamSupport.stream(Iterables.filter(removedShapes, PersistenceUnitNode.class).spliterator(), false)
		.map(PersistenceUnitNode::getPersistenceUnitDescriptor)
		.filter(java.util.Objects::nonNull)
		.distinct()
		.peek(pud -> log.debug("collected persistence unid descriptor: {}", pud))
		.forEach(PersistenceUnitDescriptor::detachFromParent);
    }
}
