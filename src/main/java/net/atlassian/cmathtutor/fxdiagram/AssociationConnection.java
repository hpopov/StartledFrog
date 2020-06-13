package net.atlassian.cmathtutor.fxdiagram;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.fxdiagram.core.XConnection;
import de.fxdiagram.core.XConnectionLabel;
import de.fxdiagram.core.XNode;
import de.fxdiagram.core.model.ModelElementImpl;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.domain.persistence.OwnerType;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;
import net.atlassian.cmathtutor.domain.persistence.descriptor.AbstractDescriptor;
import net.atlassian.cmathtutor.domain.persistence.descriptor.AssociationDescriptor;
import net.atlassian.cmathtutor.helper.ChangeListenerRegistryHelper;

@Slf4j
@NoArgsConstructor
public class AssociationConnection extends XConnection {

    private StringProperty associationDescriptorId = new SimpleStringProperty(this, "id");

    private ObjectProperty<AssociationDescriptor> associationDescriptor = new SimpleObjectProperty<>();

    private ChangeListenerRegistryHelper listenerRegistryHelper = new ChangeListenerRegistryHelper();

    public AssociationConnection(XNode source, XNode target, AssociationDescriptor associationDescriptor) {
	super(source, target);
	setSourceArrowHead(new ReferentialAttributeArrowHead(this, true));
	setTargetArrowHead(new ReferentialAttributeArrowHead(this, false));
	new AssociationConnectionLabel(this, true);
	new AssociationConnectionLabel(this, false);
	setAssociationDescriptor(associationDescriptor);
    }

    @Override
    public ReferentialAttributeArrowHead getSourceArrowHead() {
	return (ReferentialAttributeArrowHead) super.getSourceArrowHead();
    }

    @Override
    public ReferentialAttributeArrowHead getTargetArrowHead() {
	return (ReferentialAttributeArrowHead) super.getTargetArrowHead();
    }

    @Override
    public void populate(ModelElementImpl modelElement) {
	super.populate(modelElement);
	modelElement.addProperty(associationDescriptorId, String.class);
    }

    public ReadOnlyObjectProperty<AssociationDescriptor> associationDescriptorProperty() {
	return this.associationDescriptor;
    }

    public AssociationDescriptor getAssociationDescriptor() {
	return this.associationDescriptorProperty().get();
    }

    public List<AssociationConnectionLabel> getAssociationLabels() {
	return getLabels().stream()
		.filter(label -> label instanceof AssociationConnectionLabel)
		.map(AssociationConnectionLabel.class::cast)
		.collect(Collectors.toList());
    }

    public void setAssociationDescriptor(AssociationDescriptor associationDescriptor) {
	this.associationDescriptor.set(associationDescriptor);
	associationDescriptorId
		.setValue(Optional.ofNullable(associationDescriptor).map(AbstractDescriptor::getId).orElse(null));
	getSourceArrowHead().setAssociationDescriptor(associationDescriptor);
	getTargetArrowHead().setAssociationDescriptor(associationDescriptor);
	List<AssociationConnectionLabel> labels = getAssociationLabels();
	Predicate<? super AssociationConnectionLabel> isContainerPredicate = AssociationConnectionLabel::isContainer;
	XConnectionLabel containerLabel = labels.stream()
		.filter(isContainerPredicate)
		.findAny().orElse(null);
	bindAttributeToLabel(associationDescriptor.getContainerAttribute(), containerLabel);
	XConnectionLabel elementLabel = labels.stream()
		.filter(isContainerPredicate.negate())
		.findAny().orElse(null);
	bindAttributeToLabel(associationDescriptor.getElementAttribute(), elementLabel);
    }

    private void bindAttributeToLabel(ReferentialAttribute attribute, XConnectionLabel label) {
	if (label == null) {
	    log.warn("label is null!");
	    return;
	}
	StringProperty labelTextProperty = label.getText().textProperty();
	if (attribute.isNavigable() || OwnerType.CLASSIFIER == attribute.getOwnerType()) {
	    labelTextProperty.bind(attribute.nameProperty());
	}
	attribute.navigableProperty().or(attribute.ownerTypeProperty().isEqualTo(OwnerType.CLASSIFIER))
		.addListener(listenerRegistryHelper.registerChangeListener((observable, oldValue, newValue) -> {
		    if (newValue && false == labelTextProperty.isBound()) {
			labelTextProperty.bind(attribute.nameProperty());
		    } else if (false == newValue && labelTextProperty.isBound()) {
			labelTextProperty.unbind();
			labelTextProperty.set(null);
		    }
		}));
    }

    public StringProperty associationDescriptorIdProperty() {
	return this.associationDescriptorId;
    }

    public String getAssociationDescriptorId() {
	return this.associationDescriptorIdProperty().get();
    }

    public void setAssociationDescriptorId(final String associationDescriptorId) {
	this.associationDescriptorIdProperty().set(associationDescriptorId);
    }

}
