package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.MonteCarlo;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private ScatterChart<Number, Number> scatterChart;
    private TextField activeTextField = new TextField();


    @FXML
    private ComboBox comboBoxOptions;
    @FXML
    private HBox hboxInput;
    @FXML
    private VBox vboxMainContainer;
    @FXML
    private Button btnShowChart;
    @FXML
    private BorderPane borderPaneChartContainer;


    public MainController(){
    }

    public BorderPane getBorderPaneChartContainer(){
        return borderPaneChartContainer;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("POZDRAV! Aplikacija pokrenuta.");
        setupComboBox();
    }

    private void setupComboBox(){
        String optionNumPoints = "Broj slučajno generisanih vrijednosti";
        String optionDecimalLength = "Tačnost po broju decimala";

        ObservableList<String> list = FXCollections.observableArrayList(
                optionNumPoints,
                optionDecimalLength
        );
        comboBoxOptions.getItems().addAll(list);

        handleComboBoxEvents();
    }

    private void handleComboBoxEvents() {
        TextField textField1 = new TextField();     textField1.setId("textFieldNumPoints");
        TextField textField2 = new TextField();     textField2.setId("textFieldPrecision");
        textField1.setVisible(false);
        textField2.setVisible(false);

        Label label1 = new Label("Unos (broj tačaka):");
        Label label2 = new Label("Unos (broj decimala):");
        label1.setVisible(false);
        label2.setVisible(false);

        comboBoxOptions.setOnAction(event -> {
            String selectedOption = (String) comboBoxOptions.getSelectionModel().getSelectedItem();

            switch (selectedOption) {
                case "Broj slučajno generisanih vrijednosti":
                    textField1.setVisible(true);
                    label1.setVisible(true);
                    activeTextField = textField1;

                    hboxInput.getChildren().removeAll(label2, textField2);
                    hboxInput.getChildren().add(label1);
                    hboxInput.getChildren().add(textField1);
                    break;
                case "Tačnost po broju decimala":
                    textField2.setVisible(true);
                    label2.setVisible(true);
                    activeTextField = textField2;

                    hboxInput.getChildren().removeAll(label1, textField1);
                    hboxInput.getChildren().add(label2);
                    hboxInput.getChildren().add(textField2);
                    break;
            }
        });


    }

    @FXML
    public void showChart(ActionEvent actionEvent) {

        if (activeTextField.getText().isEmpty()){
            showErrorMessage("Niste unijeli podatke!");
        }
        else{
            borderPaneChartContainer.setCenter(null);
            borderPaneChartContainer.setBottom(null);
            MonteCarlo monteCarlo = null;

            if (activeTextField.getId().equals("textFieldNumPoints")){
                int numPoints = 0;
                try{
                    numPoints = Integer.parseInt(activeTextField.getText());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    showErrorMessage("Pogresan format unosa! Potreban cijeli broj.");
                }

                monteCarlo = new MonteCarlo(numPoints, true, this);
                try{
                    monteCarlo.service.start();
                } catch (Exception e){
                    e.printStackTrace();
                }

                scatterChart = monteCarlo.getScatterChart();
                borderPaneChartContainer.setCenter(scatterChart);
            }
            else{
                double decimals = 0.0;
                try{
                    decimals = Integer.parseInt(activeTextField.getText());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    showErrorMessage("Pogresan format unosa! Potreban cijeli broj.");
                }


                monteCarlo = new MonteCarlo(decimals, false, this);
                try{
                    monteCarlo.service.start();
                } catch (Exception e){
                    e.printStackTrace();
                }


                scatterChart = monteCarlo.getScatterChart();
                borderPaneChartContainer.setCenter(scatterChart);
            }
        }
    }

    private void showErrorMessage(String header){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Unos podataka");
        alert.setHeaderText(header);
        alert.setContentText("Ponovite unos");

        alert.showAndWait();
    }
}
