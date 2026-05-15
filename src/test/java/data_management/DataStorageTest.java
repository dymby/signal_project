package data_management;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataStorageTest {

    private DataStorage storage;

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        storage = DataStorage.getInstance();
    }

    @Test
    void testAddAndRetrieve_singleRecord() {
        storage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        List<PatientRecord> records = storage.getRecords(1, 0L, 2000L);
        assertEquals(1, records.size());
    }

    @Test
    void testAddAndRetrieve_correctValue() {
        storage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        List<PatientRecord> records = storage.getRecords(1, 0L, 2000L);
        assertEquals(120.0, records.get(0).getMeasurementValue());
    }

    @Test
    void testAddAndRetrieve_correctLabel() {
        storage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        List<PatientRecord> records = storage.getRecords(1, 0L, 2000L);
        assertEquals("SystolicPressure", records.get(0).getRecordType());
    }

    @Test
    void testAddAndRetrieve_correctTimestamp() {
        storage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        List<PatientRecord> records = storage.getRecords(1, 0L, 2000L);
        assertEquals(1000L, records.get(0).getTimestamp());
    }

    @Test
    void testAddAndRetrieve_multipleRecords_samePatient() {
        storage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 122.0, "SystolicPressure", 2000L);
        List<PatientRecord> records = storage.getRecords(1, 0L, 5000L);
        assertEquals(2, records.size());
    }

    @Test
    void testGetRecords_filtersByTimeRange() {
        storage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 125.0, "SystolicPressure", 5000L);
        List<PatientRecord> records = storage.getRecords(1, 0L, 3000L);
        assertEquals(1, records.size());
        assertEquals(120.0, records.get(0).getMeasurementValue());
    }

    @Test
    void testGetRecords_differentPatients_isolated() {
        storage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        storage.addPatientData(2, 200.0, "SystolicPressure", 1000L);
        List<PatientRecord> p1 = storage.getRecords(1, 0L, 5000L);
        List<PatientRecord> p2 = storage.getRecords(2, 0L, 5000L);
        assertEquals(1, p1.size());
        assertEquals(1, p2.size());
        assertEquals(120.0, p1.get(0).getMeasurementValue());
        assertEquals(200.0, p2.get(0).getMeasurementValue());
    }

    @Test
    void testGetRecords_unknownPatient_returnsEmptyList() {
        List<PatientRecord> records = storage.getRecords(999, 0L, Long.MAX_VALUE);
        assertNotNull(records);
        assertTrue(records.isEmpty());
    }

    @Test
    void testGetRecords_samePatientAddedTwice_notDuplicated() {
        storage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 122.0, "SystolicPressure", 2000L);
        assertEquals(1, storage.getAllPatients().size());
    }

    @Test
    void testGetAllPatients_returnsAllPatients() {
        storage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        storage.addPatientData(2, 98.0,  "Saturation",       1000L);
        assertEquals(2, storage.getAllPatients().size());
    }

    @Test
    void testGetAllPatients_emptyStorage_returnsEmptyList() {
        assertTrue(storage.getAllPatients().isEmpty());
    }

    @Test
    void testGetAllPatients_doesNotReturnNull() {
        assertNotNull(storage.getAllPatients());
    }
}