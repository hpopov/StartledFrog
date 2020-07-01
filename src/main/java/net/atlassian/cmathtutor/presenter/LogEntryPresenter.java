package net.atlassian.cmathtutor.presenter;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class LogEntryPresenter {

    @FXML
    Label entryTextLabel;

    public void setText(String text) {
        entryTextLabel.setText(text);
    }
}
