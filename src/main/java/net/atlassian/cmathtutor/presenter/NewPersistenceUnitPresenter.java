package net.atlassian.cmathtutor.presenter;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener.Change;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.Getter;
import lombok.Setter;
import net.atlassian.cmathtutor.domain.persistence.ConstraintType;
import net.atlassian.cmathtutor.domain.persistence.PrimitiveType;
import net.atlassian.cmathtutor.domain.persistence.descriptor.IllegalOperationException;
import net.atlassian.cmathtutor.domain.persistence.descriptor.PersistenceDescriptor;
import net.atlassian.cmathtutor.domain.persistence.descriptor.PersistenceUnitDescriptor;
import net.atlassian.cmathtutor.domain.persistence.model.PersistenceUnitModel;
import net.atlassian.cmathtutor.domain.persistence.model.PrimitiveAttributeModel;
import net.atlassian.cmathtutor.util.UidUtil;

public class NewPersistenceUnitPresenter implements Initializable {

    private static final String UNABLE_TO_CREATE_PERSISTENCE_UNIT_MESSAGE = "Unable to create persistence unit";
    @FXML
    TextField unitNameTextField;
    @FXML
    TableView<PrimitiveAttributeModel> primitiveAttributesTable;
    @FXML
    TextField newAttributeNameTextField;
    @FXML
    ChoiceBox<String> typeChoiceBox;
    @FXML
    CheckBox nullableCheckBox;
    @FXML
    CheckBox uniqueCheckBox;
    @FXML
    Button createPersistenceUnitButton;
    @FXML
    Button addNewPrimitiveAttributeButton;

    @Setter
    private PersistenceDescriptor persistenceDescriptor;

    private PersistenceUnitModel persistenceUnitModel;
    @Getter
    private PersistenceUnitDescriptor persistenceUnitDescriptor;

    @FXML
    Button closeButton;

    public NewPersistenceUnitPresenter() {
	persistenceUnitModel = new PersistenceUnitModel(UidUtil.getUId());
    }

    @Override
    public void initialize(URL var1, ResourceBundle var2) {
	typeChoiceBox.getItems().addAll(
		Arrays.stream(PrimitiveType.values())
			.map(PrimitiveType::getAppearance)
			.collect(Collectors.toList()));
	typeChoiceBox.setValue(typeChoiceBox.getItems().get(0));
	setupAttributesTable();
	persistenceUnitModel.getPrimitiveAttributes()
		.addListener((Change<? extends PrimitiveAttributeModel> change) -> {
		    if (change.wasAdded()) {
			primitiveAttributesTable.getItems().add(change.getElementAdded());
		    }
		    if (change.wasRemoved()) {
			primitiveAttributesTable.getItems().remove(change.getElementRemoved());
		    }
		});
	persistenceUnitModel.nameProperty().bind(unitNameTextField.textProperty());
    }

    @SuppressWarnings("unchecked")
    private void setupAttributesTable() {
	TableColumn<PrimitiveAttributeModel, String> nameCol = new TableColumn<>("Name");
	TableColumn<PrimitiveAttributeModel, String> typeCol = new TableColumn<>("Type");
	TableColumn<PrimitiveAttributeModel, String> constraintsCol = new TableColumn<>("Constraints");

	nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
	typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
	constraintsCol.setCellValueFactory(new PropertyValueFactory<>("unmodifiableConstraints"));
	primitiveAttributesTable.getColumns().addAll(nameCol, typeCol, constraintsCol);
	primitiveAttributesTable
		.setItems(FXCollections.observableArrayList(persistenceUnitModel.getUnmodifiablePrimitiveAttributes()));
    }

    @FXML
    public void createPersistenceUnit() {
	try {
	    persistenceUnitDescriptor = persistenceDescriptor.addNewPersistenceUnit(persistenceUnitModel);
	    persistenceUnitModel.nameProperty().unbind();
	    close();
	} catch (IllegalOperationException e) {
	    Alert alert = new Alert(AlertType.ERROR);
	    alert.setHeaderText(UNABLE_TO_CREATE_PERSISTENCE_UNIT_MESSAGE);
	    alert.setContentText(e.getMessage());
	    alert.showAndWait();
	}
    }

    @FXML
    public void addNewPrimitiveAttribute() {
	PrimitiveAttributeModel primitiveAttributeModel = new PrimitiveAttributeModel(UidUtil.getUId());
	primitiveAttributeModel.setName(newAttributeNameTextField.getText());
	String value = typeChoiceBox.getValue();
	primitiveAttributeModel.setType(Arrays.stream(PrimitiveType.values())
		.filter(pt -> pt.getAppearance().equals(value))
		.findAny().orElseThrow(() -> new IllegalStateException(
			"PrimitiveType constant with appearance " + value + " does not exist")));
	primitiveAttributeModel.setParentClassifier(persistenceUnitModel);
	if (false == nullableCheckBox.isSelected()) {
	    primitiveAttributeModel.getConstraints().add(ConstraintType.NON_NULL);
	}
	if (uniqueCheckBox.isSelected()) {
	    primitiveAttributeModel.getConstraints().add(ConstraintType.UNIQUE);
	}
	persistenceUnitModel.getPrimitiveAttributes().add(primitiveAttributeModel);
    }

    @FXML
    public void close() {
	unitNameTextField.getScene().getWindow().hide();
    }

}
