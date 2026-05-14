package com.alerts.strategies;

import com.alerts.Alert;
import com.data_management.DataStorage;
import com.data_management.Patient;

import java.util.List;

public interface AlertStrategy {
    /**
     * Checks patient data and returns a list of alerts to trigger,
     * or an empty list if no alert condition is met.
     *
     * @param patient     the patient to evaluate
     * @param dataStorage the storage to read records from
     * @return list of alerts to trigger
     */
    List<Alert> checkAlert(Patient patient, DataStorage dataStorage);
}
