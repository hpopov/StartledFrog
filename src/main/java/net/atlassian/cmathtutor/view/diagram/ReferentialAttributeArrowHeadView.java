package net.atlassian.cmathtutor.view.diagram;

import java.io.IOException;
import java.util.Arrays;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import lombok.NonNull;
import net.atlassian.cmathtutor.domain.persistence.AggregationKind;
import net.atlassian.cmathtutor.domain.persistence.Association;
import net.atlassian.cmathtutor.domain.persistence.OwnerType;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;
import net.atlassian.cmathtutor.domain.persistence.translate.UnimplementedEnumConstantException;
import net.atlassian.cmathtutor.helper.ChangeListenerRegistryHelper;

public class ReferentialAttributeArrowHeadView extends HBox {

    @FXML
    Double CONTAINMENT_DIAMOND_WIDTH;
    @FXML
    Double CONTAINMENT_DIAMOND_HEIGHT;
    @FXML
    Double PRIMARY_REFERENCE_CIRCLE_RADIUS;
    @FXML
    Double SECONDARY_REFERENCE_LINE_WIDTH;
    @FXML
    Double SECONDARY_REFERENCE_LINE_HEIGHT;
    @FXML
    Double SECONDARY_REFERENCE_LINE_STROKE_WIDTH;

    @FXML
    Polygon containmentDiamond;
    @FXML
    Circle primaryReferenceCircle;
    @FXML
    Polyline secondaryReferenceLine;

    private ChangeListenerRegistryHelper listenerRegistryHelper = new ChangeListenerRegistryHelper();

    public ReferentialAttributeArrowHeadView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("referentialAttributeArrowHead.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setControllerFactory(param -> {
            if (param.equals(this.getClass())) {
                return this;
            }
            throw new IllegalStateException("Expected class "
                    + this.getClass().getSimpleName() + " as controller, but got " + param.getSimpleName());
        });
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void setReferentialAttribute(ReferentialAttribute referentialAttribute) {
        initBindings(referentialAttribute);
    }

    private void initBindings(ReferentialAttribute referentialAttribute) {
        Arrays.asList(containmentDiamond, primaryReferenceCircle, secondaryReferenceLine)
                .forEach(shape -> { shape.managedProperty().bind(shape.visibleProperty()); });
        Association association = referentialAttribute.getAssociation();
        if (referentialAttribute == association.getElementAttribute()) {
            adjustContainmentDiamond(association.getAggregationKind());
            association.aggregationKindProperty()
                    .addListener(listenerRegistryHelper.registerChangeListener(
                            (observable, oldValue, newValue) -> { adjustContainmentDiamond(newValue); }));
        } else {
            containmentDiamond.setVisible(false);
        }
        secondaryReferenceLine.visibleProperty().bind(referentialAttribute.navigableProperty());
        primaryReferenceCircle.visibleProperty()
                .bind(referentialAttribute.ownerTypeProperty().isEqualTo(OwnerType.CLASSIFIER));
    }

    private void adjustContainmentDiamond(@NonNull AggregationKind aggregationKind) {
        switch (aggregationKind) {
        case COMPOSITE:
            containmentDiamond.setFill(Color.BLACK);
            containmentDiamond.setVisible(true);
            break;
        case SHARED:
            containmentDiamond.setFill(Color.WHITE);
            containmentDiamond.setVisible(true);
            break;
        case NONE:
            containmentDiamond.setVisible(false);
            break;
        default:
            throw new UnimplementedEnumConstantException(aggregationKind);
        }
    }

    public double getShapeWidth() {
        double shapeWidth = 0d;
        if (containmentDiamond.isManaged()) {
            shapeWidth += CONTAINMENT_DIAMOND_WIDTH;
        }
        if (primaryReferenceCircle.isManaged()) {
            shapeWidth += PRIMARY_REFERENCE_CIRCLE_RADIUS;
        }
        if (secondaryReferenceLine.isManaged()) {
            shapeWidth += SECONDARY_REFERENCE_LINE_STROKE_WIDTH;
        }
        return shapeWidth;
    }
}
