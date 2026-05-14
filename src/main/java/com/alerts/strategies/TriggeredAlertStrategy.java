package com.alerts.strategies;

import com.alerts.Alert;
import com.alerts.factories.AlertFactory;
import com.alerts.factories.TriggeredAlertFactory;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TriggeredAlertStrategy implements AlertStrategy{

    private static final String ALERT = "Alert";

    @Override
    public List<Alert> checkAlert(Patient patient, List<PatientRecord> recordList) {
        AlertFactory factory = new TriggeredAlertFactory();
        List<Alert> alertList = new ArrayList<>();
        List<PatientRecord> filter = recordList.stream()
                .filter(r -> r.getRecordType().equals(ALERT))
                .collect(Collectors.toList());

        for (PatientRecord r : filter) {
            if (r.getMeasurementValue() == 1.0) {
                alertList.add(factory.createAlert(
                        String.valueOf(patient.getPatientId()),
                        "Triggered Alert",
                        r.getTimestamp()
                ));
            }
        }

        return alertList;
    }
}
