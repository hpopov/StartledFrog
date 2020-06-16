package net.atlassian.cmathtutor.fxdiagram;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.fxdiagram.core.XDiagram;
import de.fxdiagram.core.XShape;
import de.fxdiagram.core.command.AddRemoveCommand;
import de.fxdiagram.core.command.AnimationCommand;
import de.fxdiagram.core.command.CommandContext;
import javafx.animation.ParallelTransition;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.domain.persistence.descriptor.AssociationDescriptor;
import net.atlassian.cmathtutor.domain.persistence.descriptor.IllegalOperationException;
import net.atlassian.cmathtutor.domain.persistence.descriptor.PersistenceUnitDescriptor;

@Slf4j
public class StartledFrogAddRemoveCommand extends AddRemoveCommand {

    private static final String UNABLE_TO_ATTACH__ASSOCIATION_MESAGE_FORMAT = "Unable to execute attach command for association %s->%s: %s";
    private static final String UNABLE_TO_ATTACH_UNIT_MESSAGE_FORMAT = "Unable to execute attach command for persistence unit %s: %s";
    private List<PersistenceUnitDescriptor> persistenceUnitDescriptors;
    private List<AssociationDescriptor> associationDescriptors;

    public static StartledFrogAddRemoveCommand newStartledFrogAddCommand(StartledFrogDiagram diagram,
	    XShape[] shapes) {
	return new StartledFrogAddRemoveCommand(true, diagram, shapes);
    }

    public static AnimationCommand newStartledFrogAddCommand(StartledFrogDiagram diagram,
	    PersistenceUnitNode persistenceUnitNode) {
	return newStartledFrogAddCommand(diagram, new XShape[] { persistenceUnitNode });
    }

    public static StartledFrogAddRemoveCommand newStartledFrogRemoveCommand(StartledFrogDiagram diagram,
	    XShape[] shapes) {
	return new StartledFrogAddRemoveCommand(false, diagram, shapes);
    }

    public static AnimationCommand newStartledFrogAddCommand(StartledFrogDiagram diagram,
	    AssociationConnection associationConnection) {
	return newStartledFrogAddCommand(diagram, new XShape[] { associationConnection });
    }

    protected StartledFrogAddRemoveCommand(boolean isAdd, XDiagram diagram, XShape[] shapes) {
	super(isAdd, diagram, shapes);
	log.debug("Creating new instance, isAdd={}", isAdd);
	persistenceUnitDescriptors = Stream.of(shapes)
		.filter(PersistenceUnitNode.class::isInstance)
		.map(PersistenceUnitNode.class::cast)
		.map(PersistenceUnitNode::getPersistenceUnitDescriptor)
		.distinct()
		.collect(Collectors.toList());
	associationDescriptors = Stream.of(shapes)
		.filter(AssociationConnection.class::isInstance)
		.map(AssociationConnection.class::cast)
		.map(AssociationConnection::getAssociationDescriptor)
		.distinct()
		.collect(Collectors.toList());
    }

    @Override
    protected ParallelTransition add(CommandContext context) throws IllegalStateException {
	log.debug("adding...");
	for (PersistenceUnitDescriptor persistenceUnitDescriptor : persistenceUnitDescriptors) {
	    try {
		log.debug("Attaching persistence unit descriptor {} to parent", persistenceUnitDescriptor);
		persistenceUnitDescriptor.attachToParent();
	    } catch (IllegalOperationException e) {
		throw new IllegalStateException(String.format(UNABLE_TO_ATTACH_UNIT_MESSAGE_FORMAT,
			persistenceUnitDescriptor.getName(), e.getMessage()));
	    }
	}
	for (AssociationDescriptor associationDescriptor : associationDescriptors) {
	    try {
		associationDescriptor.attachToParent();
	    } catch (IllegalOperationException e) {
		throw new IllegalStateException(String.format(UNABLE_TO_ATTACH__ASSOCIATION_MESAGE_FORMAT,
			associationDescriptor.getElementAttribute().getName(),
			associationDescriptor.getContainerAttribute().getName(), e.getMessage()));
	    }
	}
	return super.add(context);
    }

    @Override
    protected ParallelTransition remove(CommandContext context) {
	associationDescriptors.forEach(AssociationDescriptor::detachFromParent);
	persistenceUnitDescriptors.forEach(PersistenceUnitDescriptor::detachFromParent);
	return super.remove(context);
    }
}
