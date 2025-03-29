package model;

import controllers.MainController;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MonteCarlo {
    public static final Integer RADIUS = 1;

    private int pointsInsideCircle;
    private int numPoints;
    private double precision;
    private final int decimalPlaces;
    private final boolean isNumPointsVariant;

    private ScatterChart<Number, Number> scatterChart;
    private MainController controller;

    public MonteCarloService service;

    public MonteCarlo(double value, boolean isNumPointsVariant, MainController controller){
        this.isNumPointsVariant = isNumPointsVariant;
        this.controller = controller;

        if(isNumPointsVariant){
            this.numPoints = (int) value;
            this.decimalPlaces = 0;
        }
        else{
            this.numPoints = 0;
            this.decimalPlaces = (int) value;
            this.precision = Math.pow(10, -decimalPlaces);
        }

        final NumberAxis xAxis = new NumberAxis(-RADIUS, RADIUS, 0.1);
        final NumberAxis yAxis = new NumberAxis(-RADIUS, RADIUS, 0.1);
        scatterChart = new ScatterChart<>(xAxis, yAxis);

        service = new MonteCarloService(this);
    }

    public int getNumPoints() {
        return numPoints;
    }
    public void setNumPoints(int numPoints) {
        this.numPoints = numPoints;
    }

    public double getPrecision() {
        return precision;
    }

    public boolean isNumPointsVariant() {
        return isNumPointsVariant;
    }

    public int getPointsInsideCircle() {
        return pointsInsideCircle;
    }
    public void setPointsInsideCircle(int pointsInsideCircle) {
        this.pointsInsideCircle = pointsInsideCircle;
    }

    public MainController getController() {
        return controller;
    }

    public ScatterChart<Number, Number> getScatterChart(){
        return scatterChart;
    }

    public double calculatePI(){
        double pi = 4.0 * pointsInsideCircle / numPoints;

        if(decimalPlaces != 0){
            BigDecimal bd = new BigDecimal(pi);
            bd = bd.setScale(decimalPlaces, RoundingMode.HALF_UP);

            return bd.doubleValue();
        }
        else{
            return pi;
        }
    }
}
