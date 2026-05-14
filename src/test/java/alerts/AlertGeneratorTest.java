package alerts;

import com.alerts.AlertGenerator;
import com.cardio_generator.outputs.OutputStrategy;
import com.data_management.DataStorage;
import com.data_management.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlertGeneratorTest {

    /**
     * Captures every call to OutputStrategy.output() so we can assert on it.
     * This tests the full pipeline including triggerAlert() itself.
     */
    static class CapturingOutputStrategy implements OutputStrategy {

        static class Entry {
            final int patientId;
            final long timestamp;
            final String label;
            final String data;

            Entry(int patientId, long timestamp, String label, String data) {
                this.patientId = patientId;
                this.timestamp = timestamp;
                this.label = label;
                this.data = data;
            }
        }

        List<Entry> outputs = new ArrayList<>();

        @Override
        public void output(int patientId, long timestamp, String label, String data) {
            outputs.add(new Entry(patientId, timestamp, label, data));
        }

        boolean hasCondition(String condition) {
            return outputs.stream().anyMatch(e -> e.data.equals(condition));
        }

        boolean isEmpty() {
            return outputs.isEmpty();
        }
    }

    private DataStorage storage;
    private CapturingOutputStrategy outputStrategy;
    private AlertGenerator generator;

    // Fixed timestamps 1 minute apart
    private static final long T1 = 1_000_000L;
    private static final long T2 = T1 + 60_000L;
    private static final long T3 = T2 + 60_000L;
    private static final long T4 = T3 + 60_000L;

    @BeforeEach
    void setUp() {
        storage = new DataStorage();
        outputStrategy = new CapturingOutputStrategy();
        generator = new AlertGenerator(storage, outputStrategy);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void add(int patientId, double value, String label, long timestamp) {
        storage.addPatientData(patientId, value, label, timestamp);
    }

    private Patient patient(int patientId) {
        return storage.getAllPatients().stream()
                .filter(p -> p.getPatientId() == patientId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Patient not found: " + patientId));
    }

    // ── triggerAlert output format ────────────────────────────────────────────

    @Test
    void testTriggerAlert_outputLabelIsAlert() {
        add(1, 185.0, "SystolicPressure", T1);
        generator.evaluateData(patient(1));
        assertTrue(outputStrategy.outputs.stream()
                .allMatch(e -> e.label.equals("Alert")));
    }

    @Test
    void testTriggerAlert_outputPatientIdIsCorrect() {
        add(42, 185.0, "SystolicPressure", T1);
        generator.evaluateData(patient(42));
        assertTrue(outputStrategy.outputs.stream()
                .allMatch(e -> e.patientId == 42));
    }

    @Test
    void testTriggerAlert_outputConditionIsData() {
        add(1, 185.0, "SystolicPressure", T1);
        generator.evaluateData(patient(1));
        assertTrue(outputStrategy.hasCondition("Critical BloodPressure"));
    }

    // ── Blood Pressure: Trend ─────────────────────────────────────────────────

    @Test
    void testBP_systolicIncreasingTrend_triggersAlert() {
        add(1, 100.0, "SystolicPressure", T1);
        add(1, 115.0, "SystolicPressure", T2); // +15
        add(1, 130.0, "SystolicPressure", T3); // +15
        generator.evaluateData(patient(1));
        assertTrue(outputStrategy.hasCondition("BloodPressure Trend Alert"));
    }

    @Test
    void testBP_systolicDecreasingTrend_triggersAlert() {
        add(1, 160.0, "SystolicPressure", T1);
        add(1, 145.0, "SystolicPressure", T2); // -15
        add(1, 130.0, "SystolicPressure", T3); // -15
        generator.evaluateData(patient(1));
        assertTrue(outputStrategy.hasCondition("BloodPressure Trend Alert"));
    }

    @Test
    void testBP_systolicSmallChanges_noTrendAlert() {
        // Changes of only 5 — below the 10 mmHg threshold
        add(1, 120.0, "SystolicPressure", T1);
        add(1, 125.0, "SystolicPressure", T2);
        add(1, 130.0, "SystolicPressure", T3);
        generator.evaluateData(patient(1));
        assertFalse(outputStrategy.hasCondition("BloodPressure Trend Alert"));
    }

    @Test
    void testBP_systolicMixedDirection_noTrendAlert() {
        add(1, 110.0, "SystolicPressure", T1);
        add(1, 125.0, "SystolicPressure", T2); // +15
        add(1, 112.0, "SystolicPressure", T3); // -13 — direction flips
        generator.evaluateData(patient(1));
        assertFalse(outputStrategy.hasCondition("BloodPressure Trend Alert"));
    }

    @Test
    void testBP_diastolicIncreasingTrend_triggersAlert() {
        add(1, 70.0, "DiastolicPressure", T1);
        add(1, 85.0, "DiastolicPressure", T2); // +15
        add(1, 100.0,"DiastolicPressure", T3); // +15
        generator.evaluateData(patient(1));
        assertTrue(outputStrategy.hasCondition("BloodPressure Trend Alert"));
    }

    @Test
    void testBP_diastolicDecreasingTrend_triggersAlert() {
        add(1, 100.0, "DiastolicPressure", T1);
        add(1, 85.0,  "DiastolicPressure", T2); // -15
        add(1, 70.0,  "DiastolicPressure", T3); // -15
        generator.evaluateData(patient(1));
        assertTrue(outputStrategy.hasCondition("BloodPressure Trend Alert"));
    }

    @Test
    void testBP_onlyTwoReadings_noTrendAlert() {
        // Need at least 3 readings for a trend
        add(1, 100.0, "SystolicPressure", T1);
        add(1, 115.0, "SystolicPressure", T2);
        generator.evaluateData(patient(1));
        assertFalse(outputStrategy.hasCondition("BloodPressure Trend Alert"));
    }

    // ── Blood Pressure: Critical Threshold ───────────────────────────────────

    @Test
    void testBP_systolicAbove180_triggersAlert() {
        add(1, 185.0, "SystolicPressure", T1);
        generator.evaluateData(patient(1));
        assertTrue(outputStrategy.hasCondition("Critical BloodPressure"));
    }

    @Test
    void testBP_systolicBelow90_triggersAlert() {
        add(1, 85.0, "SystolicPressure", T1);
        generator.evaluateData(patient(1));
        assertTrue(outputStrategy.hasCondition("Critical BloodPressure"));
    }

    @Test
    void testBP_systolicExactly180_noAlert() {
        add(1, 180.0, "SystolicPressure", T1);
        generator.evaluateData(patient(1));
        assertFalse(outputStrategy.hasCondition("Critical BloodPressure"));
    }

    @Test
    void testBP_systolicExactly90_noAlert() {
        add(1, 90.0, "SystolicPressure", T1);
        generator.evaluateData(patient(1));
        assertFalse(outputStrategy.hasCondition("Critical BloodPressure"));
    }

    @Test
    void testBP_systolicNormal_noAlert() {
        add(1, 120.0, "SystolicPressure", T1);
        generator.evaluateData(patient(1));
        assertFalse(outputStrategy.hasCondition("Critical BloodPressure"));
    }

    @Test
    void testBP_diastolicAbove120_triggersAlert() {
        add(1, 125.0, "DiastolicPressure", T1);
        generator.evaluateData(patient(1));
        assertTrue(outputStrategy.hasCondition("Critical BloodPressure"));
    }

    @Test
    void testBP_diastolicBelow60_triggersAlert() {
        add(1, 55.0, "DiastolicPressure", T1);
        generator.evaluateData(patient(1));
        assertTrue(outputStrategy.hasCondition("Critical BloodPressure"));
    }

    @Test
    void testBP_diastolicExactly120_noAlert() {
        add(1, 120.0, "DiastolicPressure", T1);
        generator.evaluateData(patient(1));
        assertFalse(outputStrategy.hasCondition("Critical BloodPressure"));
    }

    @Test
    void testBP_diastolicExactly60_noAlert() {
        add(1, 60.0, "DiastolicPressure", T1);
        generator.evaluateData(patient(1));
        assertFalse(outputStrategy.hasCondition("Critical BloodPressure"));
    }

    @Test
    void testBP_diastolicNormal_noAlert() {
        add(1, 80.0, "DiastolicPressure", T1);
        generator.evaluateData(patient(1));
        assertFalse(outputStrategy.hasCondition("Critical BloodPressure"));
    }

    // ── Saturation: Low ───────────────────────────────────────────────────────

    @Test
    void testSaturation_below92_triggersAlert() {
        add(1, 91.0, "Saturation", T1);
        generator.evaluateData(patient(1));
        assertTrue(outputStrategy.hasCondition("Low Blood Saturation"));
    }

    @Test
    void testSaturation_exactly92_noAlert() {
        add(1, 92.0, "Saturation", T1);
        generator.evaluateData(patient(1));
        assertFalse(outputStrategy.hasCondition("Low Blood Saturation"));
    }

    @Test
    void testSaturation_above92_noAlert() {
        add(1, 98.0, "Saturation", T1);
        generator.evaluateData(patient(1));
        assertFalse(outputStrategy.hasCondition("Low Blood Saturation"));
    }

    // ── Saturation: Rapid Drop ────────────────────────────────────────────────

    @Test
    void testSaturation_rapidDropOf5_within10min_triggersAlert() {
        long now = System.currentTimeMillis();
        add(1, 97.0, "Saturation", now - 300_000L); // 5 min ago
        add(1, 92.0, "Saturation", now - 60_000L);  // 1 min ago — drop of 5
        generator.evaluateData(patient(1));
        assertTrue(outputStrategy.hasCondition("Rapid Saturation Drop"));
    }

    @Test
    void testSaturation_rapidDropOver5_within10min_triggersAlert() {
        long now = System.currentTimeMillis();
        add(1, 97.0, "Saturation", now - 300_000L);
        add(1, 91.0, "Saturation", now - 60_000L);  // drop of 6
        generator.evaluateData(patient(1));
        assertTrue(outputStrategy.hasCondition("Rapid Saturation Drop"));
    }

    @Test
    void testSaturation_dropOf4_noAlert() {
        long now = System.currentTimeMillis();
        add(1, 97.0, "Saturation", now - 300_000L);
        add(1, 93.0, "Saturation", now - 60_000L);  // drop of 4 — under threshold
        generator.evaluateData(patient(1));
        assertFalse(outputStrategy.hasCondition("Rapid Saturation Drop"));
    }

    @Test
    void testSaturation_rapidDrop_outsideWindow_noAlert() {
        long now = System.currentTimeMillis();
        // Both readings are older than 10 minutes — outside window
        add(1, 97.0, "Saturation", now - 900_000L);
        add(1, 91.0, "Saturation", now - 800_000L);
        generator.evaluateData(patient(1));
        assertFalse(outputStrategy.hasCondition("Rapid Saturation Drop"));
    }

    // ── Combined: Hypotensive Hypoxemia ──────────────────────────────────────

    @Test
    void testHypotensiveHypoxemia_bothConditions_triggersAlert() {
        add(1, 85.0, "SystolicPressure", T1); // systolic < 90
        add(1, 90.0, "Saturation",       T2); // saturation < 92
        generator.evaluateData(patient(1));
        assertTrue(outputStrategy.hasCondition("Hypotensive Hypoxemia Alert"));
    }

    @Test
    void testHypotensiveHypoxemia_onlyLowBP_noAlert() {
        add(1, 85.0, "SystolicPressure", T1);
        add(1, 97.0, "Saturation",       T2); // normal saturation
        generator.evaluateData(patient(1));
        assertFalse(outputStrategy.hasCondition("Hypotensive Hypoxemia Alert"));
    }

    @Test
    void testHypotensiveHypoxemia_onlyLowSaturation_noAlert() {
        add(1, 120.0, "SystolicPressure", T1); // normal BP
        add(1, 88.0,  "Saturation",       T2);
        generator.evaluateData(patient(1));
        assertFalse(outputStrategy.hasCondition("Hypotensive Hypoxemia Alert"));
    }

    @Test
    void testHypotensiveHypoxemia_bothNormal_noAlert() {
        add(1, 120.0, "SystolicPressure", T1);
        add(1, 98.0,  "Saturation",       T2);
        generator.evaluateData(patient(1));
        assertFalse(outputStrategy.hasCondition("Hypotensive Hypoxemia Alert"));
    }

    // ── ECG: Abnormal Peak ────────────────────────────────────────────────────

    @Test
    void testECG_hugePeak_triggersAlert() {
        // 10 baseline readings of 1.0, then a spike of 50.0 (50x average)
        for (int i = 0; i < 10; i++) {
            add(1, 1.0, "ECG", T1 + i * 1000L);
        }
        add(1, 50.0, "ECG", T1 + 11_000L);
        generator.evaluateData(patient(1));
        assertTrue(outputStrategy.hasCondition("ECG Abnormal Peak"));
    }

    @Test
    void testECG_uniformReadings_noAlert() {
        for (int i = 0; i < 15; i++) {
            add(1, 1.0, "ECG", T1 + i * 1000L);
        }
        generator.evaluateData(patient(1));
        assertFalse(outputStrategy.hasCondition("ECG Abnormal Peak"));
    }

    @Test
    void testECG_fewerThanWindowSize_noAlert() {
        // Only 5 readings — window of 10 not met, so no alert possible
        for (int i = 0; i < 5; i++) {
            add(1, 1.0, "ECG", T1 + i * 1000L);
        }
        add(1, 50.0, "ECG", T1 + 6_000L);
        generator.evaluateData(patient(1));
        assertFalse(outputStrategy.hasCondition("ECG Abnormal Peak"));
    }

    @Test
    void testECG_slightlyElevated_noAlert() {
        // Peak is only 2x average — below the 3x threshold
        for (int i = 0; i < 10; i++) {
            add(1, 1.0, "ECG", T1 + i * 1000L);
        }
        add(1, 2.0, "ECG", T1 + 11_000L);
        generator.evaluateData(patient(1));
        assertFalse(outputStrategy.hasCondition("ECG Abnormal Peak"));
    }

    // ── Triggered (Button) Alert ──────────────────────────────────────────────

    @Test
    void testTriggeredAlert_triggered_encodedAs1_triggersAlert() {
        // FileDataReader encodes "triggered" as 1.0
        add(1, 1.0, "Alert", T1);
        generator.evaluateData(patient(1));
        assertTrue(outputStrategy.hasCondition("Triggered Alert"));
    }

    @Test
    void testTriggeredAlert_resolved_encodedAs0_noAlert() {
        // FileDataReader encodes "resolved" as 0.0
        add(1, 0.0, "Alert", T1);
        generator.evaluateData(patient(1));
        assertFalse(outputStrategy.hasCondition("Triggered Alert"));
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    void testEvaluateData_noRecords_noAlerts() {
        add(1, 120.0, "SystolicPressure", T1); // one normal reading
        generator.evaluateData(patient(1));
        assertTrue(outputStrategy.isEmpty());
    }

    @Test
    void testEvaluateData_multiplePatients_alertsIsolated() {
        // Patient 1 has critical BP, patient 2 is normal
        add(1, 185.0, "SystolicPressure", T1);
        add(2, 120.0, "SystolicPressure", T1);
        generator.evaluateData(patient(1));
        generator.evaluateData(patient(2));

        // Only patient 1 should have triggered an alert
        assertTrue(outputStrategy.outputs.stream()
                .filter(e -> e.data.equals("Critical BloodPressure"))
                .allMatch(e -> e.patientId == 1));
    }

    @Test
    void testEvaluateData_usesDataStorage_notPatientDirectly() {
        // Add data only via DataStorage — evaluateData must read from there
        storage.addPatientData(1, 185.0, "SystolicPressure", T1);
        Patient p = patient(1);
        generator.evaluateData(p);
        assertTrue(outputStrategy.hasCondition("Critical BloodPressure"));
    }
}