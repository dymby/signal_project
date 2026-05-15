package alerts;

import com.alerts.Alert;
import com.alerts.strategies.ECGAlertStrategy;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ECGAlertStrategyTest {

    private ECGAlertStrategy strategy;
    private DataStorage storage;

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        storage = DataStorage.getInstance();
        strategy = new ECGAlertStrategy();
    }

    private Patient getPatient(int id) {
        return storage.getAllPatients().stream()
                .filter(p -> p.getPatientId() == id)
                .findFirst().orElseThrow();
    }

    private List<PatientRecord> records(Patient patient) {
        return storage.getRecords(patient.getPatientId(), 0L, Long.MAX_VALUE);
    }

    @Test
    void testAbnormalPeak_triggersAlert() {
        for (int i = 0; i < 10; i++) {
            storage.addPatientData(1, 1.0, "ECG", 1_000_000L + i * 1000L);
        }
        storage.addPatientData(1, 50.0, "ECG", 1_000_000L + 11_000L);
        Patient p = getPatient(1);
        List<Alert> alerts = strategy.checkAlert(p, records(p));
        assertTrue(alerts.stream().anyMatch(a ->
                a.getCondition().equals("ECG Abnormal Peak")));
    }

    @Test
    void testNormalReadings_noAlert() {
        for (int i = 0; i < 15; i++) {
            storage.addPatientData(1, 1.0, "ECG", 1_000_000L + i * 1000L);
        }
        Patient p = getPatient(1);
        List<Alert> alerts = strategy.checkAlert(p, records(p));
        assertFalse(alerts.stream().anyMatch(a ->
                a.getCondition().equals("ECG Abnormal Peak")));
    }

    @Test
    void testTooFewRecords_noAlert() {
        storage.addPatientData(1, 1.0, "ECG", 1_000_000L);
        storage.addPatientData(1, 50.0, "ECG", 1_001_000L);
        Patient p = getPatient(1);
        List<Alert> alerts = strategy.checkAlert(p, records(p));
        assertTrue(alerts.isEmpty());
    }

    @Test
    void testReturnsEmptyList_notNull() {
        storage.addPatientData(1, 1.0, "ECG", 1_000_000L);
        Patient p = getPatient(1);
        List<Alert> alerts = strategy.checkAlert(p, records(p));
        assertNotNull(alerts);
    }
}