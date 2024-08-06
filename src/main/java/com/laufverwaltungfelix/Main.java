package com.laufverwaltungfelix;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/mainPage.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 200, 250);
        stage.setTitle("LaufVerwaltungFelix");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.initStyle(StageStyle.UNDECORATED); // Titelleiste ausblenden
        
        // Set the application icon
        String iconPath = getClass().getResource("/IconLogo.jpg").toExternalForm();
        stage.getIcons().add(new Image(iconPath));
        
        stage.show();
    }

    @FXML
    private void openDateneingabe() throws IOException {
        openWindow("/fxml/dateneingabePage.fxml", "Dateneingabe");
    }

    @FXML
    private void openDatenauswertung() throws IOException {
        openWindow("/fxml/datenauswertungPage.fxml", "Datenauswertung");
    }

    @FXML
    private void deletePersonendaten() throws IOException {
        openWindow("/fxml/personendatenLoeschenPage.fxml", "Daten löschen");
    }

    @FXML
    private void print() throws IOException {
        openWindow("/fxml/druckenPage.fxml", "Drucken");
    }

    private void openWindow(String fxmlFile, String title) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 200, 250);
        stage.setTitle(title);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.initStyle(StageStyle.UNDECORATED); // Titelleiste ausblenden
        stage.show();
    }

    @FXML
    private Button closeButton;

    @FXML
    private void closeWindow() {
        // Holt das aktuelle Stage (Fenster) und schließt es
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    public static void main(String[] args) {
        launch();
    }
}
