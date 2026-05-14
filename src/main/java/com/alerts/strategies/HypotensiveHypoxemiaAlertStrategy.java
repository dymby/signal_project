package com.alerts.strategies;

import com.alerts.Alert;
import com.alerts.factories.AlertFactory;
import com.alerts.factories.HypotensiveHypoxemiaAlertFactory;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HypotensiveHypoxemiaAlertStrategy implements AlertStrategy {

    private static final String SATURATION = "Saturation";
    private static final String SYSTOLIC = "SystolicPressure";

    @Override
    public List<Alert> checkAlert(Patient patient, List<PatientRecord> records) {
        AlertFactory factory = new HypotensiveHypoxemiaAlertFactory();
        List<Alert> alerts = new ArrayList<>();

        boolean lowSat = records.stream()
                .filter(r ->r.getRecordType().equals(SATURATION))
                .anyMatch(r -> r.getMeasurementValue() < 92);
        boolean lowPre = records.stream()
                .filter(r -> r.getRecordType().equals(SYSTOLIC))
                .anyMatch(r -> r.getMeasurementValue() < 90);
        if (lowSat && lowPre) {
            alerts.add(factory.createAlert(
                    String.valueOf(patient.getPatientId()),
                    "Hypotensive Hypoxemia Alert",
                    System.currentTimeMillis()
            ));
        }

        return alerts;
    }
}
