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
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.domain.persistence.AggregationKind;
import net.atlassian.cmathtutor.domain.persistence.Association;
import net.atlassian.cmathtutor.domain.persistence.Persistence;
import net.atlassian.cmathtutor.domain.persistence.PersistenceUnit;
import net.atlassian.cmathtutor.domain.persistence.model.AssociationModel;
import net.atlassian.cmathtutor.domain.persistence.model.PersistenceModel;
import net.atlassian.cmathtutor.domain.persistence.model.PersistenceUnitModel;
import net.atlassian.cmathtutor.domain.persistence.model.ReferentialAttributeModel;
import net.atlassian.cmathtutor.util.UidUtil;

@Slf4j
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
            AssociationDescriptor associationDescriptor = AssociationDescriptor.wrap(association,
                    persistenceDescriptor);
            persistenceDescriptor.addExistedAssociationDescriptor(associationDescriptor, association);
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
        assertPersistenceUnitNameIsNotNull(persistenceUnit.getName());
        validatePersistenceUnitIsNotAddedToPersistence(persistenceUnit);
        PersistenceUnitDescriptor persistenceUnitDescriptor = addPersistenceUnitInner(persistenceUnit);
        persistenceUnit.setPersistence(persistence);
        persistence.getPersistenceUnits().add(persistenceUnit);
        return persistenceUnitDescriptor;
    }

    private void assertPersistenceUnitNameIsNotNull(@NonNull String persistenceUnitName) {
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
        addPersistenceUnitDescriptor(persistenceUnitDescriptor, persistenceUnit);
        return persistenceUnitDescriptor;
    }

    private void addPersistenceUnitDescriptor(
            PersistenceUnitDescriptor persistenceUnitDescriptor,
            PersistenceUnitModel persistenceUnit
    ) throws IllegalOperationException {
        validatePersistenceUnitReferentialAttributes(persistenceUnit.getReferentialAttributes());
        idToPersistenceUnitDescriptors.put(persistenceUnitDescriptor.getId(), persistenceUnitDescriptor);
    }

    private void validatePersistenceUnitReferentialAttributes(Set<ReferentialAttributeModel> referentialAttributes)
            throws IllegalOperationException {
        log.debug("Validating persistence unit referential attributes: {}", referentialAttributes);
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

        AssociationDescriptor associationDescriptor = AssociationDescriptor.wrap(association, this);
        addNewAssociationDescriptor(associationDescriptor, association);

        association.setPersistence(persistence);
        persistence.getAssociations().add(association);
        return associationDescriptor;
    }

    private void validateAssociationIsNotAddedToPersistence(AssociationModel association)
            throws IllegalOperationException {
        if (persistence.getAssociations().contains(association)) {
            throw new IllegalOperationException("Equal association already exists");
        }
    }

    private void addNewAssociationDescriptor(AssociationDescriptor associationDescriptor, AssociationModel association)
            throws IllegalOperationException {
        ReferentialAttributeModel containerAttribute = association.getContainerAttribute();
        ReferentialAttributeModel elementAttribute = association.getElementAttribute();
        validateAssociationReferentialAttributes(containerAttribute, elementAttribute);
        PersistenceUnitDescriptor containerUnitDescriptor = idToPersistenceUnitDescriptors
                .get(containerAttribute.getParentClassifier().getId());
        PersistenceUnitDescriptor elementUnitDescriptor = idToPersistenceUnitDescriptors
                .get(elementAttribute.getParentClassifier().getId());

        containerUnitDescriptor.addReferentialAttribute(containerAttribute);

        try {
            elementUnitDescriptor.addReferentialAttribute(elementAttribute);
        } catch (IllegalOperationException e) {
            log.info("Failed to add association element attribute, but container attribute has already been added."
                    + " Reverting the change back...");
            containerUnitDescriptor.removeReferentialAttribute(containerAttribute);
            throw e;
        }

        if (association.getAggregationKind().equals(AggregationKind.SHARED)
                || association.getAggregationKind().equals(AggregationKind.COMPOSITE)) {
            try {
                addEdgeToPersistenceUnitGraph(containerAttribute, elementAttribute);
            } catch (IllegalOperationException e) {
                log.info("Failed to add association to the graph,"
                        + " but both attributes have already been added to descriptor. Reverting the change back...");
                containerUnitDescriptor.removeReferentialAttribute(containerAttribute);
                elementUnitDescriptor.removeReferentialAttribute(elementAttribute);
                throw e;
            }
        }
        idToAssociationDescriptors.put(associationDescriptor.getId(), associationDescriptor);
    }

    private void addExistedAssociationDescriptor(
            AssociationDescriptor associationDescriptor,
            AssociationModel association
    )
            throws IllegalOperationException {
        ReferentialAttributeModel containerAttribute = association.getContainerAttribute();
        ReferentialAttributeModel elementAttribute = association.getElementAttribute();
        validateAssociationReferentialAttributes(containerAttribute, elementAttribute);
        if (association.getAggregationKind().equals(AggregationKind.SHARED)
                || association.getAggregationKind().equals(AggregationKind.COMPOSITE)) {
            addEdgeToPersistenceUnitGraph(containerAttribute, elementAttribute);
        }
        idToAssociationDescriptors.put(associationDescriptor.getId(), associationDescriptor);
    }

    private void validateAssociationReferentialAttributes(
            @NonNull ReferentialAttributeModel containerAttribute,
            @NonNull ReferentialAttributeModel elementAttribute
    ) throws IllegalOperationException {
        assertAttributeParentClassifiersAreNotNull(containerAttribute.getParentClassifier(),
                elementAttribute.getParentClassifier());
        if (false == persistence.getPersistenceUnits().contains(containerAttribute.getParentClassifier())) {
            // log.debug("Container parent classifier was not found at
            // registered persistence unit using contains(..)");
            // if (false == persistence.getPersistenceUnits().stream()
            // .anyMatch(containerAttribute.getParentClassifier()::equals)) {
            throw new IllegalOperationException("Container attribute persistence unit of the associtation to add"
                    + " must already be registered within persistence descriptor");
            // }
        }
        if (false == persistence.getPersistenceUnits().contains(elementAttribute.getParentClassifier())) {
            // log.debug("Element parent classifier was not found at registered
            // persistence unit using contains(..)");
            // if (false == persistence.getPersistenceUnits().stream()
            // .anyMatch(elementAttribute.getParentClassifier()::equals)) {
            throw new IllegalOperationException("Element attribute persistence unit of the associtation to add"
                    + " must already be registered within persistence descriptor");
            // }
        }
    }

    private void assertAttributeParentClassifiersAreNotNull(
            @NonNull PersistenceUnitModel containerParentClassifier,
            @NonNull PersistenceUnitModel elementParentClassifier
    ) {
    }

    private void addEdgeToPersistenceUnitGraph(
            ReferentialAttributeModel containerAttribute,
            ReferentialAttributeModel elementAttribute
    ) throws IllegalOperationException {
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

    private boolean assertNodesDontHaveCommonPath(
            PersistenceUnitModel firstClassifier,
            PersistenceUnitModel lastClassifier
    ) {
        if (false == persistenceUnitGraph.nodes().containsAll(Arrays.asList(firstClassifier, lastClassifier))) {
            return true;
        }
        if (Graphs.reachableNodes(persistenceUnitGraph, lastClassifier).contains(firstClassifier)) {
            return false;
        }
        return true;
    }

    // private boolean assertNoNodesAreLinkedTo(Set<PersistenceUnitModel> nodes,
    // Set<PersistenceUnitModel> processedNodes,
    // PersistenceUnitModel lastNode) {
    // for (PersistenceUnitModel node : nodes) {
    // if (persistenceUnitGraph.hasEdgeConnecting(node, lastNode)) {
    // return false;
    // }
    // }
    // processedNodes.addAll(nodes);
    // return assertNoNodesAreLinkedTo(nodes.stream().flatMap(
    // node -> persistenceUnitGraph.successors(node).stream().filter(sNode ->
    // !processedNodes.contains(sNode)))
    // .collect(Collectors.toSet()), processedNodes, lastNode);
    // }

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
        addExistedAssociationDescriptor(associationDescriptor, association);

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
        // assuming that descritor is right after detachment, i.e. it has a
        // reference to
        // underlying persistence
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
