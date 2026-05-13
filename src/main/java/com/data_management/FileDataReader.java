package com.data_management;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.util.Scanner;

public class FileDataReader implements DataReader {

    String filePath;

    FileDataReader(String filePath) {
        this.filePath = filePath;
    }

    public void readData(DataStorage dataStorage) throws IOException {
        // firstly checking if the file actually exists
        File file = new File(filePath);
        if (!file.canRead() || file.exists()) {
            throw new IOException("Invalid file path: " + filePath);
        }

        try {
            InputStreamReader reader = new FileReader(new File(filePath));
            Scanner scanner = new Scanner(reader);

            while (scanner.hasNextLine()) {
                int patientId = scanner.nextInt();
                double measurementValue = scanner.nextDouble();
                long timestamp = scanner.nextLong();
                String recordType = scanner.next();

                dataStorage.addPatientData(patientId, measurementValue, recordType, timestamp);
            }

            scanner.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
