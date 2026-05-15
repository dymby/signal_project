package alerts;

import com.alerts.Alert;
import com.alerts.strategies.BloodSaturationAlertStrategy;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BloodSaturationStrategyTest {

    private BloodSaturationAlertStrategy strategy;
    private DataStorage storage;

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        storage = DataStorage.getInstance();
        strategy = new BloodSaturationAlertStrategy();
    }

    private Patient addAndGet(int id, double value, String label, long timestamp) {
        storage.addPatientData(id, value, label, timestamp);
        return storage.getAllPatients().stream()
                .filter(p -> p.getPatientId() == id)
                .findFirst().orElseThrow();
    }

    private List<PatientRecord> records(Patient patient) {
        return storage.getRecords(patient.getPatientId(), 0L, Long.MAX_VALUE);
    }

    @Test
    void testLowSaturation_triggersAlert() {
        Patient p = addAndGet(1, 91.0, "Saturation", 1_000_000L);
        List<Alert> alerts = strategy.checkAlert(p, records(p));
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().equals("Low Blood Saturation")));
    }

    @Test
    void testNormalSaturation_noAlert() {
        Patient p = addAndGet(1, 98.0, "Saturation", 1_000_000L);
        List<Alert> alerts = strategy.checkAlert(p, records(p));
        assertFalse(alerts.stream().anyMatch(a -> a.getCondition().equals("Low Blood Saturation")));
    }

    @Test
    void testRapidDrop_within10min_triggersAlert() {
        long now = System.currentTimeMillis();
        storage.addPatientData(1, 97.0, "Saturation", now - 300_000L);
        storage.addPatientData(1, 91.0, "Saturation", now - 60_000L);
        Patient p = storage.getAllPatients().stream()
                .filter(pa -> pa.getPatientId() == 1).findFirst().orElseThrow();
        List<Alert> alerts = strategy.checkAlert(p, records(p));
        assertTrue(alerts.stream().anyMatch(a ->
                a.getCondition().equals("Rapid Saturation Drop")));
    }

    @Test
    void testReturnsEmptyList_notNull() {
        Patient p = addAndGet(2, 98.0, "Saturation", 1_000_000L);
        List<Alert> alerts = strategy.checkAlert(p, records(p));
        assertNotNull(alerts);
    }
}