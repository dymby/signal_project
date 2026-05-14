package com.data_management;

import java.io.*;
import java.util.Scanner;

public class FileDataReader implements DataReader {

    String directory;

    public FileDataReader(String filePath) {
        this.directory = filePath;
    }

    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        File dir = new File(directory);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files == null) return;

        for (File file : files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    parseLine(line, dataStorage);
                }
            }
        }
    }

    private void parseLine(String line, DataStorage dataStorage) {
        try {
            int patientId = Integer.parseInt(extractValue(line, "Patient ID"));
            long timestamp = Long.parseLong(extractValue(line, "Timestamp"));
            String label = extractValue(line, "Label");
            String rawData = extractValue(line, "Data");

            rawData = rawData.replace("%", "").trim();

            double value;
            if (rawData.equals("triggered")) {
                value = 1.0;
            } else if (rawData.equals("resolved")) {
                value = 0.0;
            } else {
                value = Double.parseDouble(rawData);
            }

            dataStorage.addPatientData(patientId, value, label, timestamp);

        } catch (IllegalArgumentException e) {
            // skip malformed lines
        }
    }

    private String extractValue(String line, String key) {
        String search = key + ":";
        int start = line.indexOf(search);
        if (start == -1) throw new IllegalArgumentException("Key not found: " + key);
        start += search.length();

        int end = line.length();
        for (int i = start; i < line.length() - 2; i++) {
            if (line.charAt(i) == ',' && line.charAt(i+1) == ' '
                    && Character.isUpperCase(line.charAt(i+2))) {
                end = i;
                break;
            }
        }
        return line.substring(start, end).trim();
    }
}
