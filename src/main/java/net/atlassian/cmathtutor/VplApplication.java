package net.atlassian.cmathtutor;

import java.io.IOException;

import com.airhacks.afterburner.injection.Injector;
import com.airhacks.afterburner.views.FXMLView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.atlassian.cmathtutor.view.MainView;

public class VplApplication extends Application {

    public static void main(String[] args) throws Exception {
	launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
	FXMLView view = new MainView();
	Scene scene = new Scene(view.getView());
	stage.setScene(scene);
	stage.setTitle("Startled Frog -- visual programming language");
	stage.show();
    }

    @Override
    public void stop() throws Exception {
	Injector.forgetAll();
    }
}
