package net.atlassian.cmathtutor.view.diagram;

import java.io.IOException;
import java.util.Optional;

import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import net.atlassian.cmathtutor.domain.persistence.ConstraintType;
import net.atlassian.cmathtutor.domain.persistence.PrimitiveAttribute;
import net.atlassian.cmathtutor.domain.persistence.PrimitiveType;
import net.atlassian.cmathtutor.domain.persistence.translate.UnimplementedEnumConstantException;
import net.atlassian.cmathtutor.helper.ChangeListenerRegistryHelper;

public class PrimitiveAttributeEntryView extends HBox {

    @FXML
    private Image NULLABLE_IMAGE;
    @FXML
    private Image NON_NULLABLE_IMAGE;

    @FXML
    private ImageView uniqueImageView;
    @FXML
    private ImageView nullableImageView;
    @FXML
    private Label nameLabel;
    @FXML
    private Label typeLabel;

    private ChangeListenerRegistryHelper listenerRegistryHelper = new ChangeListenerRegistryHelper();

    public PrimitiveAttributeEntryView(PrimitiveAttribute primitiveAttribute) {
	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("primitiveAttributeEntry.fxml"));
	fxmlLoader.setRoot(this);
	fxmlLoader.setController(this);
	try {
	    fxmlLoader.load();
	} catch (IOException exception) {
	    throw new RuntimeException(exception);
	}
	initBindings(primitiveAttribute);
    }

    private void initBindings(PrimitiveAttribute primitiveAttribute) {
	nameLabel.textProperty().bind(primitiveAttribute.nameProperty());
	setType(primitiveAttribute.getType());
	primitiveAttribute.typeProperty()
		.addListener(listenerRegistryHelper.registerChangeListener((observable, oldValue, newValue) -> {
		    setType(newValue);
		}));
	boolean isUnique = false;
	boolean isNonNull = false;
	ObservableSet<ConstraintType> constraints = primitiveAttribute.getUnmodifiableConstraints();
	for (ConstraintType constraint : constraints) {
	    switch (constraint) {
	    case NON_NULL:
		isNonNull = true;
		break;
	    case UNIQUE:
		isUnique = true;
		break;
	    default:
		throw new UnimplementedEnumConstantException(constraint);
	    }
	}
	displayNullabilityImage(isNonNull);
	displayUniquenessImage(isUnique);
	constraints.addListener(new SetChangeListener<ConstraintType>() {

	    @Override
	    public void onChanged(Change<? extends ConstraintType> change) {
		if (change.wasRemoved()) {
		    ConstraintType elementRemoved = change.getElementRemoved();
		    switch (elementRemoved) {
		    case NON_NULL:
			displayNullableImage();
			break;
		    case UNIQUE:
			displayNonUniqueImage();
			break;
		    default:
			throw new UnimplementedEnumConstantException(elementRemoved);
		    }
		}
		if (change.wasAdded()) {
		    ConstraintType elementAdded = change.getElementAdded();
		    switch (elementAdded) {
		    case NON_NULL:
			displayNonNullImage();
			break;
		    case UNIQUE:
			displayUniqueImage();
			break;
		    default:
			throw new UnimplementedEnumConstantException(elementAdded);
		    }
		}
	    }
	});
    }

    private void setType(PrimitiveType newValue) {
	String type = Optional.ofNullable(newValue).map(PrimitiveType::getAppearance).orElse(null);
	typeLabel.setText(type);
    }

    private void displayNullabilityImage(boolean isNonNull) {
	if (isNonNull) {
	    displayNonNullImage();
	} else {
	    displayNullableImage();
	}
    }

    private void displayUniquenessImage(boolean isUnique) {
	if (isUnique) {
	    displayUniqueImage();
	} else {
	    displayNonUniqueImage();
	}
    }

    private void displayNullableImage() {
	nullableImageView.setImage(NULLABLE_IMAGE);
    }

    private void displayNonUniqueImage() {
	uniqueImageView.setVisible(false);
    }

    protected void displayNonNullImage() {
	nullableImageView.setImage(NON_NULLABLE_IMAGE);
    }

    protected void displayUniqueImage() {
	uniqueImageView.setVisible(true);
    }
}
