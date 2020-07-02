package net.atlassian.cmathtutor.presenter;

import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import lombok.Getter;
import net.atlassian.cmathtutor.domain.configuration.model.GlobalConfigurationModel;
import net.atlassian.cmathtutor.helper.ChangeListenerRegistryHelper;
import net.atlassian.cmathtutor.service.ConfigurationDomainService;

public class ConfigurationPresenter implements Initializable {

    @FXML
    VBox root;
    @FXML
    TextField dbUserTextField;
    @FXML
    TextField dbSchemaNameTextField;
    @FXML
    TextField dockerMachineIpTextField;
    @FXML
    PasswordField dbRootPasswordField;
    @FXML
    PasswordField dbUserPasswordField;

    @Inject
    private ConfigurationDomainService configurationDomainService;

    @Getter
    private GlobalConfigurationModel globalConfigurationModel;

    private FileChooser jdbcDriverPathChooser = new FileChooser();
    private ChangeListenerRegistryHelper changeListenerRegistryHelper = new ChangeListenerRegistryHelper();

    public ConfigurationPresenter() {
        jdbcDriverPathChooser.getExtensionFilters().add(
                new ExtensionFilter("Jar file", "*.jar"));
        jdbcDriverPathChooser.setTitle("Choose JDBC driver jar");
    }

    @Override
    public void initialize(URL var1, ResourceBundle var2) {
        globalConfigurationModel = configurationDomainService.loadConfigurationModel();

        dbRootPasswordField.setText(globalConfigurationModel.getRootPassword());
        dbRootPasswordField.textProperty()
                .addListener(changeListenerRegistryHelper.registerChangeListener(
                        (observable, oldV, newV) -> { globalConfigurationModel.setRootPassword(newV); }));
        dbSchemaNameTextField.setText(globalConfigurationModel.getDatabase());
        dbSchemaNameTextField.textProperty()
                .addListener(changeListenerRegistryHelper.registerChangeListener(
                        (observable, oldV, newV) -> { globalConfigurationModel.setDatabase(newV); }));
        dbUserPasswordField.setText(globalConfigurationModel.getPassword());
        dbUserPasswordField.textProperty()
                .addListener(changeListenerRegistryHelper.registerChangeListener(
                        (observable, oldV, newV) -> { globalConfigurationModel.setPassword(newV); }));
        dbUserTextField.setText(globalConfigurationModel.getUser());
        dbUserTextField.textProperty()
                .addListener(changeListenerRegistryHelper.registerChangeListener(
                        (observable, oldV, newV) -> { globalConfigurationModel.setUser(newV); }));
        dockerMachineIpTextField.setText(globalConfigurationModel.getDockerMachineIp());
        dockerMachineIpTextField.textProperty()
                .addListener(changeListenerRegistryHelper.registerChangeListener(
                        (observable, oldV, newV) -> { globalConfigurationModel.setDockerMachineIp(newV); }));
    }
}
