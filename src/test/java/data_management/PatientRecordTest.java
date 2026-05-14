package data_management;

import com.data_management.PatientRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatientRecordTest {

    @Test
    void testGetPatientId() {
        PatientRecord record = new PatientRecord(5, 120.0, "SystolicPressure", 1000L);
        assertEquals(5, record.getPatientId());
    }

    @Test
    void testGetMeasurementValue() {
        PatientRecord record = new PatientRecord(1, 98.6, "Saturation", 1000L);
        assertEquals(98.6, record.getMeasurementValue());
    }

    @Test
    void testGetRecordType() {
        PatientRecord record = new PatientRecord(1, 75.0, "DiastolicPressure", 1000L);
        assertEquals("DiastolicPressure", record.getRecordType());
    }

    @Test
    void testGetTimestamp() {
        PatientRecord record = new PatientRecord(1, 75.0, "DiastolicPressure", 9999L);
        assertEquals(9999L, record.getTimestamp());
    }

    @Test
    void testConstructor_zeroValue() {
        PatientRecord record = new PatientRecord(1, 0.0, "ECG", 1000L);
        assertEquals(0.0, record.getMeasurementValue());
    }

    @Test
    void testConstructor_negativeValue() {
        // Some ECG readings can be negative
        PatientRecord record = new PatientRecord(1, -0.5, "ECG", 1000L);
        assertEquals(-0.5, record.getMeasurementValue());
    }
}