package com.laufverwaltungfelix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtils {

    // CSV-Datei nach einem bestimmten Wert filtern
    public static List<String[]> filterCSV(String csvFilePath, int columnIndex, String filterValue) throws IOException {
        List<String[]> filteredData = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns[columnIndex].equals(filterValue)) {
                    filteredData.add(columns);
                }
            }
        }
        return filteredData;
    }

    // Findet das am häufigsten auftretende Team über mehrere Distanzen hinweg
    public static String findMostFrequentTeam(String csvFilePath, int columnIndex, List<String> filterValues)
            throws IOException {
        Map<String, Integer> teamFrequency = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                String distance = columns[columnIndex];
                String team = columns[8];
                if (filterValues.contains(distance)) {
                    teamFrequency.put(team, teamFrequency.getOrDefault(team, 0) + 1);
                }
            }
        }

        return teamFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Kein Team gefunden");
    }

    // CSV-Datei nach einem bestimmten Wert und Team filtern
    public static List<String[]> filterCSVWithTeam(String csvFilePath, int columnIndex, List<String> filterValues,
            String team) throws IOException {
        List<String[]> filteredData = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (filterValues.contains(columns[columnIndex]) && columns[8].equals(team)) {
                    filteredData.add(columns);
                }
            }
        }
        return filteredData;
    }

    // Häufigstes Team in der CSV-Datei finden
    public static String findMostFrequentTeam(String csvFilePath, int columnIndex, String filterValue)
            throws IOException {
        Map<String, Integer> teamCount = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns[columnIndex].equals(filterValue)) {
                    String team = columns[8]; // Angepasster Spaltenindex für Team
                    if (!team.matches("^[MW]\\d*$")) { // Vermeidung von Nicht-Team-Werten wie "W"
                        teamCount.put(team, teamCount.getOrDefault(team, 0) + 1);
                    }
                }
            }
        }
        return Collections.max(teamCount.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    // Werte einer bestimmten Spalte in einer Excel-Datei abrufen
    public static List<String> getColumnValues(String filePath, int columnIndex) throws IOException {
        List<String> values = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(new File(filePath)); Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                Cell cell = row.getCell(columnIndex);
                if (cell != null) {
                    values.add(cell.toString());
                }
            }
        }
        return values;
    }

    // Excel-Datei in CSV umwandeln
    public static void convertExcelToCSV(String excelFilePath, String csvFilePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(excelFilePath));
                FileWriter csvWriter = new FileWriter(csvFilePath)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                List<String> cells = new ArrayList<>();
                for (Cell cell : row) {
                    cells.add(cell.toString());
                }
                csvWriter.write(String.join(",", cells) + "\n");
            }
        }
    }

    // CSV-Datei nach Distanz und Geschlecht filtern
    public static List<String[]> filterCSVByDistanceAndGender(String csvFilePath, int distanceColumnIndex,
            String distance, int genderColumnIndex, String gender) throws IOException {
        List<String[]> filteredData = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns[distanceColumnIndex].equals(distance)
                        && columns[genderColumnIndex].equalsIgnoreCase(gender)) {
                    filteredData.add(columns);
                }
            }
        }
        return filteredData;
    }

    // CSV-Datei nach Altersklasse filtern
    public static List<String[]> filterCSVWithAgeClass(String csvFilePath, int columnIndex, String filterValue,
            String ageClass) throws IOException {
        List<String[]> filteredData = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns[columnIndex].equals(filterValue) && columns[10].equals(ageClass)) {
                    filteredData.add(columns);
                }
            }
        }
        return filteredData;
    }

    // Daten in eine CSV-Datei schreiben
    public static void writeDataToCSV(List<String[]> data, String csvFilePath) throws IOException {
        try (FileWriter csvWriter = new FileWriter(csvFilePath)) {
            for (String[] row : data) {
                csvWriter.write(String.join(",", row) + "\n");
            }
        }
    }

    // Top 3 Einträge in der CSV-Datei basierend auf einem bestimmten Wert
    public static List<String> processAndReturnTop3CSV(String csvFilePath, int columnIndex) throws IOException {
        List<String[]> allData = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length > columnIndex) {
                    allData.add(columns);
                }
            }
        }
        return allData.stream()
                .filter(o -> isNumeric(o[columnIndex])) // Filtert nur numerische Werte
                .sorted((o1, o2) -> Double.compare(Double.parseDouble(o1[columnIndex]),
                        Double.parseDouble(o2[columnIndex])))
                .limit(3)
                .map(o -> String.join(",", o))
                .toList();
    }

    // Hilfsmethode, um zu prüfen, ob ein Wert numerisch ist
    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Teams aus einer Excel-Datei abrufen
    public static List<String> getTeams(String excelFilePath) throws IOException {
        List<String> teams = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(excelFilePath))) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                Cell cell = row.getCell(0); // Annahme: Teamnamen sind in der ersten Spalte
                if (cell != null) {
                    teams.add(cell.toString());
                }
            }
        }
        return teams;
    }
}
