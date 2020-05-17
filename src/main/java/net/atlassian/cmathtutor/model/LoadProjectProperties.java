package net.atlassian.cmathtutor.model;

import java.io.File;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.ToString;
import net.atlassian.cmathtutor.Version;

@ToString
public class LoadProjectProperties {

    private ObjectProperty<File> startledFrogProjectFile;
    private StringProperty applicationName;
    private StringProperty rootPackage;
    private ObjectProperty<Version> startledFrogVersion;

    public LoadProjectProperties() {
	startledFrogProjectFile = new SimpleObjectProperty<>();
	applicationName = new SimpleStringProperty();
	rootPackage = new SimpleStringProperty();
	startledFrogVersion = new SimpleObjectProperty<>();
    }

    public final ObjectProperty<File> startledFrogProjectFileProperty() {
	return this.startledFrogProjectFile;
    }

    public final File getStartledFrogProjectFile() {
	return this.startledFrogProjectFileProperty().get();
    }

    public final void setStartledFrogProjectFile(final File startledFrogProjectFile) {
	this.startledFrogProjectFileProperty().set(startledFrogProjectFile);
    }

    public final StringProperty applicationNameProperty() {
	return this.applicationName;
    }

    public final String getApplicationName() {
	return this.applicationNameProperty().get();
    }

    public final void setApplicationName(final String applicationName) {
	this.applicationNameProperty().set(applicationName);
    }

    public final StringProperty rootPackageProperty() {
	return this.rootPackage;
    }

    public final String getRootPackage() {
	return this.rootPackageProperty().get();
    }

    public final void setRootPackage(final String rootPackage) {
	this.rootPackageProperty().set(rootPackage);
    }

    public final ObjectProperty<Version> startledFrogVersionProperty() {
	return this.startledFrogVersion;
    }

    public final Version getStartledFrogVersion() {
	return this.startledFrogVersionProperty().get();
    }

    public final void setStartledFrogVersion(final Version startledFrogVersion) {
	this.startledFrogVersionProperty().set(startledFrogVersion);
    }

}
