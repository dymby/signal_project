package alerts;

import com.alerts.Alert;
import com.alerts.BaseAlert;
import com.alerts.strategies.BloodPressureAlertStrategy;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BloodPressureStrategyTest {

    private BloodPressureAlertStrategy strategy;
    private Patient patient;
    private DataStorage storage;

    private static final long T1 = 1_000_000L;
    private static final long T2 = T1 + 60_000L;
    private static final long T3 = T2 + 60_000L;

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        storage = DataStorage.getInstance();
        strategy = new BloodPressureAlertStrategy();
        storage.addPatientData(1, 120.0, "SystolicPressure", T1);
        patient = storage.getAllPatients().get(0);
    }

    private List<PatientRecord> records() {
        return storage.getRecords(patient.getPatientId(), 0L, Long.MAX_VALUE);
    }

    @Test
    void testIncreasingTrend_triggersAlert() {
        DataStorage.resetInstance();
        storage = DataStorage.getInstance();
        storage.addPatientData(1, 100.0, "SystolicPressure", T1);
        storage.addPatientData(1, 115.0, "SystolicPressure", T2);
        storage.addPatientData(1, 130.0, "SystolicPressure", T3);
        patient = storage.getAllPatients().get(0);
        List<Alert> alerts = strategy.checkAlert(patient, records());
        assertTrue(alerts.stream().anyMatch(a ->
                a.getCondition().equals("BloodPressure Trend Alert")));
    }

    @Test
    void testCriticalHigh_triggersAlert() {
        DataStorage.resetInstance();
        storage = DataStorage.getInstance();
        storage.addPatientData(1, 185.0, "SystolicPressure", T1);
        patient = storage.getAllPatients().get(0);
        List<Alert> alerts = strategy.checkAlert(patient, records());
        assertTrue(alerts.stream().anyMatch(a ->
                a.getCondition().equals("Critical BloodPressure")));
    }

    @Test
    void testNormalReadings_noAlerts() {
        List<Alert> alerts = strategy.checkAlert(patient, records());
        assertTrue(alerts.isEmpty());
    }

    @Test
    void testReturnsEmptyList_notNull() {
        List<Alert> alerts = strategy.checkAlert(patient, records());
        assertNotNull(alerts);
    }
}