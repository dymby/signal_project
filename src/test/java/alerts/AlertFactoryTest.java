package alerts;

import com.alerts.Alert;
import com.alerts.factories.BloodPressureAlertFactory;
import com.alerts.factories.BloodSaturationAlertFactory;
import com.alerts.factories.ECGAlertFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlertFactoryTest {

    @Test
    void testBloodPressureFactory_createsAlert() {
        Alert alert = new BloodPressureAlertFactory()
                .createAlert("1", "Critical SystolicPressure", 1000L);
        assertNotNull(alert);
    }

    @Test
    void testBloodPressureFactory_correctPatientId() {
        Alert alert = new BloodPressureAlertFactory()
                .createAlert("1", "Critical SystolicPressure", 1000L);
        assertEquals("1", alert.getPatientId());
    }

    @Test
    void testBloodPressureFactory_correctCondition() {
        Alert alert = new BloodPressureAlertFactory()
                .createAlert("1", "Critical SystolicPressure", 1000L);
        assertEquals("Critical SystolicPressure", alert.getCondition());
    }

    @Test
    void testBloodPressureFactory_correctTimestamp() {
        Alert alert = new BloodPressureAlertFactory()
                .createAlert("1", "Critical SystolicPressure", 1000L);
        assertEquals(1000L, alert.getTimestamp());
    }

    @Test
    void testBloodOxygenFactory_createsAlert() {
        Alert alert = new BloodSaturationAlertFactory()
                .createAlert("2", "Low Saturation", 2000L);
        assertNotNull(alert);
    }

    @Test
    void testBloodOxygenFactory_correctCondition() {
        Alert alert = new BloodSaturationAlertFactory()
                .createAlert("2", "Low Saturation", 2000L);
        assertEquals("Low Saturation", alert.getCondition());
    }

    @Test
    void testECGFactory_createsAlert() {
        Alert alert = new ECGAlertFactory()
                .createAlert("3", "ECG Abnormal Peak", 3000L);
        assertNotNull(alert);
    }

    @Test
    void testECGFactory_correctCondition() {
        Alert alert = new ECGAlertFactory()
                .createAlert("3", "ECG Abnormal Peak", 3000L);
        assertEquals("ECG Abnormal Peak", alert.getCondition());
    }
}