package com.laufverwaltungfelix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DatenAnzeigenBearbeitenController {

    @FXML
    private Button bearbeitenButton, speichernButton;

    private Cell gefundenCell;
    private String filePath;
    private Row gefundeneReihe;
    private Workbook workbook;

    // Dialogfenster öffnen zum Bearbeiten von Daten
    @FXML
    protected void openEditDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Daten Bearbeiten");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ChoiceBox<String> fileChoiceBox = new ChoiceBox<>();
        List<String> excelFiles = getExcelFiles("datas");
        fileChoiceBox.getItems().addAll(excelFiles);

        TextField suchbegriffField = new TextField();
        suchbegriffField.setPromptText("Suchbegriff");

        dialogPane.setContent(new VBox(10, fileChoiceBox, suchbegriffField));

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                String selectedFile = fileChoiceBox.getValue();
                String suchbegriff = suchbegriffField.getText();
                if (selectedFile != null && suchbegriff != null && !suchbegriff.isEmpty()) {
                    try {
                        filePath = selectedFile;
                        searchAndEdit(suchbegriff);
                    } catch (IOException e) {
                        showAlert("Fehler", "Fehler beim Suchen in der Tabelle.");
                        e.printStackTrace();
                    }
                } else {
                    showAlert("Fehler", "Bitte wählen Sie eine Datei und geben Sie einen Suchbegriff ein.");
                }
            }
            return null;
        });

        dialog.setWidth(200);
        dialog.setHeight(250);
        dialog.setResizable(true); // Fenster resizable machen
        dialog.initStyle(StageStyle.UNDECORATED); // Titelleiste ausblenden
        dialog.showAndWait();
    }

    // Excel-Dateien im angegebenen Verzeichnis abrufen
    private List<String> getExcelFiles(String directoryPath) {
        List<String> excelFiles = new ArrayList<>();
        File directory = new File(directoryPath);

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    excelFiles.addAll(getExcelFiles(file.getAbsolutePath()));
                } else if (file.getName().endsWith(".xlsx")) {
                    excelFiles.add(file.getAbsolutePath());
                }
            }
        }

        return excelFiles;
    }

    // In der Excel-Datei nach dem Suchbegriff suchen und bearbeiten
    private void searchAndEdit(String suchbegriff) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);

        gefundenCell = null;

        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().contains(suchbegriff)) {
                    gefundenCell = cell;
                    gefundeneReihe = row;
                    showEditDialog(row);
                    break;
                }
            }
            if (gefundenCell != null) {
                break;
            }
        }

        fis.close();
    }

    // Dialogfenster anzeigen zum Bearbeiten der gefundenen Daten
    private void showEditDialog(Row row) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Daten Bearbeiten");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        List<TextField> fields = new ArrayList<>();
        for (Cell cell : row) {
            TextField textField = new TextField(cell.toString());
            textField.setStyle("-fx-background-color: rgba(227,212,173); -fx-text-fill: white; -fx-font-family: Arial;");
            fields.add(textField);
        }

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(fields);
        dialogPane.setContent(vbox);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                saveEditedData(fields);
            }
            return null;
        });

        dialog.setWidth(200);
        dialog.setHeight(250);
        dialog.setResizable(true); // Fenster resizable machen
        dialog.initStyle(StageStyle.UNDECORATED); // Titelleiste ausblenden
        dialog.showAndWait();
    }

    // Bearbeitete Daten in der Excel-Datei speichern
    private void saveEditedData(List<TextField> fields) {
        for (int i = 0; i < fields.size(); i++) {
            Cell cell = gefundeneReihe.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            cell.setCellValue(fields.get(i).getText());
        }

        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            workbook.write(fos);
            fos.close();
            workbook.close();
        } catch (IOException e) {
            showAlert("Fehler", "Fehler beim Speichern der bearbeiteten Daten.");
            e.printStackTrace();
        }
    }

    // Fehlermeldungen anzeigen
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
