package net.atlassian.cmathtutor.presenter;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.NodeOrientation;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import lombok.Getter;
import lombok.Setter;
import net.atlassian.cmathtutor.domain.persistence.AggregationKind;
import net.atlassian.cmathtutor.domain.persistence.AttributeArity;
import net.atlassian.cmathtutor.domain.persistence.OwnerType;
import net.atlassian.cmathtutor.domain.persistence.descriptor.AssociationDescriptor;
import net.atlassian.cmathtutor.domain.persistence.descriptor.IllegalOperationException;
import net.atlassian.cmathtutor.domain.persistence.descriptor.PersistenceDescriptor;
import net.atlassian.cmathtutor.domain.persistence.model.AssociationModel;
import net.atlassian.cmathtutor.domain.persistence.model.ReferentialAttributeModel;
import net.atlassian.cmathtutor.fxdiagram.PersistenceUnitNode;
import net.atlassian.cmathtutor.helper.ChangeListenerRegistryHelper;
import net.atlassian.cmathtutor.util.CaseUtil;
import net.atlassian.cmathtutor.util.UidUtil;

public class NewAssociationConnectionPresenter implements Initializable {

    private static final String UNABLE_TO_CREATE_ASSOCIATION_MESSAGE = "Unable to create association";

    @FXML
    ImageView containerNodeImageView;

    @FXML
    ImageView elementNodeImageView;

    @FXML
    ChoiceBox<AggregationKind> aggregationKindChoiceBox;

    @FXML
    CheckBox elementNavigabilityCheckBox;

    @FXML
    CheckBox containerNavigabilityCheckBox;

    @FXML
    RadioButton toContainerPrimaryReferenceRadioButton;

    @FXML
    ToggleGroup toggleGroup;

    @FXML
    RadioButton toElementPrimaryReferenceRadioButton;

    @FXML
    ChoiceBox<String> containerArityChoiceBox;

    @FXML
    ChoiceBox<String> elementArityChoiceBox;

    @FXML
    Button closeButton;

    @FXML
    Button createPersistenceUnitButton;

    @FXML
    ImageView primaryReferenceImageView;

    @FXML
    TextField containerAttributeNameTextLabel;

    @FXML
    TextField elementAttributeNameTextLabel;

    @Getter
    private AssociationDescriptor associationDescriptor;

    @Setter
    private PersistenceDescriptor persistenceDescriptor;
    private PersistenceUnitNode containerNode;
    private PersistenceUnitNode elementNode;

    private ChangeListenerRegistryHelper changeListenerRegistryHelper = new ChangeListenerRegistryHelper();

    @Override
    public void initialize(URL var1, ResourceBundle var2) {
	toElementPrimaryReferenceRadioButton.selectedProperty()
		.addListener(changeListenerRegistryHelper.registerChangeListener((observable, oldV, newV) -> {
		    if (newV) {
			primaryReferenceImageView.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
		    } else {
			primaryReferenceImageView.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
		    }
		}));
	aggregationKindChoiceBox.getItems().addAll(AggregationKind.values());
	aggregationKindChoiceBox.setValue(aggregationKindChoiceBox.getItems().get(0));
	List<String> attributeArities = Arrays.stream(AttributeArity.values())
		.map(AttributeArity::getAppearance)
		.collect(Collectors.toList());
	containerArityChoiceBox.getItems().addAll(attributeArities);
	containerArityChoiceBox.setValue(containerArityChoiceBox.getItems().get(0));
	elementArityChoiceBox.getItems().addAll(attributeArities);
	elementArityChoiceBox.setValue(elementArityChoiceBox.getItems().get(0));

	containerAttributeNameTextLabel.focusedProperty()
		.addListener(changeListenerRegistryHelper.registerChangeListener((observable, oldV, newV) -> {
		    if (Boolean.FALSE.equals(newV)) {
			containerAttributeNameTextLabel
				.setText(toAttributeName(containerAttributeNameTextLabel.getText()));
		    }
		}));
	elementAttributeNameTextLabel.focusedProperty()
		.addListener(changeListenerRegistryHelper.registerChangeListener((observable, oldV, newV) -> {
		    if (Boolean.FALSE.equals(newV)) {
			elementAttributeNameTextLabel
				.setText(toAttributeName(elementAttributeNameTextLabel.getText()));
		    }
		}));
    }

    @FXML
    public void close() {
	aggregationKindChoiceBox.getScene().getWindow().hide();
    }

    // TODO: extract shallow model and save data in it instead, then map it to
    // association model here
    @FXML
    public void createAssociation() {
	AssociationModel associationModel = new AssociationModel(UidUtil.getUId());

	associationModel.setAggregationKind(aggregationKindChoiceBox.getValue());
	ReferentialAttributeModel containerAttribute = new ReferentialAttributeModel(UidUtil.getUId());
	containerAttribute.setArity(defineAttributeArity(elementArityChoiceBox));
	containerAttribute.setName(toAttributeName(elementAttributeNameTextLabel.getText()));
	containerAttribute.setNavigable(elementNavigabilityCheckBox.isSelected());
	containerAttribute.setOwnerType(defineOwnerType(toElementPrimaryReferenceRadioButton.isSelected()));
	containerAttribute.setAssociation(associationModel);
	containerAttribute
		.setParentClassifier(containerNode.getPersistenceUnitDescriptor().getWrappedPersistenceUnit());
	associationModel.setContainerAttribute(containerAttribute);
	ReferentialAttributeModel elementAttribute = new ReferentialAttributeModel(UidUtil.getUId());
	elementAttribute.setArity(defineAttributeArity(containerArityChoiceBox));
	elementAttribute.setName(toAttributeName(containerAttributeNameTextLabel.getText()));
	elementAttribute.setNavigable(containerNavigabilityCheckBox.isSelected());
	elementAttribute.setOwnerType(defineOwnerType(toContainerPrimaryReferenceRadioButton.isSelected()));
	elementAttribute.setAssociation(associationModel);
	elementAttribute.setParentClassifier(elementNode.getPersistenceUnitDescriptor().getWrappedPersistenceUnit());
	associationModel.setElementAttribute(elementAttribute);

	try {
	    associationDescriptor = persistenceDescriptor.addNewAssociation(associationModel);
	    close();
	} catch (IllegalOperationException e) {
	    Alert alert = new Alert(AlertType.ERROR);
	    alert.setHeaderText(UNABLE_TO_CREATE_ASSOCIATION_MESSAGE);
	    alert.setContentText(e.getMessage());
	    alert.showAndWait();
	}
    }

    private String toAttributeName(String string) {
	return CaseUtil.trimAndLowercaseFirstLetter(string);
    }

    private AttributeArity defineAttributeArity(ChoiceBox<String> arityChoiceBox) {
	String appearance = arityChoiceBox.getValue();
	return Arrays.stream(AttributeArity.values())
		.filter(v -> appearance.equals(v.getAppearance()))
		.findAny().orElseThrow(() -> new IllegalStateException(
			"Attribute arity with appearance " + appearance + "does not exist"));
    }

    private OwnerType defineOwnerType(boolean primaryReference) {
	return primaryReference ? OwnerType.CLASSIFIER : OwnerType.ASSOCIATION;
    }

    public void setContainerNode(PersistenceUnitNode containerNode) {
	this.containerNode = containerNode;
	WritableImage snapshot = containerNode.snapshot(new SnapshotParameters(), null);
	containerNodeImageView.setImage(snapshot);
	containerAttributeNameTextLabel.setText(CaseUtil.trimAndLowercaseFirstLetter(containerNode.getName()));
    }

    public void setElementNode(PersistenceUnitNode elementNode) {
	this.elementNode = elementNode;
	WritableImage snapshot = elementNode.snapshot(new SnapshotParameters(), null);
	elementNodeImageView.setImage(snapshot);
	elementAttributeNameTextLabel.setText(CaseUtil.trimAndLowercaseFirstLetter(elementNode.getName()));
    }

}
