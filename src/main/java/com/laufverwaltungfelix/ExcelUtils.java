package com.laufverwaltungfelix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtils {

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

    // CSV-Datei nach einem bestimmten Wert und Team filtern
    public static List<String[]> filterCSVWithTeam(String csvFilePath, int columnIndex, String filterValue, String team) throws IOException {
        List<String[]> filteredData = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns[columnIndex].equals(filterValue) && columns[8].equals(team)) {
                    filteredData.add(columns);
                }
            }
        }
        return filteredData;
    }

    // CSV-Datei nach einem bestimmten Wert und Altersklasse filtern
    public static List<String[]> filterCSVWithAgeClass(String csvFilePath, int columnIndex, String filterValue, String ageClass) throws IOException {
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

    // Top 3 Einträge in der CSV-Datei basierend auf einem bestimmten Wert zurückgeben
    public static List<String> processAndReturnTop3CSV(String csvFilePath, int columnIndex) throws IOException {
        List<String[]> allData = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                allData.add(columns);
            }
        }
        return allData.stream()
                .sorted(Comparator.comparingDouble(o -> Double.parseDouble(o[columnIndex])))
                .limit(3)
                .map(o -> String.join(",", o))
                .collect(Collectors.toList());
    }

    // Teamnamen aus einer Excel-Datei abrufen
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

    // Häufigstes Team in der CSV-Datei finden
    public static String findMostFrequentTeam(String csvFilePath, int columnIndex, String filterValue) throws IOException {
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

    // Daten in eine CSV-Datei schreiben
    public static void writeDataToCSV(List<String[]> data, String csvFilePath) throws IOException {
        try (FileWriter csvWriter = new FileWriter(csvFilePath)) {
            for (String[] row : data) {
                csvWriter.write(String.join(",", row) + "\n");
            }
        }
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
}
