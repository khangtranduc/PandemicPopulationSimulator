package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.*;

import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class LinkedControllers {
    public static Simulation simulation;
    public static AtomicBoolean isOnLinked;
    public static ExecutorService linkedExecutor;
    public static Parent graphRoot;
    public static Parent settingsRoot;
    public static Pane quarantineCanvas;
    public static AtomicBoolean atomicBooleanMain;
    public static ResourceBundle rb;
    public static Stage primaryStage;
    public static Button startButton;

    public static ToggleButton half, regular, doubble;

    private final static ObservableList<Disease> diseases = FXCollections.observableArrayList();


    public static void addDisease(String name, double infectiousness, boolean record){
        try{
            Disease disease = new Disease(name, infectiousness, record);
            diseases.add(disease);
        }
        catch(InvalidNameException e){
            LinkedControllers.throwInvalidAlert("Empty Disease Name", "The disease name cannot be nothing, please enter something");
        }
    }

    //used for system loading
    public static void addDisease(String info){
        String[] tokens = info.split(",");
        String name = tokens[0];
        double infectiousness = Double.parseDouble(tokens[1]);
        addDisease(name, infectiousness, false);
    }

    public static void editDisease(Disease disease, String newName, double newInfectiousness){
        disease.editName(newName);
        disease.editInfectiousness(newInfectiousness);
    }

    //finds first instance of disease in the diseases array
    public static Disease getDisease(String name){
        for (Disease i : diseases){
            if (i.getName().equals(name)){
                return i;
            }
        }
        return null;
    }

    public static void deleteDisease(Disease disease){
        diseases.remove(disease);
        disease.updateEntry(disease.toString(), true);
    }

    public static ObservableList<Disease> getDiseases() {
        return diseases;
    }

    public static void loadPopData(Slider turnInt, Slider turnIntVar, Slider normRadius, Slider infRadius){
        try{
            //load pop data//
            Scanner reader = new Scanner(new FileInputStream(System.getProperty("user.dir") + "/src/resources/popSettings.csv"));
            String line = reader.nextLine();
            reader.close();
            String[] tokens = line.split(",");
            turnInt.setValue(Double.parseDouble(tokens[0])/1000);
            System.out.println(Double.parseDouble(tokens[0])/1000);
            turnIntVar.setValue(Double.parseDouble(tokens[1])/1000);
            normRadius.setValue(Double.parseDouble(tokens[2]));
            infRadius.setValue(Double.parseDouble(tokens[3]));
            //end of loading pop data//
        }
        catch (IOException e){
            throwInvalidAlert("File Missing!", "file 'src/resources/popSettings.csv' does not exist");
        }
    }

    public static void loadSimData(Spinner<Integer> popSize, Spinner<Integer> inSize, ComboBox<Disease> comboDisease, ToggleGroup toggleSpeed, Slider int_rem, Slider int_remVar){
        try{
            //load sim data//
            Scanner reader = new Scanner(new FileInputStream(System.getProperty("user.dir") + "/src/resources/simSettings.csv"));
            String line = reader.nextLine();
            reader.close();
            String[] tokens = line.split(",");
            comboDisease.getSelectionModel().select(getDisease(tokens[0]));
            popSize.getValueFactory().setValue(Integer.parseInt(tokens[1]));
            inSize.getValueFactory().setValue(Integer.parseInt(tokens[2]));
            int_rem.setValue(Integer.parseInt(tokens[3]));
            int_remVar.setValue(Integer.parseInt(tokens[4]));
            ToggleButton selected = null;
            switch (tokens[5]){
                case "0.5": selected = half;
                    break;
                case "1.0": selected = regular;
                    break;
                case "2.0": selected = doubble;
                    break;
                default: throwInvalidAlert("Error in file reading", "Invalid or corrupted speed of simulation in src/resources/simSettings.csv");
                    break;
            }

            if (selected != null){
                toggleSpeed.selectToggle(selected);
            }
            //end of loading sim data//
        }
        catch (IOException e){
            throwInvalidAlert("File Missing!", "file 'src/resources/simSettings.csv' does not exist");
        }
    }

    public static void loadCentralData(RadioButton centralRad, Spinner<Integer> max_per_time, Slider travelTime, Slider one_every, Slider centralProp){
        try{
            Scanner reader = new Scanner(new FileInputStream(System.getProperty("user.dir") + "/src/resources/central.csv"));
            String line = reader.nextLine();
            reader.close();
            String[] tokens = line.split(",");
            boolean isOn = Boolean.parseBoolean(tokens[0]);
            centralRad.setSelected(isOn);
            if (isOn){
                max_per_time.getValueFactory().setValue(Integer.parseInt(tokens[1]));
                travelTime.setValue(Double.parseDouble(tokens[2]));
                one_every.setValue(Double.parseDouble(tokens[3]));
                centralProp.setValue(Double.parseDouble(tokens[4]));
            }
        }
        catch (IOException e){
            throwInvalidAlert("File Missing!", "file 'src/resources/central.csv' does not exist");
        }
    }

    public static void loadQuarantineData(RadioButton quaRad, Slider timeB4){
        try{
            Scanner reader = new Scanner(new FileInputStream(System.getProperty("user.dir") + "/src/resources/quarantine.csv"));
            String line = reader.nextLine();
            reader.close();
            String[] tokens = line.split(",");
            boolean isOn = Boolean.parseBoolean(tokens[0]);
            quaRad.setSelected(isOn);
            if (isOn){
                timeB4.setValue(Double.parseDouble(tokens[1]));
            }
        }
        catch (IOException e){
            throwInvalidAlert("File Missing!", "file 'src/resources/quarantine.csv' does not exist");
        }
    }

    public static void setFalseCentral() throws ModeNotSetUpException{
        simulation.setEnableCentralSpot(false);

        try{
            PrintWriter writer = new PrintWriter(new FileOutputStream(System.getProperty("user.dir") + "/src/resources/central.csv", false), true);
            writer.print("false");
            writer.close();
        }
        catch (IOException e){
            throwInvalidAlert("File Missing!", "file 'src/resources/central.csv' does not exist");
        }
    }

    public static void setFalseQuarantine() throws ModeNotSetUpException{
        simulation.setEnableQuarantine(false);

        try{
            PrintWriter writer = new PrintWriter(new FileOutputStream(System.getProperty("user.dir") + "/src/resources/quarantine.csv", false), true);
            writer.print("false");
            writer.close();
        }
        catch (IOException e){
            throwInvalidAlert("File Missing!", "file 'src/resources/quarantine.csv' does not exist");
        }
    }

    public static void setPopParam(long turnInt, long turnIntVar, double radInf, double normRad){
        Person.setTurnInterval(turnInt);
        Person.setTurnIntervalVariation(turnIntVar);
        Person.setRadiusOfInfection(radInf);
        Person.setSize(normRad);

        try{
            PrintWriter writer = new PrintWriter(new FileOutputStream(System.getProperty("user.dir") + "/src/resources/popSettings.csv", false), true);
            writer.print(turnInt + "," + turnIntVar + "," + normRad + "," + radInf);
            writer.close();
        }
        catch (IOException e){
            throwInvalidAlert("File Missing!", "file 'src/resources/popSettings.csv' does not exist");
        }
    }

    public static void throwInvalidAlert(String header, String content){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.getDialogPane().getScene().getStylesheets().add("view/FlatBee.css");
        alert.setTitle(LinkedControllers.rb.getString("error"));
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static boolean throwConfirmationAlert(String header, String content){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getDialogPane().getScene().getStylesheets().add("view/FlatBee.css");
        alert.setTitle(LinkedControllers.rb.getString("sure"));
        alert.setHeaderText(header);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.get() == ButtonType.OK;
    }

    public static void updateSim(Disease disease, int pop_size, int in_size, long intervalRemoved, long intervalRemVar, double divider){
        simulation.setDisease(disease);
        simulation.setPop_size(pop_size);
        simulation.setInitial_in_size(in_size);
        simulation.setIntervalRemoved(intervalRemoved);
        simulation.setIntervalRemovedVariation(intervalRemVar);
        simulation.setSpeed(divider);

        try{
            PrintWriter writer = new PrintWriter(new FileOutputStream(System.getProperty("user.dir") + "/src/resources/simSettings.csv", false), true);
            writer.print(disease.getName() + "," + pop_size + "," + in_size + "," + intervalRemoved + "," + intervalRemVar + "," + divider);
            writer.close();
        }
        catch (IOException e){
            throwInvalidAlert("File Missing!", "file 'src/resources/simSettings.csv' does not exist");
        }
    }

    public static void setUpCentral(int max_per_time, double travel_time, double one_every, double centralProp){
        simulation.setCentralSpot(centralProp, max_per_time, travel_time, one_every);
        try{
            PrintWriter writer = new PrintWriter(new FileOutputStream(System.getProperty("user.dir") + "/src/resources/central.csv", false), true);
            writer.print("true," + max_per_time + "," + travel_time + "," + one_every + "," + centralProp);
            writer.close();
        }
        catch (IOException e){
            throwInvalidAlert("File Missing!", "file 'src/resources/central.csv' does not exist");
        }
    }

    public static void setUpQuarantine(long timeB4){
        simulation.setUpQuarantine(quarantineCanvas, timeB4);
        try{
            PrintWriter writer = new PrintWriter(new FileOutputStream(System.getProperty("user.dir") + "/src/resources/quarantine.csv", false), true);
            writer.print("true," + timeB4);
            writer.close();
        }
        catch (IOException e){
            throwInvalidAlert("File Missing!", "file 'src/resources/quarantine.csv' does not exist");
        }
    }

    public static void clearText(){
        try{
            diseases.clear();
            PrintWriter writer2 = new PrintWriter(new FileOutputStream(System.getProperty("user.dir") + "/src/resources/popSettings.csv"));
            PrintWriter writer3 = new PrintWriter(new FileOutputStream(System.getProperty("user.dir") + "/src/resources/simSettings.csv"));
            PrintWriter writer4 = new PrintWriter(new FileOutputStream(System.getProperty("user.dir") + "/src/resources/central.csv"));
            PrintWriter writer5 = new PrintWriter(new FileOutputStream(System.getProperty("user.dir") + "/src/resources/quarantine.csv"));
            writer2.print("2500,100,5,15");
            writer3.print("covid19,50,2,10,2,1.0");
            writer4.print("false");
            writer5.print("false");
            writer2.close();
            writer3.close();
            writer4.close();
            writer5.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
