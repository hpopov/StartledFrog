package net.atlassian.cmathtutor;

import java.io.IOException;

import com.airhacks.afterburner.injection.Injector;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.atlassian.cmathtutor.view.StartView;

public class VplApplication extends Application {

    public static final String MAIN_TITLE = "Startled Frog";
    private StartView view;

    public static void main(String[] args) throws Exception {
	launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
	view = new StartView();
	Scene scene = new Scene(view.getView());
	stage.setScene(scene);
	stage.setTitle(MAIN_TITLE + " -- " + view.getTitle());
	stage.show();
    }

    @Override
    public void stop() throws Exception {
	Injector.forgetAll();
    }
}
