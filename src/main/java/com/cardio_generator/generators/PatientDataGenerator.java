package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Common interface for all patient data generators.
 */
public interface PatientDataGenerator {

    /**
     * Generates and outputs a data sample for a given patient
     *
     * @param patientId         the ID of the patient to generate the data for
     * @param outputStrategy    the output destination for the generated data
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
