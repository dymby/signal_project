package com.alerts.strategies;

import com.alerts.Alert;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;

public interface AlertStrategy {
    /**
     * Checks patient data and returns a list of alerts to trigger,
     * or an empty list if no alert condition is met.
     *
     * @param patient     the patient to evaluate
     * @param records     the patient's records
     * @return list of alerts to trigger
     */
    List<Alert> checkAlert(Patient patient, List<PatientRecord> records);
}
