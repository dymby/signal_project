package com.alerts.strategies;

import com.alerts.Alert;
import com.alerts.BaseAlert;
import com.alerts.factories.AlertFactory;
import com.alerts.factories.ECGAlertFactory;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ECGAlertStrategy implements AlertStrategy{

    private static final String ECG = "ECG";

    @Override
    public List<Alert> checkAlert(Patient patient, List<PatientRecord> records) {
        int windowSize = 10;
        List<Alert> alerts = new ArrayList<>();
        AlertFactory factory = new ECGAlertFactory();

        List<PatientRecord> filter = records.stream()
                .filter(r -> r.getRecordType().equals(ECG))
                .collect(Collectors.toList());

        if (filter.size() <= windowSize) return alerts;

        for (int i = windowSize; i < filter.size(); i++) {
            double sum = 0;

            for (int j = i - windowSize; j < i; j++) {
                sum += filter.get(j).getMeasurementValue();
            }

            double avg = sum / windowSize;
            double currentValue = filter.get(i).getMeasurementValue();

            if (avg > 0 && currentValue > avg * 3) {
                alerts.add(factory.createAlert(
                        String.valueOf(patient.getPatientId()),
                        "ECG Abnormal Peak",
                        filter.get(i).getTimestamp()
                ));
            }
        }

        return alerts;
    }
}
