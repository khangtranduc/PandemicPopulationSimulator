package controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.ResourceBundle;

public class GraphsController {
    @FXML
    private Label popLabel;
    @FXML
    private Label R_label;
    @FXML
    private GridPane gridPane;

    //non-fxml components
    StackedAreaChart chart = LinkedControllers.simulation.getChart();
    ObservableList<StackedAreaChart.Series> seriesPop = chart.getData();
    LineChart lineChart = LinkedControllers.simulation.getRlineChart();
    ObservableList<StackedAreaChart.Series> seriesR = lineChart.getData();
    ResourceBundle rb = LinkedControllers.rb;

    @FXML
    public void initialize(){
        //set up graphs
        gridPane.add(chart, 0, 1);
        gridPane.add(lineChart, 1, 1);

        //set up resource bundle
        chart.getYAxis().setLabel(rb.getString("popGraphYAxis"));
        chart.getXAxis().setLabel(rb.getString("timeXAxis"));
        seriesPop.get(0).setName(rb.getString("susceptible"));
        seriesPop.get(1).setName(rb.getString("infected"));
        seriesPop.get(2).setName(rb.getString("removed"));
        lineChart.getXAxis().setLabel(rb.getString("timeXAxis"));
        lineChart.getYAxis().setLabel(rb.getString("RgraphyAxis"));
        seriesR.get(0).setName(rb.getString("Rdes"));
        popLabel.setText(rb.getString("popAgainstTime"));
        R_label.setText(rb.getString("R"));
    }
}
