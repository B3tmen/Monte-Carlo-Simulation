package model;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static model.MonteCarlo.RADIUS;

public class MonteCarloService extends Service<Void> {
    private static final int NUM_THREADS = 4;
    private static final int BATCH_SIZE = 1000;

    private Thread[] threads = new Thread[NUM_THREADS];
    private MonteCarlo mc;

    private XYChart.Series<Number, Number> seriesInside;
    private XYChart.Series<Number, Number> seriesOutside;

    public MonteCarloService(MonteCarlo mc) {
        this.mc = mc;

        seriesInside = new XYChart.Series<>();
        seriesInside.setName("Unutar kruga (Zeleno)");
        seriesOutside = new XYChart.Series<>();
        seriesOutside.setName("Izvan kruga (Crveno)");
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if(mc.isNumPointsVariant()){
                    calculateChartNumPoints(new Random());
                }
                else{
                    calculateChartPrecision(new Random());
                }

                return null;
            }
        };
    }

    @Override
    protected void succeeded() {
        mc.getScatterChart().getData().addAll(seriesInside, seriesOutside);
        mc.getScatterChart().setLegendVisible(true);
        Label label = new Label();
        label.setText("Aproksimacija broja PI: " + mc.calculatePI() + ", broj tačaka (ukupno): " + mc.getNumPoints() +
                ", broj tačaka unutar kruga: " + mc.getPointsInsideCircle());
        System.out.println("PI: " + mc.calculatePI());

        mc.getController().getBorderPaneChartContainer().setBottom(label);
    }

    //Ova metoda sada koristi niti za paralelno racunanje
    private void calculateChartNumPoints(Random rand) {
        int totalPoints = mc.getNumPoints();
        AtomicInteger pointsInsideCircle = new AtomicInteger(0);

        int pointsPerThread = totalPoints / NUM_THREADS;

        for (int i = 0; i < NUM_THREADS; i++) {
            final int dummyI = i;
            threads[i] = new Thread(() -> {
                Thread.currentThread().setName("Calculation-Thread_" + (dummyI + 1));

                int localPointsInsideCircle = 0;
                List<XYChart.Data<Number, Number>> insidePointsBatch = new ArrayList<>(BATCH_SIZE);
                List<XYChart.Data<Number, Number>> outsidePointsBatch = new ArrayList<>(BATCH_SIZE);
                for (int j = 0; j < pointsPerThread; j++) {
                    double x = rand.nextDouble() * 2 * RADIUS - RADIUS;
                    double y = rand.nextDouble() * 2 * RADIUS - RADIUS;

                    boolean isInsideCircle = Math.pow(x, 2) + Math.pow(y, 2) <= Math.pow(RADIUS, 2);

                    XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(x, y);
                    dataPoint.setNode(new Circle(0.5, isInsideCircle ? Color.GREEN : Color.RED));

                    if (isInsideCircle) {
                        localPointsInsideCircle++;
                        insidePointsBatch.add(dataPoint);
                    } else {
                        outsidePointsBatch.add(dataPoint);
                    }

                    // Periodically update the UI with batches of points
                    if (insidePointsBatch.size() >= BATCH_SIZE || outsidePointsBatch.size() >= BATCH_SIZE) {
                        List<XYChart.Data<Number, Number>> finalInsidePointsBatch = new ArrayList<>(insidePointsBatch);
                        List<XYChart.Data<Number, Number>> finalOutsidePointsBatch = new ArrayList<>(outsidePointsBatch);

                        Platform.runLater(() -> {
                            seriesInside.getData().addAll(finalInsidePointsBatch);
                            seriesOutside.getData().addAll(finalOutsidePointsBatch);
                        });

                        insidePointsBatch.clear();
                        outsidePointsBatch.clear();
                    }
                }

                // Add remaining points to the UI
                if (!insidePointsBatch.isEmpty() || !outsidePointsBatch.isEmpty()) {
                    Platform.runLater(() -> {
                        seriesInside.getData().addAll(insidePointsBatch);
                        seriesOutside.getData().addAll(outsidePointsBatch);
                    });
                }

                pointsInsideCircle.addAndGet(localPointsInsideCircle);
            });
        }

        long startTime = System.nanoTime();

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to finish
        for (Thread thread : threads) {
            try {
                thread.join();
                System.out.println("Kraj kalkulacije, " + thread.getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long endTime = System.nanoTime();                       // End of measuring time
        long duration = endTime - startTime;                    // Time in ns
        double durationInSeconds = duration / 1_000_000_000.0;  // Changing to seconds

        // Postavi broj tačaka unutar kruga
        mc.setPointsInsideCircle(pointsInsideCircle.get());

        System.out.println("------");
        System.out.println("Vrijeme trajanja kalkulacije: " + durationInSeconds + " sekundi");
        System.out.println("Aproksimacija broja PI: " + mc.calculatePI() + ", broj tačaka (ukupno): " + mc.getNumPoints() +
                ", broj tačaka unutar kruga: " + mc.getPointsInsideCircle());
    }

    private void calculateChartPrecision(Random rand) {
        AtomicInteger pointsInsideCircle = new AtomicInteger(0);
        AtomicInteger numPoints = new AtomicInteger(0);
        AtomicReference<Double> pi = new AtomicReference<>(0.0);
        AtomicBoolean precisionAchieved = new AtomicBoolean(false);

        Thread[] threads = new Thread[NUM_THREADS];

        long startTime = System.nanoTime(); // Start of measuring time

        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new Thread(() -> {
                List<XYChart.Data<Number, Number>> insidePointsBatch = new ArrayList<>(BATCH_SIZE);
                List<XYChart.Data<Number, Number>> outsidePointsBatch = new ArrayList<>(BATCH_SIZE);

                while (!precisionAchieved.get()) {
                    double x = rand.nextDouble() * 2 * RADIUS - RADIUS;
                    double y = rand.nextDouble() * 2 * RADIUS - RADIUS;

                    boolean isInsideCircle = Math.pow(x, 2) + Math.pow(y, 2) <= Math.pow(RADIUS, 2);

                    XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(x, y);
                    dataPoint.setNode(new Circle(0.5, isInsideCircle ? Color.GREEN : Color.RED));

                    if (isInsideCircle) {
                        insidePointsBatch.add(dataPoint);
                    } else {
                        outsidePointsBatch.add(dataPoint);
                    }

                    if (insidePointsBatch.size() >= BATCH_SIZE || outsidePointsBatch.size() >= BATCH_SIZE) {
                        Platform.runLater(() -> {
                            seriesInside.getData().addAll(insidePointsBatch);
                            seriesOutside.getData().addAll(outsidePointsBatch);
                        });

                        insidePointsBatch.clear();
                        outsidePointsBatch.clear();
                    }

                    int currentPointsInsideCircle = isInsideCircle ? pointsInsideCircle.incrementAndGet() : pointsInsideCircle.get();
                    int currentNumPoints = numPoints.incrementAndGet();
                    double currentPi = 4.0 * currentPointsInsideCircle / currentNumPoints;
                    pi.set(currentPi);

                    if (Math.abs(Math.PI - currentPi) <= mc.getPrecision()) {
                        precisionAchieved.set(true);
                    }
                }

                // Add remaining points to the UI
                if (!insidePointsBatch.isEmpty() || !outsidePointsBatch.isEmpty()) {
                    Platform.runLater(() -> {
                        seriesInside.getData().addAll(insidePointsBatch);
                        seriesOutside.getData().addAll(outsidePointsBatch);
                    });
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        double durationInSeconds = duration / 1_000_000_000.0;

        mc.setPointsInsideCircle(pointsInsideCircle.get());
        mc.setNumPoints(numPoints.get());

        System.out.println("Vrijeme trajanja kalkulacije: " + durationInSeconds + " sekundi");
        System.out.println("Aproksimacija broja PI: " + mc.calculatePI() + ", broj tačaka (ukupno): " + mc.getNumPoints() +
                ", broj tačaka unutar kruga: " + mc.getPointsInsideCircle());
    }
}
