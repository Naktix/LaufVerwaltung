package com.laufverwaltungfelix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DruckenController {

    // Methode zur Konvertierung von Excel zu CSV
    private void convertExcelToCSV(String excelFilePath, String csvFilePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
                Workbook workbook = new XSSFWorkbook(fis);
                FileWriter csvWriter = new FileWriter(new File(csvFilePath))) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                StringBuilder csvLine = new StringBuilder();
                row.forEach(cell -> {
                    switch (cell.getCellType()) {
                        case STRING:
                            csvLine.append(cell.getStringCellValue());
                            break;
                        case NUMERIC:
                            csvLine.append(cell.getNumericCellValue());
                            break;
                        case BOOLEAN:
                            csvLine.append(cell.getBooleanCellValue());
                            break;
                        default:
                            csvLine.append("");
                    }
                    csvLine.append(",");
                });
                csvLine.deleteCharAt(csvLine.length() - 1); // Entferne das letzte Komma
                csvWriter.write(csvLine.toString());
                csvWriter.write("\n");
            }
        }
    }

    // Methode zum Generieren der Startzettel
    private void generateStartzettel() {
        String excelFilePath = "datas/Daten/Daten.xlsx";
        String csvFilePath = "datas/Zwischenspeicher/Daten.csv";

        // Konvertiere Excel zu CSV
        try {
            convertExcelToCSV(excelFilePath, csvFilePath);
        } catch (IOException e) {
            showAlert("Fehler", "Fehler beim Konvertieren der Excel-Datei.");
            e.printStackTrace();
            return;
        }

        // Lese die CSV-Datei und verarbeite sie
        File csvFile = new File(csvFilePath);
        try {
            List<String> lines = Files.readAllLines(csvFile.toPath());

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] data = line.split(",");

                String id = data[0].trim();
                String dienstgrad = data[3].trim();
                String nachname = data[1].trim();
                String vorname = data[2].trim();
                String mannschaft = data[8].trim();
                String strecke = getStrecke(data[9].trim());
                String altersklasse = data[10].trim();

                try (PDDocument pdfDocument = PDDocument.load(new File("datas/Blanko/Laufzettel.pdf"))) {
                    PDAcroForm form = pdfDocument.getDocumentCatalog().getAcroForm();

                    fillField(form, "Jahr", String.valueOf(LocalDate.now().getYear()));
                    fillField(form, "Text6", id);
                    fillField(form, "Strecke", strecke);
                    fillField(form, "Lauf", strecke);
                    fillField(form, "läufer", dienstgrad + " " + nachname + " " + vorname + " " + mannschaft);
                    fillField(form, "Nr", id);
                    fillField(form, "Altersklasse", altersklasse);

                    String outputFilename = "datas/UrkundenLaufzettel/Laufzettel" + (i + 1) + ".pdf";
                    createDirectoryIfNotExists("datas/UrkundenLaufzettel");
                    pdfDocument.save(outputFilename);
                }
            }

            showAlert("Erfolg", "Startzettel wurden erfolgreich generiert.");

        } catch (IOException e) {
            showAlert("Fehler", "Fehler beim Generieren der Startzettel.");
            e.printStackTrace();
        }
    }

    /*
     * private int getNextFileIndex(String prefix) {
     * File folder = new File("datas/UrkundenLaufzettel");
     * File[] files = folder
     * .listFiles((dir, name) -> name.startsWith(new File(prefix).getName()) &&
     * name.endsWith(".pdf"));
     * int maxIndex = 0;
     * if (files != null) {
     * // Use fully qualified class name to avoid collision
     * java.util.regex.Pattern pattern = java.util.regex.Pattern
     * .compile(java.util.regex.Pattern.quote(prefix) + "(\\d+)\\.pdf");
     * for (File file : files) {
     * java.util.regex.Matcher matcher = pattern.matcher(file.getName());
     * if (matcher.find()) {
     * int index = Integer.parseInt(matcher.group(1));
     * if (index > maxIndex) {
     * maxIndex = index;
     * }
     * }
     * }
     * }
     * return maxIndex + 1;
     * }
     */

    private void generateMeldungenPdf(String filename) throws IOException {
        // Suche nach der ausgewählten Datei im "datas/Auswertungen/"-Ordner
        File auswertungenFolder = new File("datas/Auswertungen");
        File txtFile = new File(auswertungenFolder, filename);

        // Überprüfe, ob die Datei existiert
        if (!txtFile.exists()) {
            throw new FileNotFoundException(filename + " nicht gefunden.");
        }

        // Bestimme den Namen der Ausgabedatei basierend auf dem ausgewählten Dateinamen
        String outputPdfFilename;
        String category;
        if (filename.equals("7.5kmMeldungen.txt")) {
            outputPdfFilename = "7.5kmMeldungen.pdf";
            category = "Team für 7.5km";
        } else if (filename.equals("GesamtMeldungen.txt")) {
            outputPdfFilename = "GesamtMeldungen.pdf";
            category = "Team gesamt";
        } else {
            throw new IllegalArgumentException("Ungültiger Dateiname: " + filename);
        }

        // Lese alle Zeilen der ausgewählten Datei
        List<String> lines = Files.readAllLines(txtFile.toPath());

        // Bestimme Einheit aus der ersten Zeile
        String einheit = getEinheit(lines.get(0));

        // Anzahl der Zeilen
        int anzahl = lines.size();

        // Lade das Blanko-PDF und fülle die Felder aus
        try (PDDocument pdfDocument = PDDocument.load(new File("datas/Blanko/Meldungen.pdf"))) {
            PDAcroForm form = pdfDocument.getDocumentCatalog().getAcroForm();

            // Fülle die Felder des Formulars aus
            fillField(form, "Jahr", String.valueOf(LocalDate.now().getYear()));
            fillField(form, "Laufnr", String.valueOf(LocalDate.now().getYear() - 1957));
            fillField(form, "Einheit", einheit);
            fillField(form, "Anzahl", String.valueOf(anzahl));
            fillField(form, "Datum", DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDate.now()));
            fillField(form, "Kategorie", category);

            // Ausgabe-PDF-Datei speichern
            createDirectoryIfNotExists("datas/UrkundenLaufzettel");
            String outputFilePath = "datas/UrkundenLaufzettel/" + outputPdfFilename;
            pdfDocument.save(outputFilePath);
        }
    }

    private String[] getTimes(List<String> lines) {
        if (lines.size() < 3) {
            showAlert("Fehler", "Nicht genügend Daten zum Abrufen der Zeiten.");
            return new String[] { "00:00:00", "00:00:00", "00:00:00" };
        }
        return new String[] {
                lines.get(0).split(",")[11].trim(),
                lines.get(1).split(",")[11].trim(),
                lines.get(2).split(",")[11].trim()
        };
    }

    private void fillField(PDAcroForm form, String fieldName, String value) throws IOException {
        PDField field = form.getField(fieldName);
        if (field != null) {
            field.setValue(value);
        }
    }

    private String getTeamName(String line) {
        return line.split(",")[8].trim();
    }

    private String getEinheit(String line) {
        return line.split(",")[8].trim();
    }

    private String getDistance(String line) {
        String distanceValue = line.split(",")[9].trim();
        return distanceValue.equals("10.0") ? "10000M" : (distanceValue.equals("7.5") ? "7500M Walken" : "3000M");
    }

    private String getDienstgrad(String line) {
        return line.split(",")[3].trim();
    }

    private String getVorname(String line) {
        return line.split(",")[2].trim();
    }

    private String getNachname(String line) {
        return line.split(",")[1].trim();
    }

    private String getAltersklasse(String line) {
        return line.split(",")[10].trim();
    }

    private String getTime(String line) {
        return line.split(",")[11].trim();
    }

    private String getName(String line) {
        String[] parts = line.split(",");
        return parts[1].trim() + " " + parts[2].trim();
    }

    private String getStrecke(String value) {
        switch (value) {
            case "10.0":
                return "10000M";
            case "7.5":
                return "7500M Walken";
            case "3.0":
                return "3000M";
            default:
                return "";
        }
    }

    private String formatTime(String time) {
        double millisecondsTotal = Double.parseDouble(time) * 1000;
        long hours = (long) millisecondsTotal / 3600000;
        long minutes = ((long) millisecondsTotal % 3600000) / 60000;
        long seconds = ((long) millisecondsTotal % 60000) / 1000;
        long milliseconds = (long) millisecondsTotal % 1000;
        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds);
    }

    // Corrected sumTimes method with error handling
    private String sumTimes(String[] times) {
        double sumMilliseconds = 0;
        for (String time : times) {
            String[] timeParts = time.split(":");
            double hours = timeParts.length > 0 ? Double.parseDouble(timeParts[0]) : 0;
            double minutes = timeParts.length > 1 ? Double.parseDouble(timeParts[1]) : 0;
            double seconds = timeParts.length > 2 ? Double.parseDouble(timeParts[2]) : 0;
            sumMilliseconds += (seconds + minutes + hours);
        }
        return formatTime(String.valueOf(sumMilliseconds));
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) printButton.getScene().getWindow();
        stage.close();
    }

    private void createDirectoryIfNotExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @FXML
    private Button printButton;

    @FXML
    private void openPrintDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Drucken");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button teamurkundeButton = new Button("Teamurkunde");
        teamurkundeButton.setOnAction(e -> openTeamurkundeDialog());

        Button personenurkundeButton = new Button("Personenurkunde");

        personenurkundeButton.setOnAction(e -> openPersonenurkundeDialog());

        Button meldungenButton = new Button("Meldungen");

        meldungenButton.setOnAction(e -> openMeldungenDialog());

        Button startzettelButton = new Button("Startzettel");

        startzettelButton.setOnAction(e -> generateStartzettel());

        VBox vbox = new VBox(10, teamurkundeButton, personenurkundeButton, meldungenButton, startzettelButton);

        dialog.getDialogPane().setContent(vbox);

        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.showAndWait();
    }

    // Methode zum Abrufen von Auswertungsdateien mit verschiedenen Filterregeln
    private List<String> getAuswertungenFiles(String regex) {
        File auswertungenDir = new File("datas/Auswertungen");
        try {
            return Files.list(auswertungenDir.toPath())
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.matches(regex))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            showAlert("Fehler", "Fehler beim Laden der Dateien.");
            return List.of();
        }
    }

    // Methode für die Teamurkunde
    private void openTeamurkundeDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Teamurkunde drucken");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ChoiceBox<String> fileChoiceBox = new ChoiceBox<>();
        // Filter für Dateien, die "1.Platz", "2.Platz" oder "3.Platz" im Namen haben
        fileChoiceBox.getItems().addAll(getAuswertungenFiles(".*(1\\.Platz|2\\.Platz|3\\.Platz).*\\.txt"));

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                String selectedFile = fileChoiceBox.getValue();
                if (selectedFile != null) {
                    try {
                        generateTeamUrkunde(selectedFile);
                    } catch (IOException ex) {
                        showAlert("Fehler", "Fehler beim Generieren der Urkunde.");
                        ex.printStackTrace();
                    }
                }
            }
            return null;
        });

        VBox vbox = new VBox(10, fileChoiceBox);

        dialog.getDialogPane().setContent(vbox);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.showAndWait();
    }

    // Methode für die Personenurkunde
    private void openPersonenurkundeDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Personenurkunde drucken");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ChoiceBox<String> fileChoiceBox = new ChoiceBox<>();
        // Filter für Dateien, die "Gesamt", "M", "M30", "M40", "M45", "M50", "M55",
        // "M60", "W", "W30", "W40", "W45", "W50", "W60" im Namen haben
        List<String> files = getAuswertungenFiles(".*(Gesamt|M(30|40|45|50|55|60)?|W(30|40|45|50|60)?).*\\.txt");

        // Filter out specific files (e.g., "7.5kmMeldungen.txt" and
        // "GesamtMeldungen.txt")
        List<String> filteredFiles = files.stream()
                .filter(file -> !file.equals("7.5kmMeldungen.txt") && !file.equals("GesamtMeldungen.txt"))
                .collect(Collectors.toList());

        fileChoiceBox.getItems().addAll(filteredFiles);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                String selectedFile = fileChoiceBox.getValue();
                if (selectedFile != null) {
                    try {
                        generatePersonenUrkunde(selectedFile);
                    } catch (IOException ex) {
                        showAlert("Fehler", "Fehler beim Generieren der Urkunde.");
                        ex.printStackTrace();
                    }
                }
            }
            return null;
        });

        VBox vbox = new VBox(10, fileChoiceBox);
        dialog.getDialogPane().setContent(vbox);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.showAndWait();
    }

    // Methode für die Meldungenauswertung
    private void openMeldungenDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Meldungen drucken");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ChoiceBox<String> fileChoiceBox = new ChoiceBox<>();
        // Filter für Dateien, die "Meldungen" im Namen haben
        fileChoiceBox.getItems().addAll(getAuswertungenFiles(".*Meldungen.*\\.txt"));

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                String selectedFile = fileChoiceBox.getValue();
                if (selectedFile != null) {
                    try {
                        generateMeldungenPdf(selectedFile);
                    } catch (IOException ex) {
                        showAlert("Fehler", "Fehler beim Generieren der Meldungen.");
                        ex.printStackTrace();
                    }
                }
            }
            return null;
        });

        VBox vbox = new VBox(10, fileChoiceBox);

        dialog.getDialogPane().setContent(vbox);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Methode zum Generieren der Personenurkunde
    private void generatePersonenUrkunde(String filename) throws IOException {
        String fileNameWithoutTxt = removeTxtExtension(filename); // Entferne die .txt-Erweiterung
        File txtFile = new File("datas/Auswertungen/" + filename);
        List<String> lines = Files.readAllLines(txtFile.toPath());

        // Überprüfen, ob der Dateiname "Gesamt" enthält
        boolean isGesamt = fileNameWithoutTxt.contains("Gesamt");

        // Extrahiere die Altersklasse aus dem Dateinamen, falls es kein
        // "Gesamt"-Dateiname ist
        String altersklasseFromFileName = isGesamt ? "" : extractAltersklasseFromFileName(fileNameWithoutTxt);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String distance = getDistance(line);
            String dienstgrad = getDienstgrad(line);
            String vorname = getVorname(line);
            String nachname = getNachname(line);
            String altersklasse = getAltersklasse(line); // Altersklasse aus der Zeile extrahieren
            String time = getTime(line);

            String platz = (i + 1) + ".Platz"; // Platz basierend auf der Zeile
            String kilometerZahl = getKilometerZahl(line);

            // Falls "Gesamt" im Dateinamen vorhanden, keine Altersklasse nutzen, ansonsten
            // die aus der Zeile/Dateinamen
            String altersklassePart = isGesamt ? ""
                    : (!altersklasseFromFileName.isEmpty() ? altersklasseFromFileName : altersklasse);

            // Erstelle den Dateinamen mit "_" als Trennzeichen und ohne Altersklasse bei
            // "Gesamt"
            String outputFilename = "datas/UrkundenLaufzettel/" + kilometerZahl
                    + (altersklassePart.isEmpty() ? "_Gesamt" : "_" + altersklassePart)
                    + "_" + platz + "_" + nachname + "_" + vorname + "_Urkunde.pdf";

            try (PDDocument pdfDocument = PDDocument.load(new File("datas/Blanko/Personurkunde.pdf"))) {
                PDAcroForm form = pdfDocument.getDocumentCatalog().getAcroForm();

                fillField(form, "Jahr", String.valueOf(LocalDate.now().getYear()));
                fillField(form, "Text32", String.valueOf(LocalDate.now().getYear() - 1957));
                fillField(form, "welcher Lauf", "Fliegerhorstlauf");
                fillField(form, "Dienstgrad", dienstgrad);
                fillField(form, "Vorname", vorname);
                fillField(form, "Nachname", nachname);
                fillField(form, "Platz", platz);
                fillField(form, "Strecke", distance);
                fillField(form, "Altersklasse", "Gesamt");

                // Altersklasse wird nur eingetragen, wenn es kein "Gesamt"-Dateiname ist
                if (!isGesamt) {
                    fillField(form, "Altersklasse", altersklassePart);
                }

                fillField(form, "Zeit", formatTime(time));
                fillField(form, "dateField", DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDate.now()));

                createDirectoryIfNotExists("datas/UrkundenLaufzettel");
                pdfDocument.save(outputFilename);
            }
        }
    }

    // Hilfsmethode, um die Altersklasse aus dem Dateinamen zu extrahieren
    private String extractAltersklasseFromFileName(String fileName) {
        // Suche nach Altersklassenspezifikatoren (z.B., "M30", "W50", etc.)
        String regex = "(M|M30|M40|M45|M50|M55|M60|W|W30|W40|W45|W50|W55|W60)";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            return matcher.group();
        }
        return ""; // Falls keine Altersklasse gefunden wird
    }

    // Hilfsmethode zum Entfernen der .txt-Erweiterung
    private String removeTxtExtension(String fileName) {
        if (fileName != null && fileName.endsWith(".txt")) {
            return fileName.substring(0, fileName.length() - 4); // Entferne die letzten 4 Zeichen (.txt)
        }
        return fileName; // Wenn die Datei keine .txt-Endung hat, bleibt sie unverändert
    }

    // Hilfsmethode zum Abrufen der Kilometerzahl für die Dateinamen
    private String getKilometerZahl(String line) {
        String distanceValue = line.split(",")[9].trim();
        switch (distanceValue) {
            case "10.0":
                return "10km";
            case "7.5":
                return "7.5km";
            case "3.0":
                return "3km";
            default:
                return "";
        }
    }

    // Methode zum Generieren der Teamurkunde
    private void generateTeamUrkunde(String filename) throws IOException {
        String fileNameWithoutTxt = removeTxtExtension(filename); // Entferne die .txt-Erweiterung
        File txtFile = new File("datas/Auswertungen/" + filename);
        List<String> lines = Files.readAllLines(txtFile.toPath());

        String teamName = getTeamName(lines.get(0));
        String distance = getDistance(lines.get(0));
        String[] times = getTimes(lines);
        String totalTime = sumTimes(times);

        String platz = "";
        if (filename.contains("1.Platz")) {
            platz = "1. Platz";
        } else if (filename.contains("2.Platz")) {
            platz = "2. Platz";
        } else if (filename.contains("3.Platz")) {
            platz = "3. Platz";
        }

        try (PDDocument pdfDocument = PDDocument.load(new File("datas/Blanko/Teamurkunde.pdf"))) {
            PDAcroForm form = pdfDocument.getDocumentCatalog().getAcroForm();

            fillField(form, "Jahr", String.valueOf(LocalDate.now().getYear()));
            fillField(form, "I", String.valueOf(LocalDate.now().getYear() - 1957));
            fillField(form, "welcher Lauf", "Fliegerhorstlauf");
            fillField(form, "Teamwertung + Strecke", "Teamwertung " + teamName + " " + distance);
            fillField(form, "Platz", platz);
            fillField(form, "Zeit gesamt", totalTime);
            fillField(form, "Name1", getName(lines.get(0)));
            fillField(form, "Zeit1", formatTime(times[0]));
            fillField(form, "Name2", getName(lines.get(1)));
            fillField(form, "Zeit2", formatTime(times[1]));
            fillField(form, "Name3", getName(lines.get(2)));
            fillField(form, "Zeit3", formatTime(times[2]));
            fillField(form, "dateField", DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDate.now()));
            fillField(form, "Einheit", teamName);

            // Erstelle den Dateinamen mit "_" als Trennzeichen
            String outputFilename = "datas/UrkundenLaufzettel/" + fileNameWithoutTxt + "_Urkunde.pdf";

            createDirectoryIfNotExists("datas/UrkundenLaufzettel");
            pdfDocument.save(outputFilename);
        }
    }
}
