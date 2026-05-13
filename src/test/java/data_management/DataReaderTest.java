package data_management;

import com.data_management.DataStorage;
import com.data_management.DataReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class DataReaderTest implements DataReader{


    @Override
    public void readData(DataStorage dataStorage) throws IOException {

        try {
            InputStreamReader reader = new FileReader(new File("src/test/resources/MockInput.txt"));
            Scanner scanner = new Scanner(reader);
                
            while (scanner.hasNextLine()) {
                int patientId = scanner.nextInt();
                double measurementValue = scanner.nextDouble();
                long timestamp = scanner.nextLong();
                String recordType = scanner.next();
                dataStorage.addPatientData(patientId, measurementValue, recordType, timestamp);
                }

            scanner.close();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
