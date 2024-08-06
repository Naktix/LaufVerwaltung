package com.laufverwaltungfelix;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class UrkundenauswertungController {

    @FXML
    private void openUrkundenauswertung() {
        Stage stage = new Stage();
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);

        Button gesamtauswertungButton = new Button("Gesamtauswertung");
        gesamtauswertungButton.setStyle("-fx-background-color: rgba(227,212,173); -fx-text-fill: white; -fx-font-family: Arial;");
        gesamtauswertungButton.setOnAction(e -> openGesamtauswertung());

        Button mannschaftsauswertungButton = new Button("Mannschaftsauswertung");
        mannschaftsauswertungButton.setStyle("-fx-background-color: rgba(227,212,173); -fx-text-fill: white; -fx-font-family: Arial;");
        mannschaftsauswertungButton.setOnAction(e -> openMannschaftsauswertung());

        Button altersklassenauswertungButton = new Button("Altersklassenauswertung");
        altersklassenauswertungButton.setStyle("-fx-background-color: rgba(227,212,173); -fx-text-fill: white; -fx-font-family: Arial;");
        altersklassenauswertungButton.setOnAction(e -> openAltersklassenauswertung());

        Button meldungsauswertungButton = new Button("Meldungsauswertung");
        meldungsauswertungButton.setStyle("-fx-background-color: rgba(227,212,173); -fx-text-fill: white; -fx-font-family: Arial;");
        meldungsauswertungButton.setOnAction(e -> openMeldungsauswertung());

        vbox.getChildren().addAll(gesamtauswertungButton, mannschaftsauswertungButton, altersklassenauswertungButton, meldungsauswertungButton);

        Scene scene = new Scene(vbox, 200, 250);
        stage.setScene(scene);
        stage.setTitle("Urkundenauswertung");
        stage.setResizable(true);
        stage.initStyle(StageStyle.UNDECORATED); // Titelleiste ausblenden
        stage.show();
    }

    @FXML
    private void openGesamtauswertung() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Gesamtauswertung");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ChoiceBox<String> dropdown = new ChoiceBox<>();
        dropdown.getItems().addAll("10km", "3km");
        dropdown.setStyle("-fx-background-color: rgba(227,212,173); -fx-font-family: Arial;");
        dialog.getDialogPane().setContent(dropdown);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return dropdown.getValue();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(distance -> {
            if ("10km".equals(distance)) {
                handleGesamtauswertung("10km", "datas/Zwischenspeicher/Gesamt10km.csv", "10.0");
            } else if ("3km".equals(distance)) {
                handleGesamtauswertung("3km", "datas/Zwischenspeicher/Gesamt3km.csv", "3.0");
            }
        });

        dialog.setWidth(200);
        dialog.setHeight(250);
        dialog.setResizable(true); // Fenster resizable machen
        dialog.initStyle(StageStyle.UNDECORATED); // Titelleiste ausblenden
        dialog.showAndWait();
    }

    private void handleGesamtauswertung(String distance, String outputFilePath, String filterValue) {
        try {
            File outputFile = new File(outputFilePath);
            if (outputFile.exists()) {
                outputFile.delete();
            }
            outputFile.createNewFile();

            String csvFilePath = "datas/Zwischenspeicher/Daten.csv";
            ExcelUtils.convertExcelToCSV("datas/Daten/Daten.xlsx", csvFilePath);

            List<String[]> filteredData = ExcelUtils.filterCSV(csvFilePath, 9, filterValue);
            ExcelUtils.writeDataToCSV(filteredData, outputFilePath);

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Daten erfolgreich gefiltert.", ButtonType.OK);
            successAlert.showAndWait().ifPresent(button -> {
                if (button == ButtonType.OK) {
                    handleAuswerten(outputFilePath, distance, "G");
                }
            });

        } catch (IOException e) {
            showError("Fehler beim Filtern der Daten.", e);
        }
    }

    private void handleAuswerten(String filePath, String distance, String category) {
        try {
            List<String> results = ExcelUtils.processAndReturnTop3CSV(filePath, 11);

            String alertContent = String.join("\n", results);
            Alert resultsAlert = new Alert(Alert.AlertType.INFORMATION, alertContent, ButtonType.OK);
            resultsAlert.setTitle("Ergebnisse");
            resultsAlert.setHeaderText("Top 3 Ergebnisse");
            ButtonType saveButtonType = new ButtonType("Sichern", ButtonBar.ButtonData.OK_DONE);
            resultsAlert.getButtonTypes().setAll(ButtonType.OK, saveButtonType);

            Optional<ButtonType> alertResult = resultsAlert.showAndWait();
            if (alertResult.isPresent() && alertResult.get() == saveButtonType) {
                int nextIndex = getNextFileIndex(distance, category);
                String outputPath = String.format("datas/Auswertungen/%s%s%d.txt", distance, category, nextIndex);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
                    for (String result : results) {
                        writer.write(result);
                        writer.newLine();
                    }
                }

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Auswertung abgeschlossen und gespeichert.", ButtonType.OK);
                successAlert.showAndWait();
            }

        } catch (IOException e) {
            showError("Fehler bei der Auswertung der Daten.", e);
        }
    }

    private int getNextFileIndex(String distance, String category) {
        File folder = new File("datas/Auswertungen");
        File[] listOfFiles = folder.listFiles((dir, name) -> name.matches(distance + category + "\\d+\\.txt"));
        int maxIndex = 0;
        if (listOfFiles != null) {
            Pattern pattern = Pattern.compile(distance + category + "(\\d+)\\.txt");
            for (File file : listOfFiles) {
                Matcher matcher = pattern.matcher(file.getName());
                if (matcher.matches()) {
                    int index = Integer.parseInt(matcher.group(1));
                    if (index > maxIndex) {
                        maxIndex = index;
                    }
                }
            }
        }
        return maxIndex + 1;
    }

    @FXML
    private void openMannschaftsauswertung() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Mannschaftsauswertung");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ChoiceBox<String> dropdown = new ChoiceBox<>();
        dropdown.getItems().addAll("10km", "7.5km");
        dropdown.setStyle("-fx-background-color: rgba(227,212,173); -fx-font-family: Arial;");
        dialog.getDialogPane().setContent(dropdown);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return dropdown.getValue();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(distance -> {
            ChoiceBox<String> teamDropdown = new ChoiceBox<>();
            try {
                List<String> teams = ExcelUtils.getTeams("datas/Mannschaften/Mannschaften.xlsx");
                teamDropdown.getItems().addAll(teams);
                teamDropdown.setStyle("-fx-background-color: rgba(227,212,173); -fx-font-family: Arial;");
            } catch (IOException e) {
                showError("Fehler beim Laden der Mannschaften.", e);
                return;
            }

            Dialog<String> teamDialog = new Dialog<>();
            teamDialog.setTitle("Mannschaftsauswahl");
            teamDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            teamDialog.getDialogPane().setContent(teamDropdown);

            teamDialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return teamDropdown.getValue();
                }
                return null;
            });

            Optional<String> teamResult = teamDialog.showAndWait();
            teamResult.ifPresent(team -> {
                if ("10km".equals(distance)) {
                    handleMannschaftsauswertung("10km", "datas/Zwischenspeicher/Mannschaft10km.csv", "10.0", team);
                } else if ("7.5km".equals(distance)) {
                    handleMannschaftsauswertung("7.5km", "datas/Zwischenspeicher/Mannschaft7.5km.csv", "7.5", team);
                }
            });
        });

        dialog.setWidth(200);
        dialog.setHeight(250);
        dialog.setResizable(true); // Fenster resizable machen
        dialog.initStyle(StageStyle.UNDECORATED); // Titelleiste ausblenden
        dialog.showAndWait();
    }

    private void handleMannschaftsauswertung(String distance, String outputFilePath, String filterValue, String team) {
        try {
            File outputFile = new File(outputFilePath);
            if (outputFile.exists()) {
                outputFile.delete();
            }
            outputFile.createNewFile();

            String csvFilePath = "datas/Zwischenspeicher/Daten.csv";
            ExcelUtils.convertExcelToCSV("datas/Daten/Daten.xlsx", csvFilePath);

            List<String[]> filteredData = ExcelUtils.filterCSVWithTeam(csvFilePath, 9, filterValue, team);
            ExcelUtils.writeDataToCSV(filteredData, outputFilePath);

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Daten erfolgreich gefiltert.", ButtonType.OK);
            successAlert.showAndWait().ifPresent(button -> {
                if (button == ButtonType.OK) {
                    handleAuswerten(outputFilePath, distance, "M");
                }
            });

        } catch (IOException e) {
            showError("Fehler beim Filtern der Daten.", e);
        }
    }

    @FXML
    private void openAltersklassenauswertung() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Altersklassenauswertung");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ChoiceBox<String> dropdown = new ChoiceBox<>();
        dropdown.getItems().addAll("10km", "7.5km", "3km");
        dropdown.setStyle("-fx-background-color: rgba(227,212,173); -fx-font-family: Arial;");
        dialog.getDialogPane().setContent(dropdown);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return dropdown.getValue();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(distance -> {
            ChoiceBox<String> ageClassDropdown = new ChoiceBox<>();
            ageClassDropdown.getItems().addAll("M", "M30", "M40", "M45", "M50", "M55", "M60", "W", "W30", "W40", "W45", "W50", "W55", "W60");
            ageClassDropdown.setStyle("-fx-background-color: rgba(227,212,173); -fx-font-family: Arial;");

            Dialog<String> ageClassDialog = new Dialog<>();
            ageClassDialog.setTitle("Altersklassenauswahl");
            ageClassDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            ageClassDialog.getDialogPane().setContent(ageClassDropdown);

            ageClassDialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return ageClassDropdown.getValue();
                }
                return null;
            });

            Optional<String> ageClassResult = ageClassDialog.showAndWait();
            ageClassResult.ifPresent(ageClass -> {
                if ("10km".equals(distance)) {
                    handleAltersklassenauswertung("10km", "datas/Zwischenspeicher/Altersklasse10km.csv", "10.0", ageClass);
                } else if ("7.5km".equals(distance)) {
                    handleAltersklassenauswertung("7.5km", "datas/Zwischenspeicher/Altersklasse7.5km.csv", "7.5", ageClass);
                } else if ("3km".equals(distance)) {
                    handleAltersklassenauswertung("3km", "datas/Zwischenspeicher/Altersklasse3km.csv", "3.0", ageClass);
                }
            });
        });

        dialog.setWidth(200);
        dialog.setHeight(250);
        dialog.setResizable(true); // Fenster resizable machen
        dialog.initStyle(StageStyle.UNDECORATED); // Titelleiste ausblenden
        dialog.showAndWait();
    }

    private void handleAltersklassenauswertung(String distance, String outputFilePath, String filterValue, String ageClass) {
        try {
            File outputFile = new File(outputFilePath);
            if (outputFile.exists()) {
                outputFile.delete();
            }
            outputFile.createNewFile();

            String csvFilePath = "datas/Zwischenspeicher/Daten.csv";
            ExcelUtils.convertExcelToCSV("datas/Daten/Daten.xlsx", csvFilePath);

            List<String[]> filteredData = ExcelUtils.filterCSVWithAgeClass(csvFilePath, 9, filterValue, ageClass);
            ExcelUtils.writeDataToCSV(filteredData, outputFilePath);

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Daten erfolgreich gefiltert.", ButtonType.OK);
            successAlert.showAndWait().ifPresent(button -> {
                if (button == ButtonType.OK) {
                    handleAuswerten(outputFilePath, distance, "A");
                }
            });

        } catch (IOException e) {
            showError("Fehler beim Filtern der Daten.", e);
        }
    }

    @FXML
    private void openMeldungsauswertung() {
        try {
            String outputFilePath = "datas/Zwischenspeicher/Meldungen.csv";
            File outputFile = new File(outputFilePath);
            if (outputFile.exists()) {
                outputFile.delete();
            }
            outputFile.createNewFile();

            String csvFilePath = "datas/Zwischenspeicher/Daten.csv";
            ExcelUtils.convertExcelToCSV("datas/Daten/Daten.xlsx", csvFilePath);

            String mostFrequentTeam = ExcelUtils.findMostFrequentTeam(csvFilePath, 9, "7.5");

            List<String[]> filteredData = ExcelUtils.filterCSVWithTeam(csvFilePath, 9, "7.5", mostFrequentTeam);
            ExcelUtils.writeDataToCSV(filteredData, outputFilePath);

            Alert resultsAlert = new Alert(Alert.AlertType.INFORMATION, "Am häufigsten Team: " + mostFrequentTeam, ButtonType.OK);
            resultsAlert.setTitle("Meldungsauswertung");
            resultsAlert.setHeaderText("Team: " + mostFrequentTeam + " mit 7.5 Eintrag");
            ButtonType saveButtonType = new ButtonType("Sichern", ButtonBar.ButtonData.OK_DONE);
            resultsAlert.getButtonTypes().setAll(ButtonType.OK, saveButtonType);

            Optional<ButtonType> alertResult = resultsAlert.showAndWait();
            if (alertResult.isPresent() && alertResult.get() == saveButtonType) {
                String outputPath = "datas/Auswertungen/Meldungen.txt";
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
                    for (String[] row : filteredData) {
                        writer.write(String.join(",", row));
                        writer.newLine();
                    }
                }

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Auswertung abgeschlossen und gespeichert.", ButtonType.OK);
                successAlert.showAndWait();
            }

        } catch (IOException e) {
            showError("Fehler bei der Meldungsauswertung.", e);
        }
    }

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
        e.printStackTrace();
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
