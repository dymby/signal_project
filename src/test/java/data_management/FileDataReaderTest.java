package data_management;

import com.data_management.DataStorage;
import com.data_management.FileDataReader;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileDataReaderTest {

    private Path tempDir;
    private DataStorage storage;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("test_output");
        storage = new DataStorage();
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> p.toFile().delete());
    }

    // helper — writes lines to a .txt file in the temp directory
    private void writeFile(String filename, String... lines) throws IOException {
        Files.write(tempDir.resolve(filename), List.of(lines));
    }

    // helper — reads all records for a patient
    private List<PatientRecord> recordsFor(int patientId) {
        return storage.getRecords(patientId, 0L, Long.MAX_VALUE);
    }


    @Test
    void testReadData_parsesPatientId() throws IOException {
        writeFile("data.txt",
                "Patient ID: 89, Timestamp: 1000, Label: SystolicPressure, Data: 126.0");
        new FileDataReader(tempDir.toString()).readData(storage);
        assertFalse(recordsFor(89).isEmpty());
    }

    @Test
    void testReadData_parsesTimestamp() throws IOException {
        writeFile("data.txt",
                "Patient ID: 1, Timestamp: 5000, Label: SystolicPressure, Data: 120.0");
        new FileDataReader(tempDir.toString()).readData(storage);
        assertEquals(5000L, recordsFor(1).get(0).getTimestamp());
    }

    @Test
    void testReadData_parsesLabel() throws IOException {
        writeFile("data.txt",
                "Patient ID: 1, Timestamp: 1000, Label: DiastolicPressure, Data: 75.0");
        new FileDataReader(tempDir.toString()).readData(storage);
        assertEquals("DiastolicPressure", recordsFor(1).get(0).getRecordType());
    }

    @Test
    void testReadData_parsesNumericValue() throws IOException {
        writeFile("data.txt",
                "Patient ID: 1, Timestamp: 1000, Label: SystolicPressure, Data: 126.0");
        new FileDataReader(tempDir.toString()).readData(storage);
        assertEquals(126.0, recordsFor(1).get(0).getMeasurementValue());
    }

    @Test
    void testReadData_stripsSaturationPercent() throws IOException {
        writeFile("data.txt",
                "Patient ID: 18, Timestamp: 1000, Label: Saturation, Data: 99.0%");
        new FileDataReader(tempDir.toString()).readData(storage);
        assertEquals(99.0, recordsFor(18).get(0).getMeasurementValue());
    }

    @Test
    void testReadData_alertTriggered_encodedAs1() throws IOException {
        writeFile("data.txt",
                "Patient ID: 61, Timestamp: 1000, Label: Alert, Data: triggered");
        new FileDataReader(tempDir.toString()).readData(storage);
        assertEquals(1.0, recordsFor(61).get(0).getMeasurementValue());
    }

    @Test
    void testReadData_alertResolved_encodedAs0() throws IOException {
        writeFile("data.txt",
                "Patient ID: 61, Timestamp: 1000, Label: Alert, Data: resolved");
        new FileDataReader(tempDir.toString()).readData(storage);
        assertEquals(0.0, recordsFor(61).get(0).getMeasurementValue());
    }

    @Test
    void testReadData_multipleLines_allParsed() throws IOException {
        writeFile("data.txt",
                "Patient ID: 1, Timestamp: 1000, Label: SystolicPressure, Data: 120.0",
                "Patient ID: 1, Timestamp: 2000, Label: SystolicPressure, Data: 122.0",
                "Patient ID: 1, Timestamp: 3000, Label: SystolicPressure, Data: 125.0"
        );
        new FileDataReader(tempDir.toString()).readData(storage);
        assertEquals(3, recordsFor(1).size());
    }

    @Test
    void testReadData_multiplePatients_allParsed() throws IOException {
        writeFile("data.txt",
                "Patient ID: 1, Timestamp: 1000, Label: SystolicPressure, Data: 120.0",
                "Patient ID: 2, Timestamp: 1000, Label: Saturation, Data: 97.0"
        );
        new FileDataReader(tempDir.toString()).readData(storage);
        assertFalse(recordsFor(1).isEmpty());
        assertFalse(recordsFor(2).isEmpty());
    }

    @Test
    void testReadData_multipleFiles_allParsed() throws IOException {
        writeFile("file1.txt",
                "Patient ID: 1, Timestamp: 1000, Label: SystolicPressure, Data: 120.0");
        writeFile("file2.txt",
                "Patient ID: 2, Timestamp: 2000, Label: Saturation, Data: 97.0");
        new FileDataReader(tempDir.toString()).readData(storage);
        assertFalse(recordsFor(1).isEmpty());
        assertFalse(recordsFor(2).isEmpty());
    }

    @Test
    void testReadData_ecgValue_parsedCorrectly() throws IOException {
        writeFile("data.txt",
                "Patient ID: 67, Timestamp: 1000, Label: ECG, Data: 0.32641426767704684");
        new FileDataReader(tempDir.toString()).readData(storage);
        assertEquals(0.32641426767704684,
                recordsFor(67).get(0).getMeasurementValue(), 1e-10);
    }

    // ── Parsing: edge cases ───────────────────────────────────────────────────

    @Test
    void testReadData_emptyDirectory_noException() {
        assertDoesNotThrow(() ->
                new FileDataReader(tempDir.toString()).readData(storage));
    }

    @Test
    void testReadData_emptyFile_noException() throws IOException {
        writeFile("empty.txt");
        assertDoesNotThrow(() ->
                new FileDataReader(tempDir.toString()).readData(storage));
    }

    @Test
    void testReadData_blankLines_ignored() throws IOException {
        writeFile("data.txt",
                "",
                "Patient ID: 1, Timestamp: 1000, Label: SystolicPressure, Data: 120.0",
                ""
        );
        new FileDataReader(tempDir.toString()).readData(storage);
        assertEquals(1, recordsFor(1).size());
    }

    @Test
    void testReadData_malformedLine_skipped() throws IOException {
        writeFile("data.txt", "THIS IS NOT VALID");
        assertDoesNotThrow(() ->
                new FileDataReader(tempDir.toString()).readData(storage));
        assertTrue(storage.getAllPatients().isEmpty());
    }

    @Test
    void testReadData_mixedValidAndMalformed_validLinesParsed() throws IOException {
        writeFile("data.txt",
                "GARBAGE LINE",
                "Patient ID: 3, Timestamp: 5000, Label: ECG, Data: 0.8"
        );
        new FileDataReader(tempDir.toString()).readData(storage);
        assertFalse(recordsFor(3).isEmpty());
    }

    @Test
    void testReadData_nonTxtFilesIgnored() throws IOException {
        // Write a .csv file — should not be read
        Files.write(tempDir.resolve("data.csv"), List.of(
                "Patient ID: 1, Timestamp: 1000, Label: SystolicPressure, Data: 120.0"
        ));
        new FileDataReader(tempDir.toString()).readData(storage);
        assertTrue(storage.getAllPatients().isEmpty());
    }
}