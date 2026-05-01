package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@code FileOutputStrategy} class is used for creating a directory, creating
 * files for each label of data from a hash map and writing the correct data their.
 * It is based on a string of directory, creating it and writing the labeled files,
 * data is labeled using {@link ConcurrentHashMap}.
 */

public class FileOutputStrategy implements OutputStrategy {
    // to lowerCamelCase
    private String baseDirectory;

    // from snake_case to lowerCamelCase
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();

    /**
     * Constructs a {@code FileOutputStrategy} with a specified {@code baseDirectory},
     * acting as the output directory where the created files will be added, label wise.
     *
     * @param baseDirectory identifier of where this directory to be created and where
     *                      the user can find the files
     */
    public FileOutputStrategy(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    /**
     * Constructs the directory, the files and writes them with their designated
     * file. This is done using an {@link ConcurrentHashMap} that caches labels
     * and filePath pairs and writes them based on their label value.
     *
     * @param patientId the unique identifier of the patient whose data is recorded
     * @param timestamp the Unix epoch time (in milliseconds) at which the measurement
     *                  was taken
     * @param label     the category or type of measurement, determining the output
     *                  file
     * @param data      the measurement value or payload to record
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // Set the FilePath variable; to lowerCamelCase
        String filePath = fileMap.computeIfAbsent(label,
                k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Write the data to the file
        // updated the line by applying the rules for 100 characters length
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(Paths.get(filePath),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n",
                    patientId, timestamp, label, data);
            // catching IOException instead of Exception
        } catch (IOException e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}