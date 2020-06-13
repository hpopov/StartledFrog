package net.atlassian.cmathtutor.view.diagram;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.fxdiagram.lib.nodes.RectangleBorderPane;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import net.atlassian.cmathtutor.domain.persistence.OwnerType;
import net.atlassian.cmathtutor.domain.persistence.PrimitiveAttribute;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;
import net.atlassian.cmathtutor.domain.persistence.descriptor.PersistenceUnitDescriptor;

public class PersistenceUnitNodeView extends RectangleBorderPane {

    @FXML
    private Text nameText;

    @FXML
    private VBox primitiveAttributeEntriesVBox;

    @FXML
    private VBox referentialAttributeEntriesVBox;

    private Map<String, PrimitiveAttributeEntryView> idToPrimitiveAttributeEntries = new HashMap<>();
    private Map<String, ReferentialAttributeEntryView> idToReferentialAttributeEntries = new HashMap<>();

    private PersistenceUnitDescriptor persistenceUnitDescriptor;

    public PersistenceUnitNodeView() {
	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("persistenceUnitNode.fxml"));
	fxmlLoader.setRoot(this);
	fxmlLoader.setController(this);
	try {
	    fxmlLoader.load();
	} catch (IOException exception) {
	    throw new RuntimeException(exception);
	}
    }

    public StringProperty nameProperty() {
	return nameText.textProperty();
    }

    public String getName() {
	return nameProperty().get();
    }

    public void setName(String name) {
	nameProperty().set(name);
    }

    public PersistenceUnitDescriptor getPersistenceUnitDescriptor() {
	return persistenceUnitDescriptor;
    }

    public void setPersistenceUnitDescriptor(PersistenceUnitDescriptor persistenceUnitDescriptor) {
	this.persistenceUnitDescriptor = persistenceUnitDescriptor;
	initBindings(persistenceUnitDescriptor);
    }

    private void initBindings(PersistenceUnitDescriptor persistenceUnitDescriptor) {
	setName(null);
	clearPrimitiveAttributeEntries();
	clearReferentialAttributeEntries();
	if (persistenceUnitDescriptor != null) {
	    nameProperty().bind(persistenceUnitDescriptor.nameProperty());
	    bindPrimitiveAttributes(persistenceUnitDescriptor);
	    bindReferentialAttributes(persistenceUnitDescriptor);
	}
    }

    private void clearPrimitiveAttributeEntries() {
	idToPrimitiveAttributeEntries.clear();
	primitiveAttributeEntriesVBox.getChildren().clear();
    }

    private void bindPrimitiveAttributes(PersistenceUnitDescriptor persistenceUnitDescriptor) {
	ObservableSet<? extends PrimitiveAttribute> primitiveAttributes = persistenceUnitDescriptor
		.getUnmodifiablePrimitiveAttributes();
	for (PrimitiveAttribute attribute : primitiveAttributes) {
	    addPrimitiveAttributeToEntries(attribute);
	}
	primitiveAttributes.addListener(new SetChangeListener<PrimitiveAttribute>() {

	    @Override
	    public void onChanged(Change<? extends PrimitiveAttribute> change) {
		if (change.wasRemoved()) {
		    PrimitiveAttribute elementRemoved = change.getElementRemoved();
		    PrimitiveAttributeEntryView entryView = idToPrimitiveAttributeEntries.get(elementRemoved.getId());
		    if (entryView != null) {
			primitiveAttributeEntriesVBox.getChildren().remove(entryView);
		    }
		}
		if (change.wasAdded()) {
		    addPrimitiveAttributeToEntries(change.getElementAdded());
		}
	    }
	});
    }

    private void addPrimitiveAttributeToEntries(PrimitiveAttribute attribute) {
	PrimitiveAttributeEntryView entryView = new PrimitiveAttributeEntryView(attribute);
	primitiveAttributeEntriesVBox.getChildren().add(entryView);
	idToPrimitiveAttributeEntries.put(attribute.getId(), entryView);
    }

    private void clearReferentialAttributeEntries() {
	idToReferentialAttributeEntries.clear();
	referentialAttributeEntriesVBox.getChildren().clear();
    }

    private void bindReferentialAttributes(PersistenceUnitDescriptor persistenceUnitDescriptor) {
	ObservableSet<? extends ReferentialAttribute> referentialAttributes = persistenceUnitDescriptor
		.getUnmodifiableReferentialAttributes();
	for (ReferentialAttribute attribute : referentialAttributes) {
	    addReferentialAttributeToEntriesIfNavigableOrMain(attribute);
	}
	referentialAttributes.addListener(new SetChangeListener<ReferentialAttribute>() {

	    @Override
	    public void onChanged(Change<? extends ReferentialAttribute> change) {
		if (change.wasRemoved()) {
		    ReferentialAttribute elementRemoved = change.getElementRemoved();
		    ReferentialAttributeEntryView entryView = idToReferentialAttributeEntries
			    .get(elementRemoved.getId());
		    if (entryView != null) {
			referentialAttributeEntriesVBox.getChildren().remove(entryView);
		    }
		}
		if (change.wasAdded()) {
		    addReferentialAttributeToEntriesIfNavigableOrMain(change.getElementAdded());
		}
	    }
	});
    }

    private void addReferentialAttributeToEntriesIfNavigableOrMain(ReferentialAttribute attribute) {
	if (attribute.isNavigable() || attribute.getOwnerType() == OwnerType.CLASSIFIER) {
	    ReferentialAttributeEntryView entryView = new ReferentialAttributeEntryView(attribute);
	    referentialAttributeEntriesVBox.getChildren().add(entryView);
	    idToReferentialAttributeEntries.put(attribute.getId(), entryView);
	}
    }
}
