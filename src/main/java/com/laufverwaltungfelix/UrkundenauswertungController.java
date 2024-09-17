package com.laufverwaltungfelix;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

        gesamtauswertungButton.setOnAction(e -> openGesamtauswertung());

        Button mannschaftsauswertungButton = new Button("Mannschaftsauswertung");

        mannschaftsauswertungButton.setOnAction(e -> openMannschaftsauswertung());

        Button altersklassenauswertungButton = new Button("Altersklassenauswertung");

        altersklassenauswertungButton.setOnAction(e -> openAltersklassenauswertung());

        Button meldungsauswertungButton = new Button("Meldungsauswertung");

        meldungsauswertungButton.setOnAction(e -> openMeldungsauswertung());

        vbox.getChildren().addAll(gesamtauswertungButton, mannschaftsauswertungButton, altersklassenauswertungButton,
                meldungsauswertungButton);

        Scene scene = new Scene(vbox, 200, 250);
        stage.setScene(scene);
        stage.setTitle("Urkundenauswertung");
        stage.setResizable(true);
        stage.initStyle(StageStyle.UNDECORATED); // Titelleiste ausblenden
        stage.show();
    }

    @FXML
    private void openMeldungsauswertung() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Meldungsauswertung");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ChoiceBox<String> dropdown = new ChoiceBox<>();
        dropdown.getItems().addAll("Gesamt", "7.5km");

        dialog.getDialogPane().setContent(dropdown);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return dropdown.getValue();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selection -> {
            // Deine existierende Logik
            String outputFilePath = "datas/Zwischenspeicher/Meldungen.csv";
            File outputFile = new File(outputFilePath);
            try {
                if (outputFile.exists()) {
                    outputFile.delete();
                }
                outputFile.createNewFile();

                String csvFilePath = "datas/Zwischenspeicher/Daten.csv";
                ExcelUtils.convertExcelToCSV("datas/Daten/Daten.xlsx", csvFilePath);

                String mostFrequentTeam;
                List<String[]> filteredData;

                if ("Gesamt".equals(selection)) {
                    List<String> distances = Arrays.asList("10.0", "7.5", "3.0");
                    mostFrequentTeam = ExcelUtils.findMostFrequentTeam(csvFilePath, 9, distances);
                    filteredData = ExcelUtils.filterCSVWithTeam(csvFilePath, 9, distances, mostFrequentTeam);
                    outputFilePath = "datas/Auswertungen/GesamtMeldungen.txt";
                } else if ("7.5km".equals(selection)) {
                    mostFrequentTeam = ExcelUtils.findMostFrequentTeam(csvFilePath, 9,
                            Collections.singletonList("7.5"));
                    filteredData = ExcelUtils.filterCSVWithTeam(csvFilePath, 9, Collections.singletonList("7.5"),
                            mostFrequentTeam);
                    outputFilePath = "datas/Auswertungen/7.5kmMeldungen.txt";
                } else {
                    showError("Ungültige Auswahl.", new IllegalArgumentException("Kein gültiger Wert ausgewählt"));
                    return;
                }

                ExcelUtils.writeDataToCSV(filteredData, outputFilePath);

                Alert resultsAlert = new Alert(Alert.AlertType.INFORMATION, "Am häufigsten Team: " + mostFrequentTeam,
                        ButtonType.OK);
                resultsAlert.setTitle("Meldungsauswertung");
                resultsAlert.setHeaderText("Team: " + mostFrequentTeam + " ausgewählt");
                ButtonType saveButtonType = new ButtonType("Sichern", ButtonBar.ButtonData.OK_DONE);
                resultsAlert.getButtonTypes().setAll(ButtonType.OK, saveButtonType);

                Optional<ButtonType> alertResult = resultsAlert.showAndWait();
                if (alertResult.isPresent() && alertResult.get() == saveButtonType) {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
                        for (String[] row : filteredData) {
                            writer.write(String.join(",", row));
                            writer.newLine();
                        }
                    }

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION,
                            "Auswertung abgeschlossen und gespeichert.", ButtonType.OK);
                    successAlert.showAndWait();
                }

            } catch (IOException e) {
                showError("Fehler bei der Meldungsauswertung.", e);
            }
        });
    }

    @FXML
    private Button closeButton;

    @FXML
    private void closeWindow() {
        // Holt das aktuelle Stage (Fenster) und schließt es
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private List<String[]> handleMannschaftsauswertung(String distance, String outputFilePath, String filterValue,
            String team) {
        List<String[]> top3Entries = new ArrayList<>();
        try {
            File outputFile = new File(outputFilePath);
            if (outputFile.exists()) {
                outputFile.delete();
            }
            outputFile.createNewFile();

            String csvFilePath = "datas/Zwischenspeicher/Daten.csv";
            ExcelUtils.convertExcelToCSV("datas/Daten/Daten.xlsx", csvFilePath);

            // Verwende die aktualisierte ExcelUtils.filterCSVWithTeam-Methode, die eine
            // List<String> erwartet
            List<String[]> filteredData = ExcelUtils.filterCSVWithTeam(csvFilePath, 9,
                    Collections.singletonList(filterValue), team);

            if (filteredData.size() >= 3) { // Sicherstellen, dass mindestens 3 Einträge vorhanden sind
                // Sortiere die gefilterten Daten nach der Zeit (Spalte 11)
                top3Entries = filteredData.stream()
                        .sorted(java.util.Comparator.comparingDouble(o -> Double.parseDouble(o[11])))
                        .limit(3)
                        .toList();
            }

        } catch (IOException e) {
            showError("Fehler beim Filtern der Daten.", e);
        }
        return top3Entries;
    }

    @FXML
    private void openGesamtauswertung() {
        // Dialog für Distanz und Geschlecht Auswahl
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Gesamtauswertung");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ChoiceBox<String> distanceDropdown = new ChoiceBox<>();
        distanceDropdown.getItems().addAll("10km", "3km");

        ChoiceBox<String> genderDropdown = new ChoiceBox<>();
        genderDropdown.getItems().addAll("Maenlich", "Weiblich");

        VBox vbox = new VBox(10, distanceDropdown, genderDropdown);
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK && distanceDropdown.getValue() != null
                    && genderDropdown.getValue() != null) {
                return new String[] { distanceDropdown.getValue(), genderDropdown.getValue() };
            }
            return null;
        });

        Optional<String[]> result = dialog.showAndWait();
        result.ifPresent(selection -> {
            String distance = selection[0];
            String gender = selection[1];
            handleGesamtauswertung(distance, gender);
        });

        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.showAndWait();
    }

    private void handleGesamtauswertung(String distance, String gender) {
        try {
            String csvFilePath = "datas/Zwischenspeicher/Daten.csv";
            String outputFilePath = String.format("datas/Zwischenspeicher/%s%sGesamt.txt", distance, gender);

            // Filterung der CSV-Datei nach Distanz und Geschlecht
            ExcelUtils.convertExcelToCSV("datas/Daten/Daten.xlsx", csvFilePath);
            List<String[]> filteredData = ExcelUtils.filterCSVByDistanceAndGender(csvFilePath, 9,
                    distance.equals("10km") ? "10.0" : "3.0", 6, gender);

            ExcelUtils.writeDataToCSV(filteredData, outputFilePath);

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Daten erfolgreich gefiltert.", ButtonType.OK);
            successAlert.showAndWait().ifPresent(button -> {
                if (button == ButtonType.OK) {
                    handleAuswerten(outputFilePath, distance, "Gesamt", gender, "");
                }
            });

        } catch (IOException e) {
            showError("Fehler beim Filtern der Daten.", e);
        }
    }

    private void handleAuswerten(String filePath, String distance, String category, String genderInitial,
            String suffix) {
        try {
            List<String> results = ExcelUtils.processAndReturnTop3CSV(filePath, 11);
            String alertContent = String.join("\n", results);

            Alert resultsAlert = new Alert(Alert.AlertType.INFORMATION, alertContent, ButtonType.OK);
            resultsAlert.setTitle("Ergebnisse");
            resultsAlert.setHeaderText("Top 3 Ergebnisse");

            ButtonType saveButtonType = new ButtonType("Sichern", ButtonType.OK.getButtonData());
            resultsAlert.getButtonTypes().setAll(ButtonType.OK, saveButtonType);

            Optional<ButtonType> alertResult = resultsAlert.showAndWait();
            if (alertResult.isPresent() && alertResult.get() == saveButtonType) {
                String outputPath;

                if (!genderInitial.isEmpty()) {
                    outputPath = String.format("datas/Auswertungen/%s%s%s.txt", distance, genderInitial, category);
                } else if (!suffix.isEmpty()) {
                    outputPath = String.format("datas/Auswertungen/%s%s%s.txt", distance, category, suffix);
                } else {
                    outputPath = String.format("datas/Auswertungen/%s%s.txt", distance, category);
                }

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
                    for (String result : results) {
                        writer.write(result);
                        writer.newLine();
                    }
                }

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Auswertung abgeschlossen und gespeichert.",
                        ButtonType.OK);
                successAlert.showAndWait();
            }

        } catch (IOException e) {
            showError("Fehler bei der Auswertung der Daten.", e);
        }
    }

    @FXML
    private void openAltersklassenauswertung() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Altersklassenauswertung");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ChoiceBox<String> distanceDropdown = new ChoiceBox<>();
        distanceDropdown.getItems().addAll("10km", "7.5km", "3km");

        // Add "M" and "W" for gender options along with age classes
        ChoiceBox<String> ageClassDropdown = new ChoiceBox<>();
        ageClassDropdown.getItems().addAll("M", "M30", "M40", "M45", "M50", "M55", "M60", "W", "W30", "W40", "W45",
                "W50", "W55", "W60");

        VBox vbox = new VBox(10, distanceDropdown, ageClassDropdown);
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK && distanceDropdown.getValue() != null
                    && ageClassDropdown.getValue() != null) {
                return new String[] { distanceDropdown.getValue(), ageClassDropdown.getValue() };
            }
            return null;
        });

        Optional<String[]> result = dialog.showAndWait();
        result.ifPresent(selection -> {
            String distance = selection[0];
            String ageClass = selection[1];
            handleAltersklassenauswertung(distance, ageClass);
        });
    }

    private void handleAltersklassenauswertung(String distance, String ageClass) {
        try {
            String csvFilePath = "datas/Zwischenspeicher/Daten.csv";
            String outputFilePath = String.format("datas/Zwischenspeicher/%s%s.txt", distance, ageClass);

            ExcelUtils.convertExcelToCSV("datas/Daten/Daten.xlsx", csvFilePath);
            List<String[]> filteredData = ExcelUtils.filterCSVWithAgeClass(csvFilePath, 9,
                    distance.equals("10km") ? "10.0" : distance.equals("7.5km") ? "7.5" : "3.0", ageClass);

            ExcelUtils.writeDataToCSV(filteredData, outputFilePath);

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Daten erfolgreich gefiltert.", ButtonType.OK);
            successAlert.showAndWait().ifPresent(button -> {
                if (button == ButtonType.OK) {
                    handleAuswerten(outputFilePath, distance, "Altersklasse", "", ageClass);
                }
            });
        } catch (IOException e) {
            showError("Fehler beim Filtern der Daten.", e);
        }
    }

    // Mannschaftsauswertung
    @FXML
    private void openMannschaftsauswertung() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Mannschaftsauswertung");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ChoiceBox<String> distanceDropdown = new ChoiceBox<>();
        distanceDropdown.getItems().addAll("10km", "7.5km");

        dialog.getDialogPane().setContent(distanceDropdown);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK && distanceDropdown.getValue() != null) {
                return distanceDropdown.getValue();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(distance -> {
            try {
                List<String> teams = ExcelUtils.getTeams("datas/Mannschaften/Mannschaften.xlsx");
                List<TeamResult> teamResults = new ArrayList<>();

                for (String team : teams) {
                    List<String[]> top3Entries = handleMannschaftsauswertung(distance,
                            "datas/Zwischenspeicher/Mannschaft" + distance + ".csv",
                            distance.equals("10km") ? "10.0" : "7.5", team);
                    if (!top3Entries.isEmpty()) {
                        double totalTime = top3Entries.stream().mapToDouble(o -> Double.parseDouble(o[11])).sum();
                        teamResults.add(new TeamResult(team, totalTime, top3Entries));
                    }
                }

                // Sortiere die Ergebnisse nach der Gesamtzeit
                teamResults.sort(java.util.Comparator.comparingDouble(TeamResult::getTotalTime));

                // Schreibe die Ergebnisse für die Top 3 Mannschaften
                for (int i = 0; i < Math.min(3, teamResults.size()); i++) {
                    TeamResult resultTeam = teamResults.get(i);
                    String outputPath = String.format("datas/Auswertungen/%s%s%d.Platz.txt",
                            distance, resultTeam.getTeamName(), i + 1);

                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
                        for (String[] row : resultTeam.getTop3Entries()) {
                            writer.write(String.join(",", row));
                            writer.newLine();
                        }
                    }
                }
            } catch (IOException e) {
                showError("Fehler beim Laden der Mannschaften.", e);
            }
        });
    }

    // Fehlerbehandlung
    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
        e.printStackTrace();
    }

    // TeamResult Klasse für die Mannschaftsauswertung
    private static class TeamResult {
        private final String teamName;
        private final double totalTime;
        private final List<String[]> top3Entries;

        public TeamResult(String teamName, double totalTime, List<String[]> top3Entries) {
            this.teamName = teamName;
            this.totalTime = totalTime;
            this.top3Entries = top3Entries;
        }

        public String getTeamName() {
            return teamName;
        }

        public double getTotalTime() {
            return totalTime;
        }

        public List<String[]> getTop3Entries() {
            return top3Entries;
        }
    }
}
