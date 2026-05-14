package com.alerts.strategies;

import com.alerts.Alert;
import com.alerts.factories.AlertFactory;
import com.alerts.factories.BloodSaturationAlertFactory;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BloodSaturationAlertStrategy implements AlertStrategy{

    private static final String SATURATION = "Saturation";

    @Override
    public List<Alert> checkAlert(Patient patient, DataStorage dataStorage) {
        long now = System.currentTimeMillis();
        long tenMinutesAgo = now - 10 * 60 * 1000;
        AlertFactory factory = new BloodSaturationAlertFactory();
        List<Alert> alerts = new ArrayList<>();
        String patientIdString = String.valueOf(patient.getPatientId());

        List<PatientRecord> records = dataStorage.getRecords(
                patient.getPatientId(),
                0L,
                now
        );

        List<PatientRecord> filter = records.stream()
                .filter(r -> r.getRecordType().equals(SATURATION))
                .collect(Collectors.toList());

        for (PatientRecord r : filter) {
            if (r.getMeasurementValue() < 92) {
                alerts.add(factory.createAlert(
                        patientIdString,
                        "Low Blood Saturation",
                        r.getTimestamp()
                ));
            }
        }

        List<PatientRecord> recent = filter.stream()
                .filter(r -> r.getTimestamp() < now && r.getTimestamp() > tenMinutesAgo)
                .collect(Collectors.toList());

        for (int i = 1; i < recent.size(); i++) {
            double drop = recent.get(i).getMeasurementValue() - recent.get(i-1).getMeasurementValue();
            if (drop >= 5.0) {
                alerts.add(factory.createAlert(
                        patientIdString,
                        "Rapid Saturation Drop",
                        recent.get(i).getTimestamp()
                ));
            }
        }

        return alerts;
    }

}
