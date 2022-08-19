package test;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Disease;
import model.InvalidNameException;
import model.ModeNotSetUpException;
import model.Simulation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class AnimationTest extends Application {
    public void start(Stage primaryStage) throws InvalidNameException {
        //initialize panes
        BorderPane root = new BorderPane();
        Pane canvas = new Pane();
        Pane quarantineCanvas = new Pane();
        StackPane Rpane = new StackPane();
        VBox vbox = new VBox();
        HBox hbox = new HBox();

        //set up panes properties
        canvas.setPrefHeight(300);
        canvas.setPrefWidth(400);
        canvas.setStyle("-fx-background-color: lightgrey;");
        quarantineCanvas.setPrefHeight(canvas.getPrefHeight()/2);
        quarantineCanvas.setPrefWidth(canvas.getPrefWidth()/2);
        quarantineCanvas.setStyle("-fx-background-color: lightgreen");

        //initialize nodes
        Button start = new Button("Start");
        Button stop = new Button("Stop");
        Button reset = new Button("Reset");
        Button centralSpot = new Button("Central?");
        Button quarantine = new Button("Quarantine?");
        Label R = new Label("R: ");

        //setting up boxes/ containers
        Rpane.getChildren().add(R);
        Rpane.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(start, stop, centralSpot, quarantine);
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(20);
        hbox.getChildren().addAll(reset, quarantineCanvas);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setSpacing(20);

        //set up diseases
        Disease covid69 = new Disease("Covid-19", 0.5, false);

        //always after setting up canvas.PrefWidth
        Simulation sim = new Simulation(canvas, quarantineCanvas, 50, 2, covid69);

        //central on
        sim.setCentralSpot(0.3, 5, 0.5, 2);

        //quarantine on
        sim.setUpQuarantine(quarantineCanvas, 2);

        //generate new StrackedAreaChart
        StackedAreaChart chart = sim.getChart();

        //set up executor service
        ExecutorService executor = Executors.newCachedThreadPool();
        AtomicBoolean isOn  = new AtomicBoolean(false);

        //testing mechanism
        AtomicLong delay = new AtomicLong();
        long frameBeforeCheck = sim.getSpeed();

        //set on action for buttons
        centralSpot.setOnAction(e -> {
            try {
                sim.setEnableCentralSpot(!sim.isCentralInPlay());
            } catch (ModeNotSetUpException ex) {
                ex.printStackTrace();
            }
        });
        quarantine.setOnAction(e -> {
            try {
                sim.setEnableQuarantine(!sim.isQuarantineInPlay());
            } catch (ModeNotSetUpException ex) {
                ex.printStackTrace();
            }
        });
        start.setOnAction(e -> {
            executor.execute(() ->{
                isOn.set(true);
                while (isOn.get()){
                    //step method body start
                    sim.move();
                    Platform.runLater(sim::updatePos);
                    if (delay.get() % frameBeforeCheck == 0){
                        Platform.runLater(() -> {
                            sim.updateRemove();
                            sim.updateQuarantine();
                            sim.updateCentral();
                            sim.updateCollisions();
                            R.setText("R: " + sim.getR() + ",S: " + sim.getSus_size() +
                                    ",I: " + sim.getIn_size() +
                                    ",Re: " + sim.getRem_size());
                        });
                        sim.updateTime();
                    }
                    delay.getAndIncrement();
                    if (sim.isOver()){
                        sim.updateTime();
                        break;
                    }
                    //step method body end
                    try {
                        Thread.sleep(sim.getDuration());
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            start.setDisable(true);
        });
        stop.setOnAction(e -> {
            isOn.set(false);
            start.setDisable(false);
        });
        reset.setOnAction(e -> {
            isOn.set(false);
            start.setDisable(false);
            sim.resetSim();
        });

        //close out things when window is exited
        primaryStage.setOnCloseRequest(e -> {
            isOn.set(false);
            executor.shutdown();
            while (!executor.isTerminated()){}
            System.out.println("All threads closed");
        });

        //set up root
        root.setCenter(canvas);
        root.setRight(chart);
        root.setLeft(vbox);
        root.setTop(R);
        root.setBottom(hbox);

        //set up stage
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("AnimationTest");
        primaryStage.show();
    }

    public static void main(String[] args){
        launch(args);
    }
}
