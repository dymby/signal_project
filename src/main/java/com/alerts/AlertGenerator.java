package com.alerts;

import com.cardio_generator.outputs.FileOutputStrategy;
import com.cardio_generator.outputs.OutputStrategy;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private DataStorage dataStorage;
    private OutputStrategy outputStrategy;

    private static final String SATURATION = "Saturation";
    private static final String DIASTOLIC = "DiastolicPressure";
    private static final String SYSTOLIC = "SystolicPressure";
    private static final String ECG = "ECG";
    private static final String ALERT = "Alert";

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage, OutputStrategy outputStrategy) {
        this.dataStorage = dataStorage;
        this.outputStrategy = outputStrategy;
    }


    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert} method. This method should define the specific conditions
     * under which an alert will be triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        long now = System.currentTimeMillis();
        long tenMinutesAgo = now - 10 * 60 * 1000;

        List<PatientRecord> allRecords = dataStorage.getRecords(patient.getPatientId(), 0L, now);

        checkBloodPressure(patient, allRecords);
        checkECG(patient, allRecords);
        checkLowBloodSaturation(patient, allRecords);
        checkRapidBloodSaturationDrop(patient, allRecords, tenMinutesAgo, now);
        checkHypotensiveHypoxemia(patient, allRecords);
        checkTriggeredAlert(patient, allRecords);
    }

    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument.
     *
     * @param alert the alert object containing details about the alert condition
     */
    protected void triggerAlert(Alert alert) {
        outputStrategy.output(
                Integer.parseInt(alert.getPatientId()),
                alert.getTimestamp(),
                "Alert",
                alert.getCondition()
        );
    }

    private void checkBloodPressure(Patient patient, List<PatientRecord> records) {
        checkPressureType(patient, records, SYSTOLIC);
        checkPressureType(patient, records, DIASTOLIC);
    }

    private void checkPressureType(Patient patient, List<PatientRecord> records, String label) {

        List<PatientRecord> bloodPressure = filterLabel(records, label);

        // values for Systolic Pressure
        int thresholdMax = 180;
        int thresholdMin = 90;

        if (label.equals(DIASTOLIC)) {
            thresholdMin = 60;
            thresholdMax = 120;
        }

        for (int i = 0; i < bloodPressure.size(); i++) {

            double v = records.get(i).getMeasurementValue();
            if (v > thresholdMax || v < thresholdMin)
                triggerAlert(new Alert(
                        String.valueOf(patient.getPatientId()),
                        "Critical BloodPressure",
                        records.get(i).getTimestamp()
                ));

            if (i < 2) continue;

            double d1 = records.get(i-1).getMeasurementValue() - records.get(i-2).getMeasurementValue();
            double d2 = records.get(i).getMeasurementValue() - records.get(i-1).getMeasurementValue();

            if ((d1 > 10 && d2 > 10) || (d1 < -10 && d2 < -10)) {
                triggerAlert(new Alert(
                        String.valueOf(patient.getPatientId()),
                        "BloodPressure Trend Alert",
                        records.get(i).getTimestamp()
                ));
            }
        }
    }

    private void checkRapidBloodSaturationDrop(Patient patient, List<PatientRecord> records, long startTime, long endTime) {
        List<PatientRecord> recent = filterLabel(records, SATURATION).stream()
                .filter(r -> r.getTimestamp() >= startTime && r.getTimestamp() <= endTime)
                .collect(Collectors.toList());

        for (int i = 1; i < recent.size(); i++) {
            double drop = recent.get(i-1).getMeasurementValue()
                    - recent.get(i).getMeasurementValue();
            if (drop >= 5.0) {
                triggerAlert(new Alert(
                        String.valueOf(patient.getPatientId()),
                        "Rapid Saturation Drop",
                        recent.get(i).getTimestamp()
                ));
            }
        }
    }

    private void checkLowBloodSaturation(Patient patient, List<PatientRecord> records) {
        for (PatientRecord r : filterLabel(records, SATURATION)) {
            if (r.getMeasurementValue() < 92) {
                triggerAlert(new Alert(
                        String.valueOf(patient.getPatientId()),
                        "Low Blood Saturation",
                        r.getTimestamp()
                ));
            }
        }
    }

    private void checkHypotensiveHypoxemia(Patient patient, List<PatientRecord> records) {
        boolean lowSat = filterLabel(records, SATURATION).stream()
                .anyMatch(r -> r.getMeasurementValue() < 92);
        boolean lowPre = filterLabel(records, SYSTOLIC).stream()
                .anyMatch(r -> r.getMeasurementValue() < 90);

        if (lowSat && lowPre) {
            triggerAlert(new Alert(
                    String.valueOf(patient.getPatientId()),
                    "Hypotensive Hypoxemia Alert",
                    System.currentTimeMillis()
            ));
        }
    }

    private void checkECG(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> ecg = filterLabel(records, ECG);
        int windowSize = 10;
        if (ecg.size() <= windowSize) return;

        for (int i = windowSize; i < ecg.size(); i++) {
            double sum = 0;
            for (int j = i - windowSize; j < i; j++) {
                sum += ecg.get(j).getMeasurementValue();
            }
            double avg = sum / windowSize;
            double current = ecg.get(i).getMeasurementValue();
            if (avg > 0 && current > 3 * avg) {
                triggerAlert(new Alert(
                        String.valueOf(patient.getPatientId()),
                        "ECG Abnormal Peak",
                        ecg.get(i).getTimestamp()
                ));
            }
        }
    }

    private void checkTriggeredAlert(Patient patient, List<PatientRecord> records) {
        for (PatientRecord r : filterLabel(records, ALERT)) {
            if (r.getMeasurementValue() == 1.0) {
                triggerAlert(new Alert(
                        String.valueOf(patient.getPatientId()),
                        "Triggered Alert",
                        r.getTimestamp()
                ));
            }
        }
    }

    private List<PatientRecord> filterLabel(List<PatientRecord> records, String label) {
        return records.stream()
                .filter(r -> r.getRecordType().equals(label))
                .collect(Collectors.toList());
    }
}
