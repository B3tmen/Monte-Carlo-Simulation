package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    public static void main(String[] args){
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(".." + File.separator + "view" + File.separator + "main.fxml"));
        try{
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Monte Carlo Simulation");
            primaryStage.getIcons().add(new Image("." + File.separator + "images" + File.separator + "pi.png"));
            primaryStage.show();

        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
