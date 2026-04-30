package com.cardio_generator.generators;

import java.util.Random;
import com.cardio_generator.outputs.OutputStrategy;

public class AlertGenerator implements PatientDataGenerator {
    // switched to UPPER_SNAKE_CASE
    public static final Random RANDOM_GENERATOR = new Random();
    // switched to lowerCaseCamel
    private boolean[] alertStates; // false = resolved, true = pressed

    public AlertGenerator(int patientCount) {
        alertStates = new boolean[patientCount + 1];
    }

    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) {
                if (RANDOM_GENERATOR.nextDouble() < 0.9) { // 90% chance to resolve
                    alertStates[patientId] = false;
                    // Output the alert
                    // solved line length
                    outputStrategy.output(patientId, System.currentTimeMillis(),
                            "Alert", "resolved");
                }
            } else {
                // to lowerCamelCase
                double lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                double p = -Math.expm1(-lambda); // Probability of at least one alert in the period
                boolean alertTriggered = RANDOM_GENERATOR.nextDouble() < p;

                if (alertTriggered) {
                    alertStates[patientId] = true;
                    // Output the alert
                    // solved line length issues
                    outputStrategy.output(patientId, System.currentTimeMillis(),
                            "Alert", "triggered");
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
