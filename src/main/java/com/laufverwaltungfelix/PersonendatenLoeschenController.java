package com.laufverwaltungfelix;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PersonendatenLoeschenController {

    @FXML
    private Button loeschenButton;

    @FXML
    protected void deletePersonendaten() {
        File datenFile = new File("datas/Daten/Daten.xlsx");
        if (datenFile.exists()) {
            datenFile.delete();
        }

        File auswertungenDir = new File("datas/Auswertungen");
        if (auswertungenDir.exists()) {
            for (File file : auswertungenDir.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".txt")) {
                    file.delete();
                }
            }
        }

        File zwischenspeicherDir = new File("datas/Zwischenspeicher");
        if (zwischenspeicherDir.exists()) {
            for (File file : zwischenspeicherDir.listFiles()) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }

        System.out.println("Alle Personendaten, Auswertungen und Zwischenspeicher-Dateien wurden gelöscht.");
    }

    @SuppressWarnings("unused")
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
