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

    private static int idCounter = 1;

    @FXML
    private Button speichernButton;

    @FXML
    protected void openSaveDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Person Eintragen");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        nameField.setStyle("-fx-background-color: rgba(227,212,173); -fx-text-fill: white; -fx-font-family: Arial;");

        TextField vornameField = new TextField();
        vornameField.setPromptText("Vorname");
        vornameField.setStyle("-fx-background-color: rgba(227,212,173); -fx-text-fill: white; -fx-font-family: Arial;");

        TextField dienstgradField = new TextField();
        dienstgradField.setPromptText("Dienstgrad");
        dienstgradField.setStyle("-fx-background-color: rgba(227,212,173); -fx-text-fill: white; -fx-font-family: Arial;");

        TextField ausweisnummerField = new TextField();
        ausweisnummerField.setPromptText("Ausweisnummer");
        ausweisnummerField.setStyle("-fx-background-color: rgba(227,212,173); -fx-text-fill: white; -fx-font-family: Arial;");

        ChoiceBox<String> geschlechtChoiceBox = new ChoiceBox<>();
        geschlechtChoiceBox.getItems().addAll("Maenlich", "Weiblich");
        geschlechtChoiceBox.setStyle("-fx-background-color: rgba(227,212,173); -fx-font-family: Arial;");

        int currentYear = Year.now().getValue();
        Spinner<Integer> jahrgangSpinner = new Spinner<>();
        jahrgangSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(currentYear - 100, currentYear, currentYear));

        ChoiceBox<String> vereinChoiceBox = new ChoiceBox<>();
        ChoiceBox<String> mannschaftChoiceBox = new ChoiceBox<>();
        ChoiceBox<String> laufstreckeChoiceBox = new ChoiceBox<>();

        try {
            List<String> vereine = ExcelUtils.getColumnValues("datas/Vereine/Vereine.xlsx", 0);
            vereinChoiceBox.getItems().addAll(vereine);
            vereinChoiceBox.setStyle("-fx-background-color: rgba(227,212,173); -fx-font-family: Arial;");

            List<String> mannschaften = ExcelUtils.getColumnValues("datas/Mannschaften/Mannschaften.xlsx", 0);
            mannschaftChoiceBox.getItems().addAll(mannschaften);
            mannschaftChoiceBox.setStyle("-fx-background-color: rgba(227,212,173); -fx-font-family: Arial;");
        } catch (IOException e) {
            e.printStackTrace();
        }

        laufstreckeChoiceBox.getItems().addAll("3km", "7,5km", "10km");
        laufstreckeChoiceBox.setStyle("-fx-background-color: rgba(227,212,173); -fx-font-family: Arial;");

        dialogPane.setContent(new VBox(10, nameField, vornameField, dienstgradField, ausweisnummerField, geschlechtChoiceBox, jahrgangSpinner, vereinChoiceBox, mannschaftChoiceBox, laufstreckeChoiceBox));

        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                try {
                    double laufstrecke = convertLaufstrecke(laufstreckeChoiceBox.getValue());
                    savePerson(nameField.getText(), vornameField.getText(), dienstgradField.getText(), ausweisnummerField.getText(), jahrgangSpinner.getValue(), geschlechtChoiceBox.getValue(), vereinChoiceBox.getValue(), mannschaftChoiceBox.getValue(), laufstrecke);
                } catch (IOException e) {
                    e.printStackTrace();
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

    private void savePerson(String name, String vorname, String dienstgrad, String ausweisnummer, int jahrgang, String geschlecht, String verein, String mannschaft, double laufstrecke) throws IOException {
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

        int rowCount = sheet.getLastRowNum();
        Row row = sheet.createRow(++rowCount);

        int currentYear = Year.now().getValue();
        int age = currentYear - jahrgang;
        String altersklasse = determineAltersklasse(geschlecht, age);

        row.createCell(0).setCellValue(idCounter++);
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

        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.close();
        workbook.close();
    }

    private String determineAltersklasse(String geschlecht, int age) {
        if (geschlecht.equals("Maenlich")) {
            if (age <= 30) return "M";
            if (age <= 40) return "M30";
            if (age <= 45) return "M40";
            if (age <= 50) return "M45";
            if (age <= 55) return "M50";
            if (age <= 60) return "M55";
            return "M60";
        } else {
            if (age <= 30) return "W";
            if (age <= 40) return "W30";
            if (age <= 45) return "W40";
            if (age <= 50) return "W45";
            if (age <= 55) return "W50";
            if (age <= 60) return "W55";
            return "W60";
        }
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
