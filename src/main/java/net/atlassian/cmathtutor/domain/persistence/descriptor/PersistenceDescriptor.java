package net.atlassian.cmathtutor.domain.persistence.descriptor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;

import javafx.collections.ObservableSet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import net.atlassian.cmathtutor.domain.persistence.AggregationKind;
import net.atlassian.cmathtutor.domain.persistence.Association;
import net.atlassian.cmathtutor.domain.persistence.Persistence;
import net.atlassian.cmathtutor.domain.persistence.PersistenceUnit;
import net.atlassian.cmathtutor.domain.persistence.model.AssociationModel;
import net.atlassian.cmathtutor.domain.persistence.model.PersistenceModel;
import net.atlassian.cmathtutor.domain.persistence.model.PersistenceUnitModel;
import net.atlassian.cmathtutor.domain.persistence.model.ReferentialAttributeModel;
import net.atlassian.cmathtutor.util.UidUtil;

@ToString
@NoArgsConstructor
public class PersistenceDescriptor extends AbstractDescriptor implements Persistence {

    @Getter
    private PersistenceModel persistence;

    private Map<String, PersistenceUnitDescriptor> idToPersistenceUnitDescriptors = new HashMap<>();
    private Map<String, AssociationDescriptor> idToAssociationDescriptors = new HashMap<>();

    private MutableGraph<PersistenceUnitModel> persistenceUnitGraph = GraphBuilder.directed()
	    .allowsSelfLoops(false)
	    .build();

    private PersistenceDescriptor(String id, PersistenceModel persistence) {
	super(id);
	this.persistence = persistence;
    }

    public static PersistenceDescriptor newInstance() {
	String id = UidUtil.getUId();
	return new PersistenceDescriptor(id, new PersistenceModel(id));
    }

    public static PersistenceDescriptor wrap(@NonNull PersistenceModel persistence) throws IllegalOperationException {
	if (persistence.getId() == null) {
	    throw new IllegalArgumentException("Persistence to wrap must have non null id");
	}
	PersistenceDescriptor persistenceDescriptor = new PersistenceDescriptor(persistence.getId(), persistence);
	for (PersistenceUnitModel persistenceUnit : persistence.getPersistenceUnits()) {
	    persistenceDescriptor.addPersistenceUnitInner(persistenceUnit);
	}
	for (AssociationModel association : persistence.getAssociations()) {
	    persistenceDescriptor.addAssociationInner(association);
	}
	return persistenceDescriptor;
    }

    public PersistenceUnitDescriptor addNewPersistenceUnit(@NonNull String name)
	    throws IllegalOperationException {
	PersistenceUnitModel persistenceUnit = new PersistenceUnitModel(UidUtil.getUId());
	persistenceUnit.setName(name);
	validatePersistenceUnitIsNotAddedToPersistence(persistenceUnit);
	PersistenceUnitDescriptor persistenceUnitDescriptor = addPersistenceUnitInner(persistenceUnit);
	persistence.getPersistenceUnits().add(persistenceUnit);
	return persistenceUnitDescriptor;
    }

    public PersistenceUnitDescriptor addNewPersistenceUnit(@NonNull PersistenceUnitModel persistenceUnit)
	    throws IllegalOperationException {
	validatePersistenceUnitIsNotAddedToPersistence(persistenceUnit);
	PersistenceUnitDescriptor persistenceUnitDescriptor = addPersistenceUnitInner(persistenceUnit);
	persistence.getPersistenceUnits().add(persistenceUnit);
	return persistenceUnitDescriptor;
    }

    private void validatePersistenceUnitIsNotAddedToPersistence(PersistenceUnitModel persistenceUnit)
	    throws IllegalOperationException {
	if (persistence.getPersistenceUnits().contains(persistenceUnit)) {
	    throw new IllegalOperationException("Equal persistence unit already exists");
	}
    }

    private PersistenceUnitDescriptor addPersistenceUnitInner(PersistenceUnitModel persistenceUnit)
	    throws IllegalOperationException {
	PersistenceUnitDescriptor persistenceUnitDescriptor = PersistenceUnitDescriptor.wrap(persistenceUnit, this);
	addPersistenceUnitDescriptor(persistenceUnitDescriptor, persistenceUnitDescriptor.getWrappedPersistenceUnit());
	return persistenceUnitDescriptor;
    }

    private void addPersistenceUnitDescriptor(PersistenceUnitDescriptor persistenceUnitDescriptor,
	    PersistenceUnitModel persistenceUnit) throws IllegalOperationException {
	validatePersistenceUnitReferentialAttributes(persistenceUnit.getReferentialAttributes());
	idToPersistenceUnitDescriptors.put(persistenceUnitDescriptor.getId(), persistenceUnitDescriptor);
    }

    private void validatePersistenceUnitReferentialAttributes(Set<ReferentialAttributeModel> referentialAttributes)
	    throws IllegalOperationException {
	for (ReferentialAttributeModel referentialAttribute : referentialAttributes) {
	    if (false == persistence.getAssociations().contains(referentialAttribute.getAssociation())) {
		throw new IllegalOperationException(
			"Association for referential attribute " + referentialAttribute.getName() + " does not exist");
	    }
	}
    }

    public AssociationDescriptor addNewAssociation(@NonNull AssociationModel association)
	    throws IllegalOperationException {
	validateAssociationIsNotAddedToPersistence(association);
	AssociationDescriptor associationDescriptor = addAssociationInner(association);
	persistence.getAssociations().add(association);
	return associationDescriptor;
    }

    private void validateAssociationIsNotAddedToPersistence(AssociationModel association)
	    throws IllegalOperationException {
	if (persistence.getAssociations().contains(association)) {
	    throw new IllegalOperationException("Equal association already exists");
	}
    }

    private AssociationDescriptor addAssociationInner(AssociationModel association) throws IllegalOperationException {
	AssociationDescriptor associationDescriptor = AssociationDescriptor.wrap(association, this);
	addAssociationDescriptor(associationDescriptor, association);
	return associationDescriptor;
    }

    private void addAssociationDescriptor(AssociationDescriptor associationDescriptor, AssociationModel association)
	    throws IllegalOperationException {
	validateAssociationReferentialAttributes(association.getContainerAttribute(),
		association.getElementAttribute());
	if (association.getAggregationKind().equals(AggregationKind.SHARED)
		|| association.getAggregationKind().equals(AggregationKind.COMPOSITE)) {
	    addEdgeToPersistenceUnitGraph(association.getContainerAttribute(), association.getElementAttribute());
	}
	idToAssociationDescriptors.put(associationDescriptor.getId(), associationDescriptor);
    }

    private void validateAssociationReferentialAttributes(ReferentialAttributeModel containerAttribute,
	    ReferentialAttributeModel elementAttribute) throws IllegalOperationException {
	if (false == persistence.getPersistenceUnits().contains(containerAttribute.getParentClassifier())) {
	    throw new IllegalOperationException("Container attribute of the associtation to add must already be "
		    + "registered within persistence descriptor");
	}
	if (false == persistence.getPersistenceUnits().contains(elementAttribute.getParentClassifier())) {
	    throw new IllegalOperationException("Element attribute of the associtation to add must already be "
		    + "registered within persistence descriptor");
	}
    }

    private void addEdgeToPersistenceUnitGraph(ReferentialAttributeModel containerAttribute,
	    ReferentialAttributeModel elementAttribute) throws IllegalOperationException {
	PersistenceUnitModel containerClassifier = containerAttribute.getParentClassifier();
	PersistenceUnitModel elementClassifier = elementAttribute.getParentClassifier();
	if (containerClassifier.equals(elementClassifier)) {
	    throw new IllegalOperationException("Container classifier must not be equal to element classifier");
	}
	if (persistenceUnitGraph.nodes().contains(elementClassifier)
		&& persistenceUnitGraph.inDegree(elementClassifier) > 0) {
	    throw new IllegalOperationException("Element can only have one parent container");
	}
	if (false == assertNodesDontHaveCommonPath(elementClassifier, containerClassifier)) {
	    throw new IllegalOperationException(
		    "There must be no path existed between element and container classifiers");
	}
	boolean isNewEdge = persistenceUnitGraph.putEdge(containerClassifier,
		elementClassifier);
	if (false == isNewEdge) {
	    throw new IllegalStateException("The edge added must be a new one");
	}
    }

    private boolean assertNodesDontHaveCommonPath(PersistenceUnitModel firstClassifier,
	    PersistenceUnitModel lastClassifier) {
	if (false == persistenceUnitGraph.nodes().containsAll(Arrays.asList(firstClassifier, lastClassifier))) {
	    return true;
	}
	if (Graphs.reachableNodes(persistenceUnitGraph, lastClassifier).contains(firstClassifier)) {
	    return false;
	}
	return true;
    }

//    private boolean assertNoNodesAreLinkedTo(Set<PersistenceUnitModel> nodes, Set<PersistenceUnitModel> processedNodes,
//	    PersistenceUnitModel lastNode) {
//	for (PersistenceUnitModel node : nodes) {
//	    if (persistenceUnitGraph.hasEdgeConnecting(node, lastNode)) {
//		return false;
//	    }
//	}
//	processedNodes.addAll(nodes);
//	return assertNoNodesAreLinkedTo(nodes.stream().flatMap(
//		node -> persistenceUnitGraph.successors(node).stream().filter(sNode -> !processedNodes.contains(sNode)))
//		.collect(Collectors.toSet()), processedNodes, lastNode);
//    }

    public void detachAssociation(@NonNull AssociationDescriptor associationDescriptor) {
	idToAssociationDescriptors.remove(associationDescriptor.getId());
	AssociationModel wrappedAssociation = associationDescriptor.getWrappedAssociation();

	persistenceUnitGraph.removeEdge(wrappedAssociation.getContainerAttribute().getParentClassifier(),
		wrappedAssociation.getElementAttribute().getParentClassifier());
	persistence.getAssociations().remove(wrappedAssociation);
    }

    public void attachAssociation(AssociationDescriptor associationDescriptor) throws IllegalOperationException {
	AssociationModel association = associationDescriptor.getWrappedAssociation();
	validateAssociationIsNotAddedToPersistence(association);
	addAssociationDescriptor(associationDescriptor, association);

	persistence.getAssociations().add(association);
    }

    public void detachPersistenceUnit(PersistenceUnitDescriptor persistenceUnitDescriptor) {
	idToPersistenceUnitDescriptors.remove(persistenceUnitDescriptor.getId());
	PersistenceUnitModel wrappedPersistenceUnit = persistenceUnitDescriptor.getWrappedPersistenceUnit();

	persistenceUnitGraph.removeNode(wrappedPersistenceUnit);
	persistence.getPersistenceUnits().remove(wrappedPersistenceUnit);
    }

    public void attachPersistenceUnit(PersistenceUnitDescriptor persistenceUnitDescriptor)
	    throws IllegalOperationException {
	PersistenceUnitModel wrappedPersistenceUnit = persistenceUnitDescriptor.getWrappedPersistenceUnit();
	validatePersistenceUnitIsNotAddedToPersistence(wrappedPersistenceUnit);
	addPersistenceUnitDescriptor(persistenceUnitDescriptor, wrappedPersistenceUnit);

	persistence.getPersistenceUnits().add(wrappedPersistenceUnit);
    }

    public PersistenceUnitDescriptor getPersistenceUnitDescriptorById(String id) {
	return idToPersistenceUnitDescriptors.get(id);
    }

    public AssociationDescriptor getAssociationDescriptorById(String id) {
	return idToAssociationDescriptors.get(id);
    }

    @Override
    public ObservableSet<? extends PersistenceUnit> getUnmodifiablePersistenceUnits() {
	return persistence.getUnmodifiablePersistenceUnits();
    }

    @Override
    public ObservableSet<? extends Association> getUnmodifiableAssociations() {
	return persistence.getUnmodifiableAssociations();
    }

    public Collection<PersistenceUnitDescriptor> getPersistenceUnitDescriptors() {
	return idToPersistenceUnitDescriptors.values();
    }

    public Collection<AssociationDescriptor> getAssociationDescriptors() {
	return idToAssociationDescriptors.values();
    }
}
