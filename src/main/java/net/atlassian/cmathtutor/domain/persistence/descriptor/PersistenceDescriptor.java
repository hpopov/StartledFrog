package net.atlassian.cmathtutor.domain.persistence.descriptor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.graph.GraphBuilder;
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
	if (persistence.getPersistenceUnits().contains(persistenceUnit)) {
	    throw new IllegalOperationException("Equal persistence unit already exists");
	}
	return addPersistenceUnitInner(persistenceUnit);
    }

    private PersistenceUnitDescriptor addPersistenceUnitInner(PersistenceUnitModel persistenceUnit) {
	PersistenceUnitDescriptor persistenceUnitDescriptor = PersistenceUnitDescriptor.wrap(persistenceUnit, this);
	persistence.getPersistenceUnits().add(persistenceUnit);
	idToPersistenceUnitDescriptors.put(persistenceUnitDescriptor.getId(), persistenceUnitDescriptor);
	return persistenceUnitDescriptor;
    }

    /*
     * You need to remove units from inside of their descriptors? Or at least
     * together with their descriptors so that respective diagrams would also be
     * deleted
     */
//    public void removePersistenceUnit(@NonNull PersistenceUnitModel persistenceUnit) {
//	boolean isPresent = persistence.getPersistenceUnits().contains(persistenceUnit);
//	if (false == isPresent) {
//	    log.warn("Persistence unit {} was not present at persistence descriptor", persistenceUnit);
//	    return;
//	}
//	persistenceUnit.getReferentialAttributes().stream().map(ReferentialAttributeModel::getAssociation)
//		.forEach(this::removeAssociation);
//	persistence.getPersistenceUnits().remove(persistenceUnit);
//    }
//
//    public void removeAssociation(@NonNull AssociationModel association) {
//	if (false == persistence.getAssociations().contains(association)) {
//	    log.error("Association {} is not present at persistence descriptor", association);
//	    return;
//	}
//	removeReferentialAttributeFromItsParentClassifier(association.getContainerAttribute());
//	removeReferentialAttributeFromItsParentClassifier(association.getElementAttribute());
//	persistence.getAssociations().remove(association);
//    }
//
//    private void removeReferentialAttributeFromItsParentClassifier(@NonNull ReferentialAttributeModel attribute) {
//	attribute.getParentClassifier().getReferentialAttributes().remove(attribute);
//    }

//    public PersistenceUnitDescriptor addNewPersistenceUnit(@NonNull PersistenceUnitModel persistenceUnit)
//	    throws IllegalOperationException {
//	if (persistenceUnit.getName() == null) {
//	    throw new IllegalArgumentException("Persistence unit to add must have non null name");
//	}
//	if (persistence.getPersistenceUnits().contains(persistenceUnit)) {
//	    throw new IllegalOperationException("Equal persistence unit already exists");
//	}
//	if (false == persistenceUnit.getReferentialAttributes().isEmpty()) {
//	    throw new IllegalArgumentException("Persistence unit to add must not have any referential attributes");
//	}
//	validatePrimitiveAttributes(persistenceUnit.getPrimitiveAttributes());
//	validateRepositoryOperations(persistenceUnit.getRepositoryOperations());
//	PersistenceUnitDescriptor persistenceUnitDescriptor = PersistenceUnitDescriptor.wrap(persistenceUnit, this);
//	persistence.getPersistenceUnits().add(persistenceUnit);
//	return persistenceUnitDescriptor;
//    }

//    private void validatePrimitiveAttributes(ObservableSet<PrimitiveAttributeModel> primitiveAttributes) {
//	
//    }
//
//    private void validateRepositoryOperations(ObservableSet<RepositoryOperationModel> repositoryOperations) {
//	// TODO Auto-generated method stub
//	
//    }

    public AssociationDescriptor addNewAssociation(@NonNull AssociationModel association)
	    throws IllegalOperationException {
	if (persistence.getAssociations().contains(association)) {
	    throw new IllegalOperationException("Equal association already exists");
	}
	return addAssociationInner(association);
    }

    private AssociationDescriptor addAssociationInner(AssociationModel association) throws IllegalOperationException {
	AssociationDescriptor associationDescriptor = AssociationDescriptor.wrap(association, this);
	validateAssociationReferentialAttributes(association.getContainerAttribute(),
		association.getElementAttribute());
	if (association.getAggregationKind().equals(AggregationKind.SHARED)
		|| association.getAggregationKind().equals(AggregationKind.COMPOSITE)) {
	    addEdgeToPersistenceUnitGraph(association.getContainerAttribute(), association.getElementAttribute());
	}
	persistence.getAssociations().add(association);
	idToAssociationDescriptors.put(associationDescriptor.getId(), associationDescriptor);
	return associationDescriptor;
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
//	Graphs.hasCycle( ) or Graphs.reachableNodes(graph, node) TODO!!
	if (false == persistenceUnitGraph.nodes().containsAll(Arrays.asList(firstClassifier, lastClassifier))) {
	    return true;
	}
	return assertNoNodesAreLinkedTo(Collections.singleton(firstClassifier), new HashSet<>(), lastClassifier);
    }

    private boolean assertNoNodesAreLinkedTo(Set<PersistenceUnitModel> nodes, Set<PersistenceUnitModel> processedNodes,
	    PersistenceUnitModel lastNode) {
	for (PersistenceUnitModel node : nodes) {
	    if (persistenceUnitGraph.hasEdgeConnecting(node, lastNode)) {
		return false;
	    }
	}
	processedNodes.addAll(nodes);
	return assertNoNodesAreLinkedTo(nodes.stream().flatMap(
		node -> persistenceUnitGraph.successors(node).stream().filter(sNode -> !processedNodes.contains(sNode)))
		.collect(Collectors.toSet()), processedNodes, lastNode);
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
