package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Disease;
import model.Person;
import model.Simulation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class UIController {
    @FXML
    private Label languageLabel;
    @FXML
    private ComboBox<String> languageCombo;
    @FXML
    private Label quarantineLabel;
    @FXML
    private Button pauseButton;
    @FXML
    private Button resetButton;
    @FXML
    private Button showGraphsButton;
    @FXML
    private Button generalSettingsBtn;
    @FXML
    private Button startButton;
    @FXML
    private Pane canvas;
    @FXML
    private Pane quarantineCanvas;
    @FXML
    private Button graphsBtn;

    //non-FXML components
    Simulation sim;

    private AtomicLong delay = new AtomicLong();
    private long frameBeforeCheck;

    ExecutorService executor = Executors.newCachedThreadPool();
    AtomicBoolean isOn = new AtomicBoolean(false);

    ObservableList<String> languages = FXCollections.observableArrayList(
            "English (US)",
            "Vietnamese (Vietnam)",
            "French (France)"
    );
    private Locale locale = new Locale("en", "US");
    private ResourceBundle rb = ResourceBundle.getBundle("resources/Languages", locale);

    //setting up stages
    private Stage graphStage = new Stage();
    private Stage settingsStage = new Stage();

    @FXML
    public void showGraphs(ActionEvent event){
        try{
            Parent root = new FXMLLoader().load(new FileInputStream(System.getProperty("user.dir") + "/src/view/Graphs.fxml"));
            LinkedControllers.graphRoot = root;
            Image icon = new Image(new FileInputStream(System.getProperty("user.dir") + "/src/resources/graphsIcon.png"));

            graphStage.setResizable(false);
            graphStage.setTitle(rb.getString("graphs"));
            Scene graphScene = new Scene(root);
            graphScene.getStylesheets().add("view/FlatBee.css");
            graphStage.setScene(graphScene);
            graphStage.getIcons().add(icon);
            graphStage.show();
            showGraphsButton.setDisable(true);

            graphStage.setOnCloseRequest(e -> {
                showGraphsButton.setDisable(false);
            });
        }
        catch (IOException e){
            LinkedControllers.throwInvalidAlert(rb.getString("fileMissing"), rb.getString("graphsMissing"));
        }
    }

    @FXML
    public void openSettings(ActionEvent event){
        try{
            Parent root = new FXMLLoader().load(new FileInputStream(System.getProperty("user.dir") + "/src/view/Settings.fxml"));
            LinkedControllers.settingsRoot = root;
            Image icon = new Image(new FileInputStream(System.getProperty("user.dir") + "/src/resources/cogIcon.png"));

            settingsStage.setResizable(false);
            settingsStage.setTitle(rb.getString("generalSettings"));
            Scene settingsScene = new Scene(root);
            settingsScene.getStylesheets().add("view/FlatBee.css");
            settingsStage.setScene(settingsScene);
            settingsStage.getIcons().add(icon);
            settingsStage.show();
            generalSettingsBtn.setDisable(true);

            settingsStage.setOnCloseRequest(e -> {
                generalSettingsBtn.setDisable(false);
            });
        }
        catch (IOException e){
            LinkedControllers.throwInvalidAlert(rb.getString("fileMissing"), rb.getString("settings.fxmlMissing"));
        }
    }

    @FXML
    public void initialize(){
        //load the diseases from the disease.csv file
        ArrayList<String> tempDiseases = new ArrayList<>();
        try{
            Scanner reader = new Scanner(new FileInputStream(System.getProperty("user.dir") + "/src/resources/diseases.csv"));
            while (reader.hasNext()){
                tempDiseases.add(reader.nextLine());
            }
            for (String i : tempDiseases){
                LinkedControllers.addDisease(i);
            }
        }
        catch (IOException e){
            LinkedControllers.throwInvalidAlert(rb.getString("fileMissing"), rb.getString("disease.csvMissing"));
        }

        //initialize default parameters of the simulation
        sim = new Simulation(canvas, quarantineCanvas, 50, 2, Objects.requireNonNull(LinkedControllers.getDisease("covid19")));
        frameBeforeCheck = sim.getSpeed();

        //only one assignment ever.
        LinkedControllers.simulation = sim;
        LinkedControllers.isOnLinked = isOn;
        LinkedControllers.linkedExecutor = executor;
        LinkedControllers.quarantineCanvas = quarantineCanvas;
        LinkedControllers.atomicBooleanMain = isOn;
        LinkedControllers.rb = rb;
        LinkedControllers.startButton = startButton;
        //no more assignments to LinkedControllers other than this.

        //setting up language combo
        languageCombo.setItems(languages);
        languageCombo.getSelectionModel().select("English (US)");
        setLanguage();
    }

    @FXML
    public void startSim(ActionEvent event){
        startButton.setDisable(true);
        executor.execute(() -> {
            isOn.set(true);
            while (isOn.get()){
                step();
                if (sim.isOver()){
                    sim.updateTime();
                    startButton.setDisable(false);
                    isOn.set(false);
                    break;
                }
                try{
                    Thread.sleep(sim.getDuration());
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

        });
    }

    @FXML
    public void stopSim(ActionEvent event){
        isOn.set(false);
        startButton.setDisable(false);
    }

    @FXML
    public void resetSim(ActionEvent event){
        stopSim(event);
        sim.resetSim();
    }

    //advance the simulation by 1 step
    private void step(){
        sim.move();
        Platform.runLater(sim::updatePos);
        //delay increases per frame, then for every second (i.e. frameBeforeCheck), the collision logic and what not will be run, as well as the simulation time will update
        if (delay.get() % frameBeforeCheck == 0){
            Platform.runLater(() -> {
                sim.updateRemove();
                //same probability argument as update Collision.
                sim.updateQuarantine();
                sim.updateCentral();
                //collision only check occasionally because if it is checked
                //continuously, then no matter the infectiousness, the probability of infection will be 100%
                sim.updateCollisions();
            });
            sim.updateTime();
        }
        delay.getAndIncrement();
    }

    @FXML
    public void changeLanguage(ActionEvent event) {
        String selected = languageCombo.getSelectionModel().getSelectedItem();

        switch (selected){
            case "English (US)":
                locale = new Locale("en", "US");
                break;
            case "Vietnamese (Vietnam)":
                locale = new Locale("vi", "VN");
                break;
            case "French (France)":
                locale = new Locale("fr", "FR");
                break;
        }

        rb = ResourceBundle.getBundle("resources/Languages", locale);
        setLanguage();
    }

    private void setLanguage(){
        startButton.setText(rb.getString("start"));
        pauseButton.setText(rb.getString("pause"));
        resetButton.setText(rb.getString("reset"));
        showGraphsButton.setText(rb.getString("showGraphs"));
        LinkedControllers.primaryStage.setTitle(rb.getString("SimBanner"));
        if (rb.getLocale().getLanguage().equals("fr") && rb.getLocale().getCountry().equals("FR")){
            showGraphsButton.setMinWidth(150);
        }
        else {
            showGraphsButton.setMinWidth(100);
        }
        quarantineLabel.setText(rb.getString("quarantine"));
        generalSettingsBtn.setText(rb.getString("generalSettings"));
        languageLabel.setText(rb.getString("language"));
        LinkedControllers.rb = rb;
        if (graphStage.isShowing()){
            graphStage.close();
            showGraphs(new ActionEvent());
        }
        if (settingsStage.isShowing()){
            settingsStage.close();
            openSettings(new ActionEvent());
        }
    }
}

