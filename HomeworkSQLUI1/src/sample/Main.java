package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import sample.model.HomeworkItem;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class Main extends Application {
    @FXML
    private BorderPane mainBorderPane;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 900, 800));
        primaryStage.show();
    }


    @Override
    public void init() throws Exception {
        System.out.println("init method");


    }

    public static void main(String[] args) {
        launch(args);
    }
}
