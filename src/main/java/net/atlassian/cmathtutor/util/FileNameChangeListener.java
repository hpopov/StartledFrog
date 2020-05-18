package net.atlassian.cmathtutor.util;

import java.io.File;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FileNameChangeListener implements ChangeListener<File> {

    private StringProperty fileNameTextProperty;

    @Override
    public void changed(ObservableValue<? extends File> observable, File oldValue, File newValue) {
	if (newValue == null) {
	    fileNameTextProperty.set("...");
	} else {
	    fileNameTextProperty.set(newValue.getName());
	}
    }

}
