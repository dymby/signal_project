package com.cardio_generator.generators;

import java.util.List;
import java.util.Random;
import com.cardio_generator.outputs.OutputStrategy;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

/**
 * Generates alert data for patients, simulating triggered and resolved alert
 * events. Alert resolution has a 90% probability per cycle; triggering is
 * modelled with a Poisson process using a configurable lambda value.
 */
public class AlertGenerator implements PatientDataGenerator {
    // switched to UPPER_SNAKE_CASE
    public static final Random RANDOM_GENERATOR = new Random();
    // switched to lowerCaseCamel
    private boolean[] alertStates; // false = resolved, true = pressed

    /**
     * Initializes the {@code AlertGenerator} object with a given patients count
     *
     * @param patientCount  the number of patients the {@code alertStates} holds
     */
    public AlertGenerator(int patientCount) {
        alertStates = new boolean[patientCount + 1];
    }

    /**
     * Generates an alert event for the given patient. If an alert is currently active,
     * there is a 90% chance it resolves. Otherwise, a new alert may be triggered based
     * on a Poisson probability model.
     *
     * @param patientId      the ID of the patient to generate data for
     * @param outputStrategy the output destination for the generated alert event
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) {
                if (RANDOM_GENERATOR.nextDouble() < 0.9) { // 90% chance to resolve
                    alertStates[patientId] = false;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                // to lowerCamelCase
                double lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                double p = -Math.expm1(-lambda); // Probability of at least one alert in the period
                boolean alertTriggered = RANDOM_GENERATOR.nextDouble() < p;

                if (alertTriggered) {
                    alertStates[patientId] = true;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            // solved line length issues
            System.err.println("An error occurred while generating alert data for patient " +
                    patientId);
            e.printStackTrace();
        }
    }
}
