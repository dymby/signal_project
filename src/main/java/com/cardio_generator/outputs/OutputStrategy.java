package com.cardio_generator.outputs;

/**
 * Common interface for all output classes.
 */
public interface OutputStrategy {

    /**
     * Outputs the data sample for the given patient.
     *
     * @param patientId the ID of the patient the data belongs to
     * @param timestamp the time of the measurement in milliseconds
     * @param label     the type of measurement (e.g {@code "ECG"}
     * @param data      the measurement value
     */
    void output(int patientId, long timestamp, String label, String data);
}
