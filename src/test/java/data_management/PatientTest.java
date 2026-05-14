package data_management;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PatientTest {

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient(1);
        patient.addRecord(120.0, "SystolicPressure", 100L);
        patient.addRecord(122.0, "SystolicPressure", 200L);
        patient.addRecord(125.0, "SystolicPressure", 300L);
        patient.addRecord(130.0, "SystolicPressure", 400L);
        patient.addRecord(128.0, "SystolicPressure", 500L);
    }

    @Test
    void testGetPatientId_returnsCorrectId() {
        assertEquals(1, patient.getPatientId());
    }

    @Test
    void testGetPatientId_differentPatients_differentIds() {
        Patient other = new Patient(42);
        assertEquals(42, other.getPatientId());
        assertNotEquals(patient.getPatientId(), other.getPatientId());
    }

    @Test
    void testGetRecords_returnsRecordsWithinRange() {
        List<PatientRecord> result = patient.getRecords(200L, 400L);
        assertEquals(3, result.size());
    }

    @Test
    void testGetRecords_includesStartBoundary() {
        List<PatientRecord> result = patient.getRecords(100L, 200L);
        assertTrue(result.stream().anyMatch(r -> r.getTimestamp() == 100L));
    }

    @Test
    void testGetRecords_includesEndBoundary() {
        List<PatientRecord> result = patient.getRecords(400L, 500L);
        assertTrue(result.stream().anyMatch(r -> r.getTimestamp() == 500L));
    }

    @Test
    void testGetRecords_allRecordsInRange() {
        List<PatientRecord> result = patient.getRecords(0L, 1000L);
        assertEquals(5, result.size());
    }

    @Test
    void testGetRecords_correctMeasurementValue() {
        List<PatientRecord> result = patient.getRecords(100L, 100L);
        assertEquals(120.0, result.get(0).getMeasurementValue());
    }

    @Test
    void testGetRecords_correctRecordType() {
        List<PatientRecord> result = patient.getRecords(100L, 100L);
        assertEquals("SystolicPressure", result.get(0).getRecordType());
    }

    @Test
    void testGetRecords_correctPatientId() {
        List<PatientRecord> result = patient.getRecords(100L, 100L);
        assertEquals(1, result.get(0).getPatientId());
    }

    @Test
    void testGetRecords_emptyWhenNoRecordsInRange() {
        List<PatientRecord> result = patient.getRecords(600L, 1000L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetRecords_emptyPatient_returnsEmptyList() {
        Patient empty = new Patient(99);
        List<PatientRecord> result = empty.getRecords(0L, 1000L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetRecords_startEqualsEnd_matchesExact() {
        List<PatientRecord> result = patient.getRecords(300L, 300L);
        assertEquals(1, result.size());
        assertEquals(300L, result.get(0).getTimestamp());
    }

    @Test
    void testGetRecords_startEqualsEnd_noMatch() {
        List<PatientRecord> result = patient.getRecords(250L, 250L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetRecords_doesNotReturnNull() {
        List<PatientRecord> result = patient.getRecords(999L, 9999L);
        assertNotNull(result);
    }

    @Test
    void testGetRecords_multipleTypes_allReturnedInRange() {
        patient.addRecord(95.0, "Saturation", 350L);
        List<PatientRecord> result = patient.getRecords(300L, 400L);
        assertTrue(result.stream().anyMatch(r -> r.getRecordType().equals("Saturation")));
        assertTrue(result.stream().anyMatch(r -> r.getRecordType().equals("SystolicPressure")));
    }

    @Test
    void testGetRecords_doesNotModifyOriginalList() {
        List<PatientRecord> result1 = patient.getRecords(0L, 1000L);
        result1.clear();
        List<PatientRecord> result2 = patient.getRecords(0L, 1000L);
        assertEquals(5, result2.size());
    }
}