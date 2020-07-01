package net.atlassian.cmathtutor.view.diagram;

import java.io.IOException;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import net.atlassian.cmathtutor.domain.persistence.AttributeArity;
import net.atlassian.cmathtutor.domain.persistence.PersistenceUnit;
import net.atlassian.cmathtutor.domain.persistence.ReferentialAttribute;
import net.atlassian.cmathtutor.domain.persistence.translate.TranslatorHelper;
import net.atlassian.cmathtutor.helper.ChangeListenerRegistryHelper;

public class ReferentialAttributeEntryView extends HBox {

    private static final String ARITY_FORMAT = "(%s)";
    @FXML
    private Label nameLabel;
    @FXML
    private Label typeLabel;
    @FXML
    private Label arityLabel;

    private ChangeListenerRegistryHelper listenerRegistryHelper = new ChangeListenerRegistryHelper();

    public ReferentialAttributeEntryView(ReferentialAttribute referentialAttribute) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("referentialAttributeEntry.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        initBindings(referentialAttribute);
    }

    private void initBindings(ReferentialAttribute referentialAttribute) {
        setArity(referentialAttribute.getArity());
        referentialAttribute.arityProperty()
                .addListener(listenerRegistryHelper
                        .registerChangeListener((observable, oldValue, newValue) -> { setArity(newValue); }));
        ReferentialAttribute anotherAttr = TranslatorHelper.getAnotherAttributeFromAssociation(referentialAttribute);
        setType(anotherAttr.getParentClassifier());
        anotherAttr.parentClassifierProperty()
                .addListener(listenerRegistryHelper
                        .registerChangeListener((observable, oldValue, newValue) -> { setType(newValue); }));
        nameLabel.textProperty().bind(referentialAttribute.nameProperty());
    }

    private void setArity(AttributeArity newValue) {
        String arity = Optional.ofNullable(newValue).map(AttributeArity::getAppearance).map(this::formatArity)
                .orElse(null);
        arityLabel.setText(arity);
    }

    private String formatArity(String arity) {
        return String.format(ARITY_FORMAT, arity);
    }

    private void setType(PersistenceUnit newValue) {
        String type = Optional.ofNullable(newValue).map(PersistenceUnit::getName).orElse(null);
        typeLabel.setText(type);
    }
}
