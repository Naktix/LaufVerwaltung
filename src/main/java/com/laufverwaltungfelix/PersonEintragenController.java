package com.laufverwaltungfelix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Year;
import java.util.List;

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
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PersonEintragenController {

    @FXML
    private Button speichernButton;

    private static int getMaxId(Sheet sheet) {
        int maxId = 0;
        for (Row row : sheet) {
            if (row.getRowNum() == 0)
                continue; // Skip header row if present
            // Check if the cell contains a numeric or string value
            if (row.getCell(0).getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                int id = (int) row.getCell(0).getNumericCellValue();
                if (id > maxId) {
                    maxId = id;
                }
            } else if (row.getCell(0).getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                try {
                    // Try to convert the String value to an integer
                    int id = Integer.parseInt(row.getCell(0).getStringCellValue());
                    if (id > maxId) {
                        maxId = id;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Cannot convert value to integer in row: " + row.getRowNum());
                }
            } else {
                System.out.println("Unexpected cell type in row: " + row.getRowNum());
            }
        }
        return maxId;
    }

    @FXML
    protected void openSaveDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Person Eintragen");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextField vornameField = new TextField();
        vornameField.setPromptText("Vorname");

        TextField dienstgradField = new TextField();
        dienstgradField.setPromptText("Dienstgrad");

        TextField ausweisnummerField = new TextField();
        ausweisnummerField.setPromptText("Ausweisnummer");

        ChoiceBox<String> geschlechtChoiceBox = new ChoiceBox<>();
        geschlechtChoiceBox.getItems().addAll("Maenlich", "Weiblich");

        int currentYear = Year.now().getValue();
        Spinner<Integer> jahrgangSpinner = new Spinner<>();
        jahrgangSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(currentYear - 100, currentYear, currentYear));

        ChoiceBox<String> vereinChoiceBox = new ChoiceBox<>();
        ChoiceBox<String> mannschaftChoiceBox = new ChoiceBox<>();
        ChoiceBox<String> laufstreckeChoiceBox = new ChoiceBox<>();

        try {
            List<String> vereine = ExcelUtils.getColumnValues("datas/Vereine/Vereine.xlsx", 0);
            vereinChoiceBox.getItems().addAll(vereine);

            List<String> mannschaften = ExcelUtils.getColumnValues("datas/Mannschaften/Mannschaften.xlsx", 0);
            mannschaftChoiceBox.getItems().addAll(mannschaften);

        } catch (IOException e) {
            e.printStackTrace();
        }

        laufstreckeChoiceBox.getItems().addAll("3km", "7,5km", "10km");

        dialogPane.setContent(new VBox(10, nameField, vornameField, dienstgradField, ausweisnummerField,
                geschlechtChoiceBox, jahrgangSpinner, vereinChoiceBox, mannschaftChoiceBox, laufstreckeChoiceBox));

        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                try {
                    double laufstrecke = convertLaufstrecke(laufstreckeChoiceBox.getValue());
                    savePerson(
                            handleEmpty(nameField.getText()),
                            handleEmpty(vornameField.getText()),
                            handleEmpty(dienstgradField.getText()),
                            handleEmpty(ausweisnummerField.getText()),
                            jahrgangSpinner.getValue(),
                            handleEmpty(geschlechtChoiceBox.getValue()),
                            handleEmpty(vereinChoiceBox.getValue()),
                            handleEmpty(mannschaftChoiceBox.getValue()),
                            laufstrecke);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        });

        dialog.setWidth(200);
        dialog.setHeight(250);
        dialog.setResizable(true);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.showAndWait();
    }

    private String handleEmpty(String value) {
        return (value == null || value.trim().isEmpty()) ? "*" : value;
    }

    private double convertLaufstrecke(String laufstrecke) {
        switch (laufstrecke) {
            case "3km":
                return 3;
            case "7,5km":
                return 7.5;
            case "10km":
                return 10;
            default:
                return 0;
        }
    }

    private void savePerson(String name, String vorname, String dienstgrad, String ausweisnummer, int jahrgang,
            String geschlecht, String verein, String mannschaft, double laufstrecke) throws IOException {
        String filePath = "datas/Daten/Daten.xlsx";

        File file = new File(filePath);
        Workbook workbook;
        Sheet sheet;

        if (!file.exists()) {
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Daten");
        } else {
            FileInputStream fis = new FileInputStream(file);
            workbook = new XSSFWorkbook(fis);
            sheet = workbook.getSheetAt(0);
            fis.close();
        }

        int maxId = getMaxId(sheet);
        int newId = maxId + 1;

        int rowCount = sheet.getLastRowNum();
        Row row = sheet.createRow(++rowCount);

        int currentYear = Year.now().getValue();
        int age = currentYear - jahrgang;
        String altersklasse = determineAltersklasse(geschlecht, age);

        row.createCell(0).setCellValue(newId);
        row.createCell(1).setCellValue(name);
        row.createCell(2).setCellValue(vorname);
        row.createCell(3).setCellValue(dienstgrad);
        row.createCell(4).setCellValue(ausweisnummer);
        row.createCell(5).setCellValue(jahrgang);
        row.createCell(6).setCellValue(geschlecht);
        row.createCell(7).setCellValue(verein);
        row.createCell(8).setCellValue(mannschaft);
        row.createCell(9).setCellValue(laufstrecke);
        row.createCell(10).setCellValue(altersklasse);
        row.createCell(11).setCellValue("*"); // Always fill column 11 with "*"

        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.close();
        workbook.close();
    }

    private String determineAltersklasse(String geschlecht, int age) {
        if (geschlecht.equals("Maenlich")) {
            if (age <= 30)
                return "M";
            if (age <= 40)
                return "M30";
            if (age <= 45)
                return "M40";
            if (age <= 50)
                return "M45";
            if (age <= 55)
                return "M50";
            if (age <= 60)
                return "M55";
            return "M60";
        } else {
            if (age <= 30)
                return "W";
            if (age <= 40)
                return "W30";
            if (age <= 45)
                return "W40";
            if (age <= 50)
                return "W45";
            if (age <= 55)
                return "W50";
            if (age <= 60)
                return "W55";
            return "W60";
        }
    }

    @FXML
    private Button closeButton;

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
