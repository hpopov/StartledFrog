package net.atlassian.cmathtutor.domain.persistence.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener.Change;
import lombok.ToString;
import net.atlassian.cmathtutor.domain.persistence.Association;
import net.atlassian.cmathtutor.domain.persistence.Persistence;
import net.atlassian.cmathtutor.domain.persistence.PersistenceUnit;

@XmlRootElement
@XmlType(propOrder = { "persistenceUnits", "associations" })
@XmlAccessorType(XmlAccessType.NONE)
@ToString(callSuper = true)
public class PersistenceModel extends AbstractIdentifyableModel implements Persistence {

    private ObservableSet<PersistenceUnitModel> persistenceUnits = FXCollections.observableSet();
    private ObservableSet<AssociationModel> associations = FXCollections.observableSet();

    private ObservableSet<? extends PersistenceUnit> unmodifiablePersistenceUnits = FXCollections
            .unmodifiableObservableSet(persistenceUnits);
    private ObservableSet<? extends Association> unmodifiableAssociations = FXCollections
            .unmodifiableObservableSet(associations);

    public PersistenceModel() {
        super();
        // initBindings();
    }

    public PersistenceModel(String id) {
        super(id);
        // initBindings();
    }

    @SuppressWarnings("unused")
    private void initBindings() {
        persistenceUnits.addListener((Change<? extends PersistenceUnitModel> change) -> {
            if (change.wasRemoved()) {
                change.getElementRemoved().setPersistence(null);
            }
            if (change.wasAdded()) {
                change.getElementAdded().setPersistence(this);
            }
        });
        associations.addListener((Change<? extends AssociationModel> change) -> {
            if (change.wasRemoved()) {
                change.getElementRemoved().setPersistence(null);
            }
            if (change.wasAdded()) {
                change.getElementAdded().setPersistence(this);
            }
        });
    }

    @XmlElementWrapper(name = "persistence-units", required = true)
    @XmlElement(name = "persistence-unit", required = false)
    public ObservableSet<PersistenceUnitModel> getPersistenceUnits() {
        return persistenceUnits;
    }

    @XmlElementWrapper(name = "associations", required = true)
    @XmlElement(name = "association", required = false)
    public ObservableSet<AssociationModel> getAssociations() {
        return associations;
    }

    @Override
    public ObservableSet<? extends PersistenceUnit> getUnmodifiablePersistenceUnits() {
        return unmodifiablePersistenceUnits;
    }

    @Override
    public ObservableSet<? extends Association> getUnmodifiableAssociations() {
        return unmodifiableAssociations;
    }
}
