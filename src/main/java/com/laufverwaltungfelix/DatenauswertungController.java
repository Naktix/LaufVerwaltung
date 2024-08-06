package com.laufverwaltungfelix;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DatenauswertungController {

    @FXML
    private void openDatenAnzeigenBearbeiten() throws IOException {
        openWindow("/fxml/datenAnzeigenBearbeitenPage.fxml", "Daten Anzeigen und Bearbeiten");
    }

    @FXML
    private void openUrkundenauswertung() throws IOException {
        openWindow("/fxml/urkundenauswertungPage.fxml", "Urkundenauswertung");
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
}
