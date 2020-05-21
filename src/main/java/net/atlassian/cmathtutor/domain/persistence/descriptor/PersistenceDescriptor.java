package net.atlassian.cmathtutor.domain.persistence.descriptor;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import javafx.collections.ObservableSet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.atlassian.cmathtutor.domain.persistence.AggregationKind;
import net.atlassian.cmathtutor.domain.persistence.Association;
import net.atlassian.cmathtutor.domain.persistence.Persistence;
import net.atlassian.cmathtutor.domain.persistence.PersistenceUnit;
import net.atlassian.cmathtutor.domain.persistence.model.AssociationModel;
import net.atlassian.cmathtutor.domain.persistence.model.PersistenceModel;
import net.atlassian.cmathtutor.domain.persistence.model.PersistenceUnitModel;
import net.atlassian.cmathtutor.domain.persistence.model.ReferentialAttributeModel;
import net.atlassian.cmathtutor.util.UidUtil;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PersistenceDescriptor extends AbstractDescriptor implements Persistence {

    @Getter
    private PersistenceModel persistence;

    private MutableGraph<ReferentialAttributeModel> persistenceUnitGraph = GraphBuilder.directed()
	    .allowsSelfLoops(false)
	    .build();

    private PersistenceDescriptor(String id) {
	super(id);
	persistence = new PersistenceModel(id);
    }

    public static PersistenceDescriptor newInstance() {
	return new PersistenceDescriptor(UidUtil.getUId());
    }

    public PersistenceUnitDescriptor addNewPersistenceUnit(@NonNull String name)
	    throws IllegalOperationException {
	PersistenceUnitModel persistenceUnit = new PersistenceUnitModel(UidUtil.getUId());
	persistenceUnit.setName(name);
	if (persistence.getPersistenceUnits().contains(persistenceUnit)) {
	    throw new IllegalOperationException("Equal persistence unit already exists");
	}
	PersistenceUnitDescriptor persistenceUnitDescriptor = PersistenceUnitDescriptor.wrap(persistenceUnit, this);
	persistence.getPersistenceUnits().add(persistenceUnit);
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
	AssociationDescriptor associationDescriptor = AssociationDescriptor.wrap(association, this);
	validateAssociationReferentialAttributes(association.getContainerAttribute(),
		association.getElementAttribute());
	if (association.getAggregationKind().equals(AggregationKind.SHARED)
		|| association.getAggregationKind().equals(AggregationKind.COMPOSITE)) {
	    addEdgeToPersistenceUnitGraph(association.getContainerAttribute(), association.getElementAttribute());
	}
	persistence.getAssociations().add(association);

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
	if (containerAttribute.equals(elementAttribute)) {
	    throw new IllegalOperationException("Container must not be equal to element");
	}
	if (persistenceUnitGraph.nodes().contains(elementAttribute)
		&& persistenceUnitGraph.inDegree(elementAttribute) > 0) {
	    throw new IllegalOperationException("Element can only have one parent container");
	}
	boolean isNewEdge = persistenceUnitGraph.putEdge(containerAttribute, elementAttribute);
	if (false == isNewEdge) {
	    throw new IllegalStateException("The edge added must be a new one");
	}
    }

    @Override
    public ObservableSet<? extends PersistenceUnit> getUnmodifiablePersistenceUnits() {
	return persistence.getUnmodifiablePersistenceUnits();
    }

    @Override
    public ObservableSet<? extends Association> getUnmodifiableAssociations() {
	return persistence.getUnmodifiableAssociations();
    }

}
