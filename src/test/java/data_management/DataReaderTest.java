package data_management;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import com.data_management.DataReader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class DataReaderTest implements DataReader{

    @Override
    public void readData(DataStorage dataStorage) throws IOException {

        Scanner scanner = new Scanner(new File("src/test/resources/MockInput.txt"));

        while (scanner.hasNextLine()) {
            int patientId = scanner.nextInt();
            double measurementValue = scanner.nextDouble();
            long timestamp = scanner.nextLong();
            String recordType = scanner.next();

            dataStorage.addPatientData(patientId, measurementValue, recordType, timestamp);
        }
    }
}
