package com.laufverwaltungfelix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class VereinEintragenController {

    @FXML
    private TextField vereinNameField;

    @FXML
    private Button speichernButton;

    @FXML
    protected void saveVereinAction() {
        String vereinName = vereinNameField.getText();
        try {
            if (vereinName.isEmpty()) {
                showAlert("Fehler", "Der Verein Name darf nicht leer sein.");
            } else if (isDuplicate("datas/Vereine/Vereine.xlsx", vereinName)) {
                showAlert("Fehler", "Der Verein existiert bereits.");
            } else {
                saveVerein(vereinName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isDuplicate(String filePath, String value) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }

        FileInputStream fis = new FileInputStream(file);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            Cell cell = row.getCell(0);
            if (cell != null && cell.getStringCellValue().equalsIgnoreCase(value)) {
                workbook.close();
                fis.close();
                return true;
            }
        }

        workbook.close();
        fis.close();
        return false;
    }

    private void saveVerein(String vereinName) throws IOException {
        String filePath = "datas/Vereine/Vereine.xlsx";

        File file = new File(filePath);
        Workbook workbook;
        Sheet sheet;

        if (!file.exists()) {
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Vereine");
        } else {
            FileInputStream fis = new FileInputStream(file);
            workbook = new XSSFWorkbook(fis);
            sheet = workbook.getSheetAt(0);
            fis.close();
        }

        int rowCount = sheet.getLastRowNum();
        Row row = sheet.createRow(++rowCount);
        Cell cell = row.createCell(0);
        cell.setCellValue(vereinName);

        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.close();
        workbook.close();
    }

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
