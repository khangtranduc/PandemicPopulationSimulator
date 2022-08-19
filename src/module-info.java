module PandemicPopulationSimulat0r {
    requires javafx.controls;
    requires javafx.fxml;

    opens test;
    opens view;
    opens controller;
    opens model to javafx.base;
}