package net.atlassian.cmathtutor.domain.persistence.descriptor;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ObservableSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.domain.persistence.Association;
import net.atlassian.cmathtutor.domain.persistence.PersistenceUnit;
import net.atlassian.cmathtutor.domain.persistence.PrimitiveAttribute;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;
import net.atlassian.cmathtutor.domain.persistence.RepositoryOperation;
import net.atlassian.cmathtutor.domain.persistence.model.PersistenceUnitModel;
import net.atlassian.cmathtutor.domain.persistence.model.PrimitiveAttributeModel;
import net.atlassian.cmathtutor.domain.persistence.model.ReferentialAttributeModel;
import net.atlassian.cmathtutor.domain.persistence.model.RepositoryOperationModel;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PersistenceUnitDescriptor extends AbstractDescriptor implements PersistenceUnit {

    private PersistenceUnitModel persistenceUnit;
    private PersistenceDescriptor parentDescriptor;

    private boolean detached = false;
    private List<AssociationDescriptor> detachedAssociations;

    private PersistenceUnitDescriptor(
            PersistenceUnitModel persistenceUnit,
            PersistenceDescriptor parentDescriptor
    ) {
        super(persistenceUnit.getId());
        this.persistenceUnit = persistenceUnit;
        this.parentDescriptor = parentDescriptor;
    }

    public static PersistenceUnitDescriptor wrap(
            @NonNull PersistenceUnitModel persistenceUnit,
            @NonNull PersistenceDescriptor parentDescriptor
    ) {
        if (persistenceUnit.getId() == null) {
            throw new IllegalArgumentException("Id of the persistence unit to wrap must not be null");
        }
        return new PersistenceUnitDescriptor(persistenceUnit, parentDescriptor);
    }

    public void addNewPrimitiveAttribute(@NonNull PrimitiveAttributeModel primitiveAttribute)
            throws IllegalOperationException {
        assertAttributeNameIsNotBlank(primitiveAttribute.getName());
        assertPersistenceUnitNotContainAttributeWithName(primitiveAttribute.getName());
        persistenceUnit.getPrimitiveAttributes().add(primitiveAttribute);
    }

    public void addReferentialAttribute(@NonNull ReferentialAttributeModel referentialAttribute)
            throws IllegalOperationException {
        log.debug(
                "Id: {} | Trying to add referential attribute (id: {}, name: {}, parent: {}) to existing attriburtes",
                getId(), referentialAttribute.getId(), referentialAttribute.getName(),
                referentialAttribute.getParentClassifier());
        log.debug("Existed attributes are: {}, their parents are: {}", persistenceUnit.getReferentialAttributes(),
                persistenceUnit.getReferentialAttributes().stream().map(ReferentialAttributeModel::getParentClassifier)
                        .collect(Collectors.toList()));
        assertAttributeNameIsNotBlank(referentialAttribute.getName());
        assertPersistenceUnitNotContainAttributeWithName(referentialAttribute.getName());
        referentialAttribute.setParentClassifier(persistenceUnit);
        log.debug("Referential attribute was added? {}",
                persistenceUnit.getReferentialAttributes().add(referentialAttribute));
    }

    private void assertAttributeNameIsNotBlank(String name) throws IllegalOperationException {
        if (StringUtils.isBlank(name)) {
            throw new IllegalOperationException("Attribute name bust not be blank");
        }
    }

    public boolean removeReferentialAttribute(@NonNull ReferentialAttributeModel referentialAttribute) {
        log.debug("Going to remove attribute {} from attributes {}", referentialAttribute,
                persistenceUnit.getReferentialAttributes());
        if (false == persistenceUnit.getReferentialAttributes().remove(referentialAttribute)) {
            // log.debug("Tried to remove referential attribute using remove(..)
            // method UNSUCCESSFULLY");
            // Optional<ReferentialAttributeModel>
            // foundEqualReferentialAttribute = persistenceUnit
            // .getReferentialAttributes().stream().filter(referentialAttribute::equals).findAny();
            // if (foundEqualReferentialAttribute.isPresent()) {
            // log.debug("Found equal referential attribute {}",
            // foundEqualReferentialAttribute.get());
            // if (false ==
            // persistenceUnit.getReferentialAttributes().remove(foundEqualReferentialAttribute.get()))
            // {
            // log.debug("Tried to remove found equal attribute using remove(..)
            // UNSUCCESSFULLY");
            // Iterator<ReferentialAttributeModel> it =
            // persistenceUnit.getReferentialAttributes().iterator();
            // while (it.hasNext()) {
            // ReferentialAttributeModel next = it.next();
            // if (referentialAttribute.equals(next)) {
            // log.debug("Found equal attribute using iterator. Going to remove
            // it using iterator...");
            // it.remove();
            // if (false == persistenceUnit.getReferentialAttributes().stream()
            // .anyMatch(referentialAttribute::equals)) {
            // log.debug("UNABLE to remove attribute using iterator.remove()");
            // return false;
            // }
            // return true;
            // }
            // }
            // }
            // return true;
            // }
            log.debug("Equal referential attribute was not found.");
            return false;
        }
        return true;
    }

    private void assertPersistenceUnitNotContainAttributeWithName(@NonNull String attributeName)
            throws IllegalOperationException {
        String name = persistenceUnit.getName();
        log.debug("Id: {} | Asserting there are no existed attribute with name {}", getId(), attributeName);
        PrimitiveAttributeModel primitiveIdentifiableInstance = PrimitiveAttributeModel
                .makeIdentifiableInstance(attributeName, persistenceUnit);
        if (persistenceUnit.getPrimitiveAttributes().contains(primitiveIdentifiableInstance)
        /*
         * || persistenceUnit.getPrimitiveAttributes().stream().anyMatch(
         * primitiveIdentifiableInstance::equals)
         */) {
            throw new IllegalOperationException(
                    "Persistence unit " + name + " already contains primitive attribute with name " + attributeName);
        }
        ReferentialAttributeModel referentialIdentifiableAttribute = ReferentialAttributeModel
                .makeIdentifiableInstance(attributeName, persistenceUnit);
        log.debug("Id: {} | Referential identifiable attribute hash: {}", getId(),
                referentialIdentifiableAttribute.hashCode());

        ObservableSet<ReferentialAttributeModel> referentialAttributes = persistenceUnit.getReferentialAttributes();
        log.debug("Id: {} | PU referential attributes hashCodes: {}", getId(),
                referentialAttributes.stream().map(Object::hashCode).collect(Collectors.toList()));
        if (referentialAttributes.size() == 1) {
            ReferentialAttributeModel existedElement = referentialAttributes.iterator().next();
            boolean existedElementEqualsIdentifiable = existedElement.equals(referentialIdentifiableAttribute);
            boolean identifiableAttributeEqualsExistedElement = referentialIdentifiableAttribute.equals(existedElement);
            log.debug("Id: {} | existed referential attribute equal identifiable one? {}", getId(),
                    existedElementEqualsIdentifiable);
            log.debug("Id: {} | identifiable referential attribute equal existed one? {}", getId(),
                    identifiableAttributeEqualsExistedElement);
        }
        boolean contains = referentialAttributes
                .contains(referentialIdentifiableAttribute);
        log.debug("Id: {} | PU referential attributes contains identifiable one? {}", getId(), contains);

        if (contains
        // ||
        // referentialAttributes.stream().anyMatch(referentialIdentifiableAttribute::equals)
        ) {
            throw new IllegalOperationException(
                    "Persistence unit " + name + " already contains referential attribute with name " + attributeName);
        }
    }

    public void addNewRepositoryOperation(@NonNull RepositoryOperationModel repositoryOperation)
            throws IllegalOperationException {
        String name = repositoryOperation.getName();
        if (name == null) {
            throw new IllegalArgumentException("Name of the repository operation to add must not be null");
        }
        if (persistenceUnit.getRepositoryOperations()
                .contains(RepositoryOperationModel.makeIdentifiableInstance(name, persistenceUnit))) {
            throw new IllegalOperationException(
                    "Persistence unit already contains repository operation with the same name");
        }
        persistenceUnit.getRepositoryOperations().add(repositoryOperation);
    }

    public void detachFromParent() {
        log.debug("detachFromParent is called");
        if (true == detached) {
            log.warn("detachFromParent() was called, but descriptor {} is ALREADY detached", getId());
            return;
        }
        detached = true;
        detachedAssociations = getUnmodifiableReferentialAttributes().stream()
                .map(ReferentialAttribute::getAssociation)
                .map(Association::getId)
                .map(parentDescriptor::getAssociationDescriptorById)
                .distinct()
                .collect(Collectors.toList());
        detachedAssociations.forEach(AssociationDescriptor::detachFromParent);
        parentDescriptor.detachPersistenceUnit(this);
    }

    public void attachToParent() throws IllegalOperationException {
        log.debug("attachToParent is called");
        if (false == detached) {
            log.warn("attachToParent() was called, but descriptor {} is NOT detached", getId());
            return;
        }
        parentDescriptor.attachPersistenceUnit(this);
        for (AssociationDescriptor detachedAssociation : detachedAssociations) {
            detachedAssociation.attachToParent();
        }
        detached = false;
        log.debug("attachToParent finished successfully");
    }

    public PersistenceUnitModel getWrappedPersistenceUnit() {
        return persistenceUnit;
    }

    public ReadOnlyStringProperty nameProperty() {
        return persistenceUnit.nameProperty();
    }

    @Override
    public String getName() {
        return persistenceUnit.getName();
    }

    @Override
    public ObservableSet<? extends PrimitiveAttribute> getUnmodifiablePrimitiveAttributes() {
        return persistenceUnit.getUnmodifiablePrimitiveAttributes();
    }

    @Override
    public ObservableSet<? extends ReferentialAttribute> getUnmodifiableReferentialAttributes() {
        return persistenceUnit.getUnmodifiableReferentialAttributes();
    }

    @Override
    public ObservableSet<? extends RepositoryOperation> getUnmodifiableRepositoryOperations() {
        return persistenceUnit.getUnmodifiableRepositoryOperations();
    }

    @Override
    public PersistenceDescriptor getPersistence() {
        return parentDescriptor;
    }
}
