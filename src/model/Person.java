package model;

import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.Random;

public class Person extends Circle implements Comparable<Person>{

    //person's properties
    private State state;
    public static double radius = 5;
    private Pane canvas;

    //things for when the person is infected
    private static double radiusOfInfection = 15;
    private Circle ringOfInfection = new Circle(radiusOfInfection);
    private long infectedAt; //= Long.MAX_VALUE; represents the moment the person gets infected for time keeping

    //random movement things
    private final static int STEP = 1; //the steps a person takes each frame
    private static long turnInterval = 2500, turnIntervalVariation = 100; // person turns every interval with a slight variation
    private double FOV = Math.random() * 2 * Math.PI;
    private long lastFOVShift = System.currentTimeMillis();
    private double newX, newY;

    //thing for comparable interface to use to sort
    private int priority;

    //reproductive number of individual, i.e. the amount of people this person infect when they are infected
    private long R = 0;

    //quarantine things
    private boolean already_in_quarantine = false;

    //constructor
    public Person(double x, double y, Pane canvas){
        super(5);
        setTranslateX(x);
        setTranslateY(y);
        this.state = State.SUSCEPTIBLE;
        setFill(state.getColor());
        this.priority = state.getPriority();
        this.canvas = canvas;
        setRadius(radius);
    }

    //move methods
    public void move() {
        double currentX = getTranslateX();
        double currentY = getTranslateY();
        if (currentX > canvas.getWidth() || currentX < 0 || currentY > canvas.getHeight() || currentY < 0){
            setTranslateX(canvas.getWidth()/2);
            setTranslateY(canvas.getHeight()/2);
        }
        long now = System.currentTimeMillis();
        if (now - lastFOVShift >= turnInterval + (Math.random() - 0.5) * turnIntervalVariation){
            FOV = Math.random() * 2 * Math.PI;
            lastFOVShift = now;
        }
        double xStep = STEP * Math.cos(FOV);
        double yStep = STEP * Math.sin(FOV);
        newX = getTranslateX() + xStep;
        newY = getTranslateY() + yStep;
        if (newX > canvas.getWidth() - 2 * radius || newX < radius){
            xStep *= -1;
            FOV -= Math.PI;
            newX = getTranslateX() + xStep;
        }
        if (newY < radius || newY > canvas.getHeight() - 2 * radius){
            yStep *= -1;
            FOV -= Math.PI;
            newY = getTranslateY() + yStep;
        }
    }
    public void moveTo(double x, double y, double travelTime, boolean autoreverse){
        TranslateTransition translate = new TranslateTransition(Duration.seconds(travelTime), this);
        translate.setFromX(newX);
        translate.setFromY(newY);
        translate.setToX(x);
        translate.setToY(y);
        translate.setAutoReverse(autoreverse);
        if (autoreverse){
            translate.setCycleCount(2);
        }
        translate.play();
    }

    //move the person to quarantine, with a bit of logic to handle out patients
    public void moveToQuarantine(Pane newCanvas){
        if (!already_in_quarantine){
            this.setTranslateX(newCanvas.getPrefWidth()/2);
            this.setTranslateY(newCanvas.getPrefWidth()/2);
            canvas.getChildren().removeAll(this, ringOfInfection);
            newCanvas.getChildren().addAll(this, ringOfInfection);
            already_in_quarantine = true;
            canvas = newCanvas;
        }
    }

    //release patient from quarantine, the redundancy is for extra safety
    public void releasePatient(Pane newCanvas){
        if (already_in_quarantine){
            this.setTranslateY(newCanvas.getPrefWidth()/2);
            this.setTranslateX(newCanvas.getPrefWidth()/2);
            canvas.getChildren().removeAll(this, ringOfInfection);
            newCanvas.getChildren().add(this);
            already_in_quarantine = false;
            canvas = newCanvas;
        }
    }

    //update Pos done separately from the logic
    public void updatePos(){
        setTranslateX(newX);
        setTranslateY(newY);
    }

    //getters
    public Pane getCanvas() {
        return canvas;
    }
    public boolean isAlready_in_quarantine() {
        return already_in_quarantine;
    }
    public State getState() {
        return state;
    }
    public long getInfectedAt() {
        return infectedAt;
    }
    public long getR() {
        return R;
    }
    public int getPriority() {
        return priority;
    }

    //setters
    public static void setTurnInterval(long turnInterval) {
        Person.turnInterval = turnInterval;
    }
    public static void setTurnIntervalVariation(long turnIntervalVariation) {
        Person.turnIntervalVariation = turnIntervalVariation;
    }
    public static void setRadiusOfInfection(double radiusOfInfection) {
        Person.radiusOfInfection = radiusOfInfection;
    }
    public static void setSize(double radius){
        Person.radius = radius;
    }

    private void setState(State state){
        this.state = state;
        priority = state.getPriority();
        setFill(state.getColor());
    }

    public boolean setInfected(double infectiousness, Person infector, long timeOfInfection) {
        if (Math.random() < infectiousness){
            setState(State.INFECTED);
            infectedAt = timeOfInfection;
            if (infector != null){
                infector.incrementR();
            }
            ringOfInfection.setRadius(radiusOfInfection);
            ringOfInfection.setFill(null);
            ringOfInfection.setStroke(state.getColor());
            ringOfInfection.translateXProperty().bind(this.translateXProperty());
            ringOfInfection.translateYProperty().bind(this.translateYProperty());
            canvas.getChildren().add(ringOfInfection);
            return true;
        }
        return false;
    }

    public void setSusceptible() {
        setState(State.SUSCEPTIBLE);
    }
    public void setRemoved(){
        setState(State.REMOVED);
        ringOfInfection.translateXProperty().unbind();
        ringOfInfection.translateYProperty().unbind();
        canvas.getChildren().remove(ringOfInfection);
        R = 0;
    }
    //increases R by 1
    private void incrementR() {
        R++;
    }

    //check if person is inside infected radius of this person if this person is infected
    public boolean withinInfectedRadius(Person susceptible){
        if (canvas != susceptible.canvas || state != State.INFECTED || susceptible.getState() != State.SUSCEPTIBLE){
            return false;
        }
        return ringOfInfection.getBoundsInParent().intersects(susceptible.getBoundsInParent());
    }

    //overrides
    @Override
    public int compareTo(Person other){
        return priority - other.getPriority();
    }

    @Override
    public String toString(){
        return state + "";
    }
}
