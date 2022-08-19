package model;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class Simulation {
    //canvases
    private Pane canvas;
    private Pane quarantineCanvas;

    //people array
    private ArrayList<Person> people = new ArrayList<>();

    //disease properties
    private double infectiousness;

    //simulation needs
    private int in_size;
    private int initial_in_size;
    private int pop_size;
    private int sus_size;
    private int rem_size;

    //time keeping variable
    private long time = 0;
    private final long REFERENCE_SPEED = 60L;
    private long speed = 60L; //frame rate, which is also tied to the speed of the simulation
    private long duration = 1000; //the duration of a second
    private long intervalRemoved = 10, intervalRemovedMaxVariation = 2; //time before an infected person becomes removed with some variation, the variation should be even

    //effective reproductive number
    private double R = 0;
    private double iniR = 1;

    //central-spot things
    private boolean centralInPlay = false;
    private double centralProbability;
    private int max_per_time;
    private double travelTime;
    private double every_unit_of_time;
    private boolean centralSetUp = false;

    //setting up quarantine things
    private boolean quarantineSetUp = false;
    private boolean quarantineInPlay = false;
    private long intervalQuarantine, intervalQuarantineMaxVariation; //TODO: set up the max variation later
//    private int triggerPop; //TODO: set up trigger POP later

    //table
    NumberAxis xAxis;
    NumberAxis yAxis;
    StackedAreaChart.Series susceptible, infected, removed;
    StackedAreaChart chart;

    //R-line chart
    NumberAxis RxAxis;
    NumberAxis RyAxis;
    double RyMax = 1;
    LineChart RlineChart;
    LineChart.Series Rcounting;

    //constructor
    public Simulation(Pane canvas, Pane quarantineCanvas, int pop_size, int in_size, Disease disease) throws InvalidParameterException{
        this.canvas = canvas;
        this.quarantineCanvas = quarantineCanvas;
        this.in_size = in_size;
        this.initial_in_size = in_size;
        this.pop_size = pop_size;
        this.sus_size = pop_size - in_size;
        if (disease.getInfectiousness() < 0) {this.infectiousness = 0;}
        else if (disease.getInfectiousness() > 1) {this.infectiousness = 1;}
        else {this.infectiousness = disease.getInfectiousness();}

        //instantiation of table elements
        yAxis = new NumberAxis("Population (people)", 0, pop_size, (double) pop_size/10);
        xAxis = new NumberAxis("Time", 0, 5, 1);

        susceptible = new StackedAreaChart.Series("Susceptible" ,FXCollections.observableArrayList(
                new StackedAreaChart.Data(time, sus_size)
        ));
        infected = new StackedAreaChart.Series("Infected" ,FXCollections.observableArrayList(
                new StackedAreaChart.Data(time, this.in_size)
        ));
        removed = new StackedAreaChart.Series("Removed" ,FXCollections.observableArrayList(
                new StackedAreaChart.Data(time, rem_size)
        ));
        chart = new StackedAreaChart(xAxis, yAxis ,FXCollections.observableArrayList(
                susceptible,
                infected,
                removed
        ));

        //instantiation of line chart elements
        RxAxis = new NumberAxis("Time", 0, 5, 1);
        RyAxis = new NumberAxis("Effective Reproductive Number (R)", 0, iniR, 0.1);
        RyAxis.setTickUnit(0.2);
        Rcounting = new LineChart.Series("Effective Reproductive Number", FXCollections.observableArrayList(
                new LineChart.Data(time, R)
        ));
        RlineChart = new LineChart(RxAxis, RyAxis, FXCollections.observableArrayList(
                Rcounting
        ));

        //generating population
        if (pop_size < in_size){
            throw new InvalidParameterException();
        }

        for (int i = 0; i < pop_size; i++){
            generatePerson(canvas.getPrefWidth(), canvas.getPrefHeight());
        }
        for (int i = 0; i < in_size; i++){
            people.get(i).setInfected(1, null, time);
        }
        canvas.getChildren().addAll(people);
    }

    //generate a person with a randomly assigned position in the world
    private void generatePerson(double boundX, double boundY){
        double x = Person.radius + Math.random() * (boundX - 2 * Person.radius);
        double y = Person.radius + Math.random() * (boundY - 2 * Person.radius);
        people.add(new Person(x, y, canvas));
    }

    //move everyone in the simulation by one frame
    public void move(){
        for (Person i : people){
            i.move();
        }
    }

    //always called after move()
    //update the position of everyone in the simulation in jfx
    //calculates the effective reproductive number
    public void updatePos(){
        for (Person i : people){
            i.updatePos();
        }
    }

    //check for collisions
    public void updateCollisions(){
        Collections.sort(people);
        for (int q = 0; q < in_size; q++){
            Person i = people.get(q);
            for (Person k : people){
                if (i.withinInfectedRadius(k) && k.setInfected(infectiousness, i, time)){
                    in_size++;
                    sus_size--;
//                    if (in_size >= triggerPop && quarantineSetUp){
//                        quarantineInPlay = true;
//                    }
                }
            }
        }
    }

    //check every second to see if anyone should be removed
    public void updateRemove(){
        R = 0;
        Collections.sort(people);
        for (int k = 0; k < in_size; k++){
            Person i = people.get(k);
            R += i.getR();
            long timeInfected = time - i.getInfectedAt();
            if (timeInfected >= intervalRemoved + (Math.random() - 0.5) * 2 * intervalRemovedMaxVariation){
                i.setRemoved();
                in_size--;
                rem_size++;
                if (i.isAlready_in_quarantine()){
                    i.releasePatient(canvas);
                }
            }
        }
        if (in_size != 0){
            R /= in_size;
        }
    }

    //method that keeps time and should be called every second
    public void updateTime(){
        time++;
        updateGraph();
    }

    //method that updates the central spot if it is turned on
    public void updateCentral(){
        int curr_ppl = 0;
        if (centralInPlay){
            for (Person i : people){
                if (curr_ppl <= max_per_time && !i.isAlready_in_quarantine() && time % every_unit_of_time == 0 && Math.random() < centralProbability){
                    curr_ppl++;
                    i.moveTo(canvas.getWidth()/2, canvas.getHeight()/2, travelTime, true);
                }
            }
        }
    }

    //set up the central spot for the first time
    public void setCentralSpot(double centralProbability, int max_per_time, double travelTime, double every_unit_of_time){
        this.centralInPlay = true;
        centralSetUp = true;
        this.max_per_time = max_per_time;
        this.travelTime = travelTime;
        this.every_unit_of_time = every_unit_of_time;
        if (centralProbability < 0) {this.centralProbability = 0;}
        else if (centralProbability > 1) {this.centralProbability = 1;}
        else {this.centralProbability = centralProbability;}
    }

    //used to toggle the central spot on and off if it had already been set up
    public void setEnableCentralSpot(boolean isOn) throws ModeNotSetUpException{
        if (centralSetUp){
            centralInPlay = isOn;
        }
        else{
            throw new ModeNotSetUpException("Central Spot not Enabled");
        }
    }

    //update the stacked graph
    private void updateGraph(){
        if (time == xAxis.getUpperBound()){
            xAxis.setUpperBound(time + 1);
            RxAxis.setUpperBound(time + 1);
            if (xAxis.getUpperBound() % 10 == 0){
                //TODO: min bound of the tick unit is 5
                xAxis.setTickUnit(xAxis.getUpperBound()/5);
                RxAxis.setTickUnit(RxAxis.getUpperBound()/5);
            }
        }

        susceptible.getData().add(new StackedAreaChart.Data(time, sus_size));
        infected.getData().add(new StackedAreaChart.Data(time, in_size));
        removed.getData().add(new StackedAreaChart.Data(time, rem_size));

        //updating the R chart
        if (R > RyAxis.getUpperBound()){
            if (R > RyAxis.getUpperBound() * 3){
                R = R - Math.floor(R) + RyAxis.getUpperBound() + 0.5;
            }
            RyAxis.setUpperBound(R);
            RyAxis.setTickUnit(RyAxis.getUpperBound()/5);
        }
        Rcounting.getData().add(new LineChart.Data(time, R));
    }

    //set up the quarantine for the first time
    public void setUpQuarantine(Pane quarantineCanvas, long intervalQuarantine){
        this.quarantineSetUp = true;
        this.quarantineInPlay = true;
        this.quarantineCanvas = quarantineCanvas;
//        this.triggerPop = triggerPop;
        this.intervalQuarantine = intervalQuarantine;
    }

    //updates quarantine
    public void updateQuarantine(){
        Collections.sort(people);
        if (quarantineInPlay){
            for (int i = 0; i < in_size; i++){
                long timeInfected = time - people.get(i).getInfectedAt();
                if (timeInfected == intervalQuarantine){
                    people.get(i).moveToQuarantine(quarantineCanvas);
                }
            }
        }
    }

    //toggles quarantine
    public void setEnableQuarantine(boolean isOn) throws ModeNotSetUpException{
        if (quarantineSetUp){
            quarantineInPlay = isOn;
        }
        else{
            throw new ModeNotSetUpException("Quarantine not Enabled");
        }
    }

    //reset the simulation
    public void resetSim(){
        people.clear();
        canvas.getChildren().clear();
        quarantineCanvas.getChildren().clear();
        in_size = initial_in_size;
        sus_size = pop_size - in_size;
        rem_size = 0;
        time = 0;
        R = 0;
        susceptible.getData().clear();
        infected.getData().clear();
        removed.getData().clear();
        Rcounting.getData().clear();
        xAxis.setTickUnit(1);
        xAxis.setUpperBound(5);
        yAxis.setUpperBound(pop_size);
        RxAxis.setTickUnit(1);
        RxAxis.setUpperBound(5);
        RyAxis.setUpperBound(iniR);
        RyAxis.setTickUnit(iniR/5);
        updateGraph();
        for (int i = 0; i < pop_size; i++){
            //TODO: maybe change the preferredness out later
            generatePerson(canvas.getPrefWidth(), canvas.getPrefHeight());
        }
        for (int i = 0; i < in_size; i++){
            people.get(i).setInfected(1, null, time);
        }
        canvas.getChildren().addAll(people);
    }

    //getters
    public boolean isQuarantineInPlay() {
        return quarantineInPlay;
    }
    public int getSus_size() {
        return sus_size;
    }
    public long getDuration(){
        return duration/speed;
    }
    public long getSpeed() {
        return speed;
    }
    public int getIn_size() {
        return in_size;
    }
    public int getRem_size() {
        return rem_size;
    }
    public boolean isCentralInPlay() {
        return centralInPlay;
    }
    public double getR() {
        return R;
    }
    public LineChart getRlineChart() {
        return RlineChart;
    }
    public StackedAreaChart getChart() {
        return chart;
    }
//    public int getPop_size() {
//        return pop_size;
//    }
//    public Person getPerson (int index){
//        return people.get(index);
//    }

    //setters
    public void setDisease(Disease disease){
        infectiousness = disease.getInfectiousness();
    }
    public void setSpeed(double divider) {
        this.speed = (long) (REFERENCE_SPEED * divider);
    }
    public void setIntervalRemoved(long intervalRemoved) {
        this.intervalRemoved = intervalRemoved;
    }
    public void setIntervalRemovedVariation(long intervalRemovedVariation) {
        this.intervalRemovedMaxVariation = intervalRemovedVariation;
    }
    public void setPop_size(int pop_size) {
        this.pop_size = pop_size;
    }
    public void setInitial_in_size(int initial_in_size) {
        this.initial_in_size = initial_in_size;
    }

    //call stopper method in controller if sim is over
    public boolean isOver(){
        return in_size == 0;
    }
}
