package net.atlassian.cmathtutor.model;

import java.io.File;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.ToString;

@ToString
public class CreateProjectProperties {

    private ObjectProperty<File> projectDestinationFolder;
    private StringProperty applicationName;
    private StringProperty rootPackage;
    private StringProperty projectDescription;

    public CreateProjectProperties() {
	projectDestinationFolder = new SimpleObjectProperty<>();
	applicationName = new SimpleStringProperty();
	rootPackage = new SimpleStringProperty();
	projectDescription = new SimpleStringProperty();
    }

    public final ObjectProperty<File> projectDestinationFolderProperty() {
	return this.projectDestinationFolder;
    }

    public final File getProjectDestinationFolder() {
	return this.projectDestinationFolderProperty().getValue();
    }

    public final void setProjectDestinationFolder(final File projectDestinationFolder) {
	this.projectDestinationFolderProperty().setValue(projectDestinationFolder);
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

    public final StringProperty projectDescriptionProperty() {
	return this.projectDescription;
    }

    public final String getProjectDescription() {
	return this.projectDescriptionProperty().get();
    }

    public final void setProjectDescription(final String projectDescription) {
	this.projectDescriptionProperty().set(projectDescription);
    }

}
