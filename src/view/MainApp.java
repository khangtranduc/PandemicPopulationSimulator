package view;

import controller.LinkedControllers;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * splash screen
 * modified from https://gist.github.com/jewelsea/2305098
 * 
 */
public class MainApp extends Application {

    private Pane splashLayout;
    private ProgressBar loadProgress;
    private Label progressText;
    private ImageView imageView;
    private static final int SPLASH_WIDTH = 400;
    private static final int SPLASH_HEIGHT = 200;
 
    public static void main(String[] args){
        launch(args);
    }
 
    @Override
    public void init() throws IOException {
        splashLayout = FXMLLoader.load(getClass().getResource("SplashScreen.fxml"));
        loadProgress = (ProgressBar) splashLayout.lookup("#progressBar");
        progressText = (Label) splashLayout.lookup("#progressLabel");
        imageView = (ImageView) splashLayout.lookup("#imageView");
    }
 
    @Override
    public void start(final Stage initStage) throws Exception {
        final Task<ObservableList<String>> friendTask = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() throws InterruptedException {
                ObservableList<String> messages = FXCollections.observableArrayList(
                        "Setting up things",
                        "Magnetising magnetic fields",
                        "Training Algorithms",
                        "Getting low scores for CS",
                        "Doing barrel rolls",
                        "Developing a cure for the Varus",
                        "Electrolysing concentrated salt water",
                        "Initializing Susceptibles",
                        "Infecting Patient Zeros",
                        "Transferring diseases from bats to humans"
                );

                updateMessage("Loading Application...");
                for (int i = 0; i < messages.size(); i++) {
                    Thread.sleep(400);
                    updateProgress(i + 1, messages.size());
                    updateMessage(messages.get((new Random()).nextInt(messages.size())));
                }
                Thread.sleep(400);
                updateMessage("Application Loaded!");

                return messages;
            }
        };
 
        showSplash(
                initStage,
                friendTask,
                () -> {
                    try{
                        showMainStage();
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                }
        );
        new Thread(friendTask).start();
    }
 
    private void showMainStage() throws IOException {
        Stage primaryStage = new Stage();
        LinkedControllers.primaryStage = primaryStage;
        Image image = new Image(new FileInputStream(System.getProperty("user.dir") + "/src/resources/gasMask.png"));

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

            LinkedControllers.clearText();
            System.out.println("All necessary resets has been done");
        });

        Scene scene = new Scene(root);
        scene.getStylesheets().add("view/FlatBee.css");

        primaryStage.setResizable(false);
        primaryStage.getIcons().add(image);
        primaryStage.setScene(scene);
        primaryStage.setTitle(LinkedControllers.rb.getString("SimBanner"));
        primaryStage.show();
    }
 
    private void showSplash(final Stage initStage, Task<?> task, InitCompletionHandler initCompletionHandler) {
        progressText.textProperty().bind(task.messageProperty());
        loadProgress.progressProperty().bind(task.progressProperty());

        //logo flash
        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setNode(imageView);
        fadeTransition.setDuration(Duration.seconds(1.5));
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0);
        fadeTransition.setAutoReverse(true);
        fadeTransition.setCycleCount(Timeline.INDEFINITE);

        (new Thread(fadeTransition::play)).start();

        task.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                loadProgress.progressProperty().unbind();
                loadProgress.setProgress(1);
                initStage.toFront();
                FadeTransition fadeSplash = new FadeTransition(Duration.seconds(0.5), splashLayout);
                fadeSplash.setFromValue(1.0);
                fadeSplash.setToValue(0.0);
                fadeSplash.setOnFinished(actionEvent -> initStage.hide());
                fadeSplash.play();

                initCompletionHandler.complete();
            }
        });
 
        Scene splashScene = new Scene(splashLayout);
        splashScene.getStylesheets().add("view/FlatBee.css");
        initStage.initStyle(StageStyle.UNDECORATED);
        initStage.setScene(splashScene);
        initStage.show();
    }
 
    public interface InitCompletionHandler {
        public void complete();
    }
}
