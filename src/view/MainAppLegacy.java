package view;

import controller.LinkedControllers;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * splash screen
 * modified from https://gist.github.com/jewelsea/2305098
 */

public class MainAppLegacy extends Application {
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("UI.fxml"));

        primaryStage.setOnCloseRequest(e -> {
            LinkedControllers.isOnLinked.set(false);
            LinkedControllers.linkedExecutor.shutdown();
            if (LinkedControllers.graphRoot != null){
                ((Stage) LinkedControllers.graphRoot.getScene().getWindow()).close();
            }
            if (LinkedControllers.settingsRoot != null){
                ((Stage) LinkedControllers.settingsRoot.getScene().getWindow()).close();
            }
            while (!LinkedControllers.linkedExecutor.isTerminated()){}
            System.out.println("All threads closed");
        });


        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Epidemic Simulation");
        primaryStage.show();
    }

    public static void main(String[] args){
        launch(args);
    }
}
