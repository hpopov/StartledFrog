package net.atlassian.cmathtutor.fxdiagram;

import de.fxdiagram.core.anchors.ArrowHead;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Node;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;
import net.atlassian.cmathtutor.domain.persistence.descriptor.AssociationDescriptor;
import net.atlassian.cmathtutor.util.Lazy;
import net.atlassian.cmathtutor.view.diagram.ReferentialAttributeArrowHeadView;

@Slf4j
@NoArgsConstructor
public class ReferentialAttributeArrowHead extends ArrowHead {

    private static final double STUB_WIDTH = 50d;
    private static final double STUB_HEIGHT = 5d;

    private Lazy<ReferentialAttributeArrowHeadView> lazyView = Lazy.of(ReferentialAttributeArrowHeadView::new);

    public ReferentialAttributeArrowHead(AssociationConnection connection, boolean isSource) {
        super(connection, STUB_WIDTH, STUB_HEIGHT, null, isSource);
    }

    @Override
    public Node createNode() {
        log.debug("Creating the node...");
        ReferentialAttributeArrowHeadView view = lazyView.get();
        // view.setLayoutX(-view.getShapeWidth() / 2);
        // view.setLayoutY(10.0D);
        return view;
    }

    public void setAssociationDescriptor(AssociationDescriptor associationDescriptor) {
        ReferentialAttribute referentialAttribute = getIsSource()
                ? associationDescriptor.getElementAttribute()
                : associationDescriptor.getContainerAttribute();
        lazyView.get().setReferentialAttribute(referentialAttribute);
    }

    @Override
    public DoubleProperty widthProperty() {
        return lazyView.get().prefWidthProperty();
    }

    @Override
    public double getWidth() {
        double width = lazyView.get().getWidth();
        log.debug("Getting width: {}", width);
        return width;
    }

    @Override
    public void setWidth(double width) {
        log.debug("The attempt to set width was detected, but nothing to do...");// TODO
    }

    @Override
    public double getLineCut() {
        double lineCut = super.getLineCut() + lazyView.get().getShapeWidth();
        // log.debug("Returning lineCut of {}", lineCut);
        return lineCut;
    }
}
