package com.alerts.strategies;


import com.alerts.Alert;
import com.alerts.factories.AlertFactory;
import com.alerts.factories.BloodPressureAlertFactory;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BloodPressureAlertStrategy implements AlertStrategy {

    private static final String SYSTOLIC = "SystolicPressure";
    private static final String DIASTOLIC = "DiastolicPressure";

    @Override
    public List<Alert> checkAlert(Patient patient, List<PatientRecord> records) {
        List<Alert> alerts = new ArrayList<>();

        checkPressure(patient, records, SYSTOLIC, 180, 90, alerts);
        checkPressure(patient, records, DIASTOLIC, 120, 60, alerts);

        return alerts;
    }

    private void checkPressure(Patient patient, List<PatientRecord> records, String label, int max, int min, List<Alert> alerts) {
        List<PatientRecord> filtered = records.stream()
                .filter(r -> r.getRecordType().equals(label))
                .collect(Collectors.toList());

        AlertFactory factory = new BloodPressureAlertFactory();
        String patientId = String.valueOf(patient.getPatientId());

        for (int i = 0; i < filtered.size(); i++) {
            double v = records.get(i).getMeasurementValue();
            long timestamp = records.get(i).getTimestamp();

            if (v > max || v < min) {
                alerts.add(factory.createAlert(
                        patientId,
                        "Critical BloodPressure",
                        timestamp
                ));
            }

            if (i < 2) continue;

            double d1 = records.get(i-1).getMeasurementValue() - records.get(i-2).getMeasurementValue();
            double d2 = records.get(i).getMeasurementValue() - records.get(i-1).getMeasurementValue();

            if ((d1 > 10 && d2 > 10) || (d1 < -10 && d2 < -10)) {
                alerts.add(factory. createAlert(
                        patientId,
                        "BloodPressure Trend Alert",
                        timestamp
                ));
            }
        }
    }

}
