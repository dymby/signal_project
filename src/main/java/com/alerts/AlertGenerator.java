package com.alerts;

import com.alerts.factories.*;
import com.alerts.strategies.*;
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
        List<PatientRecord> records = dataStorage.getRecords(patient.getPatientId(), 0L, now);

        List<AlertStrategy> strategies = List.of(
                new BloodPressureAlertStrategy(),
                new BloodSaturationAlertStrategy(),
                new ECGAlertStrategy(),
                new HypotensiveHypoxemiaAlertStrategy(),
                new TriggeredAlertStrategy()
        );

        for (AlertStrategy strategy : strategies) {
            for (Alert alert : strategy.checkAlert(patient, records)) {
                triggerAlert(alert);
            }
        }
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

}
