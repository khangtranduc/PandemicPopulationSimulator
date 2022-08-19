package model;

import javafx.scene.paint.Color;

public enum State {
    SUSCEPTIBLE (Color.TURQUOISE, 1),
    INFECTED (Color.RED, 0),
    REMOVED (Color.DARKGREY, 2);

    private Color color;
    private int priority;

    State (Color color, int priority){
        this.color = color;
        this.priority = priority;
    }

    public Color getColor() {
        return this.color;
    }

    public int getPriority(){
        return this.priority;
    }
}
