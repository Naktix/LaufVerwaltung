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
        hoursField.setStyle("-fx-background-color: rgba(227,212,173); -fx-text-fill: white; -fx-font-family: Arial;");

        TextField minutesField = new TextField();
        minutesField.setPromptText("Minuten");
        minutesField.setStyle("-fx-background-color: rgba(227,212,173); -fx-text-fill: white; -fx-font-family: Arial;");

        TextField secondsField = new TextField();
        secondsField.setPromptText("Sekunden");
        secondsField.setStyle("-fx-background-color: rgba(227,212,173); -fx-text-fill: white; -fx-font-family: Arial;");

        TextField millisecondsField = new TextField();
        millisecondsField.setPromptText("Millisekunden");
        millisecondsField.setStyle("-fx-background-color: rgba(227,212,173); -fx-text-fill: white; -fx-font-family: Arial;");

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
                        if (cell != null && cell.getCellType() == CellType.NUMERIC && cell.getNumericCellValue() == newValue) {
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

        dialogPane.setContent(new VBox(10, idChoiceBox, previewText, hoursField, minutesField, secondsField, millisecondsField));

        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                try {
                    int hours = Integer.parseInt(hoursField.getText());
                    int minutes = Integer.parseInt(minutesField.getText());
                    int seconds = Integer.parseInt(secondsField.getText());
                    int milliseconds = Integer.parseInt(millisecondsField.getText());
                    long totalMilliseconds = (hours * 3600000L) + (minutes * 60000L) + (seconds * 1000L) + milliseconds;
                    saveZeit(idChoiceBox.getValue(), totalMilliseconds);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        });

        dialog.setResizable(true); // Fenster resizable machen
        /*dialog.initStyle(StageStyle.UNDECORATED); // Titelleiste ausblenden*/
        dialog.showAndWait();
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
