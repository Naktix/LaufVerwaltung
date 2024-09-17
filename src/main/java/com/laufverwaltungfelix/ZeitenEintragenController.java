package com.laufverwaltungfelix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ZeitenEintragenController {

    @FXML
    private Button speichernButton;

    @FXML
    protected void openSaveDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Zeit Eintragen");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ChoiceBox<Integer> idChoiceBox = new ChoiceBox<>();
        Text previewText = new Text();
        TextField hoursField = new TextField();
        hoursField.setPromptText("Stunden");

        TextField minutesField = new TextField();
        minutesField.setPromptText("Minuten");

        TextField secondsField = new TextField();
        secondsField.setPromptText("Sekunden");

        TextField millisecondsField = new TextField();
        millisecondsField.setPromptText("Millisekunden");

        try {
            String filePath = "datas/Daten/Daten.xlsx";
            FileInputStream fis = new FileInputStream(new File(filePath));
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                Cell cell = row.getCell(0);
                if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                    idChoiceBox.getItems().add((int) cell.getNumericCellValue());
                }
            }
            workbook.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        idChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                try {
                    String filePath = "datas/Daten/Daten.xlsx";
                    FileInputStream fis = new FileInputStream(new File(filePath));
                    Workbook workbook = new XSSFWorkbook(fis);
                    Sheet sheet = workbook.getSheetAt(0);

                    for (Row row : sheet) {
                        Cell cell = row.getCell(0);
                        if (cell != null && cell.getCellType() == CellType.NUMERIC
                                && cell.getNumericCellValue() == newValue) {
                            StringBuilder sb = new StringBuilder();
                            for (Iterator<Cell> cellIterator = row.cellIterator(); cellIterator.hasNext();) {
                                Cell nextCell = cellIterator.next();
                                sb.append(nextCell.toString()).append("\t");
                            }
                            previewText.setText(sb.toString());
                            break;
                        }
                    }
                    workbook.close();
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        dialogPane.setContent(
                new VBox(10, idChoiceBox, previewText, hoursField, minutesField, secondsField, millisecondsField));

        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                try {
                    int hours = parseOrDefault(hoursField.getText(), 0);
                    int minutes = parseOrDefault(minutesField.getText(), 0);
                    int seconds = parseOrDefault(secondsField.getText(), 0);
                    int milliseconds = parseOrDefault(millisecondsField.getText(), 0);
                    long totalMilliseconds = (hours * 3600000L) + (minutes * 60000L) + (seconds * 1000L) + milliseconds;
                    saveZeit(idChoiceBox.getValue(), totalMilliseconds);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        });

        dialog.setResizable(true); // Fenster resizable machen
        /* dialog.initStyle(StageStyle.UNDECORATED); // Titelleiste ausblenden */
        dialog.showAndWait();
    }

    // Hilfsfunktion zum Parsen von Text oder Rückgabe von Standardwert
    private int parseOrDefault(String text, int defaultValue) {
        if (text == null || text.trim().isEmpty()) {
            return defaultValue; // Wenn Eingabe leer ist, Standardwert verwenden
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return defaultValue; // Bei ungültiger Eingabe Standardwert verwenden
        }
    }

    private void saveZeit(int id, long zeit) throws IOException {
        String filePath = "datas/Daten/Daten.xlsx";

        FileInputStream fis = new FileInputStream(new File(filePath));
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            Cell cell = row.getCell(0);
            if (cell != null && cell.getCellType() == CellType.NUMERIC && cell.getNumericCellValue() == id) {
                row.createCell(11).setCellValue(zeit);
                break;
            }
        }

        fis.close();

        FileOutputStream fos = new FileOutputStream(new File(filePath));
        workbook.write(fos);
        fos.close();
        workbook.close();
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
