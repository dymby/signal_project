package com.cardio_generator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.cardio_generator.generators.AlertGenerator;
import com.cardio_generator.generators.BloodPressureDataGenerator;
import com.cardio_generator.generators.BloodSaturationDataGenerator;
import com.cardio_generator.generators.BloodLevelsDataGenerator;
import com.cardio_generator.generators.ECGDataGenerator;
import com.cardio_generator.outputs.*;
import com.data_management.DataReader;
import com.data_management.FileDataReader;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class HealthDataSimulator {

    // ── Singleton ─────────────────────────────────────────────────────────────

    private static HealthDataSimulator instance;

    /**
     * Returns the single instance of HealthDataSimulator.
     * Synchronized to be thread-safe.
     *
     * @return the singleton instance
     */
    public static synchronized HealthDataSimulator getInstance() {
        if (instance == null) {
            instance = new HealthDataSimulator();
        }
        return instance;
    }

    /**
     * Private constructor — prevents direct instantiation.
     * Use {@link #getInstance()} instead.
     */
    private HealthDataSimulator() {
        // default values set on fields below
    }

    private int patientCount = 50;
    private ScheduledExecutorService scheduler;
    private OutputStrategy outputStrategy = new ConsoleOutputStrategy();
    private final Random random = new Random();
    private DataReader dataReader;

    /**
     * Main method — gets the singleton instance and starts the simulation.
     *
     * @param args command-line arguments
     * @throws IOException if the output directory cannot be created
     */
    public static void main(String[] args) throws IOException {
        HealthDataSimulator simulator = HealthDataSimulator.getInstance();
        simulator.start(args);
    }

    /**
     * Parses arguments, initializes patient IDs, and schedules data generation.
     *
     * @param args command-line arguments
     * @throws IOException if the output directory cannot be created
     */
    public void start(String[] args) throws IOException {
        parseArguments(args);

        scheduler = Executors.newScheduledThreadPool(patientCount * 4);

        List<Integer> patientIds = initializePatientIds(patientCount);
        Collections.shuffle(patientIds);

        scheduleTasksForPatients(patientIds);
    }

    private void parseArguments(String[] args) throws IOException {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h":
                    printHelp();
                    System.exit(0);
                    break;
                case "--patient-count":
                    if (i + 1 < args.length) {
                        try {
                            patientCount = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println(
                                    "Error: Invalid number of patients. Using default value: "
                                            + patientCount);
                        }
                    }
                    break;
                case "--output":
                    if (i + 1 < args.length) {
                        String outputArg = args[++i];
                        if (outputArg.equals("console")) {
                            outputStrategy = new ConsoleOutputStrategy();
                        } else if (outputArg.startsWith("file:")) {
                            String baseDirectory = outputArg.substring(5);
                            Path outputPath = Paths.get(baseDirectory);
                            if (!Files.exists(outputPath)) {
                                Files.createDirectories(outputPath);
                            }
                            outputStrategy = new FileOutputStrategy(baseDirectory);
                        } else if (outputArg.startsWith("websocket:")) {
                            try {
                                int port = Integer.parseInt(outputArg.substring(10));
                                outputStrategy = new WebSocketOutputStrategy(port);
                                System.out.println("WebSocket output will be on port: " + port);
                            } catch (NumberFormatException e) {
                                System.err.println(
                                        "Invalid port for WebSocket output. Please specify a valid port number.");
                            }
                        } else if (outputArg.startsWith("tcp:")) {
                            try {
                                int port = Integer.parseInt(outputArg.substring(4));
                                outputStrategy = new TcpOutputStrategy(port);
                                System.out.println("TCP socket output will be on port: " + port);
                            } catch (NumberFormatException e) {
                                System.err.println(
                                        "Invalid port for TCP output. Please specify a valid port number.");
                            }
                        } else {
                            System.err.println("Unknown output type. Using default (console).");
                        }
                    }
                    break;
                case "-outputfile":
                    if (i + 1 < args.length) {
                        String outputPath = args[++i];
                        dataReader = new FileDataReader(outputPath);
                    }
                    break;
                default:
                    System.err.println("Unknown option '" + args[i] + "'");
                    printHelp();
                    System.exit(1);
            }
        }
    }

    private void printHelp() {
        System.out.println("Usage: java HealthDataSimulator [options]");
        System.out.println("Options:");
        System.out.println("  -h                       Show help and exit.");
        System.out.println(
                "  --patient-count <count>  Specify the number of patients to simulate data for (default: 50).");
        System.out.println("  --output <type>          Define the output method. Options are:");
        System.out.println("                             'console' for console output,");
        System.out.println("                             'file:<directory>' for file output,");
        System.out.println("                             'websocket:<port>' for WebSocket output,");
        System.out.println("                             'tcp:<port>' for TCP socket output.");
        System.out.println("Example:");
        System.out.println(
                "  java HealthDataSimulator --patient-count 100 --output websocket:8080");
    }

    private List<Integer> initializePatientIds(int patientCount) {
        List<Integer> patientIds = new ArrayList<>();
        for (int i = 1; i <= patientCount; i++) {
            patientIds.add(i);
        }
        return patientIds;
    }

    private void scheduleTasksForPatients(List<Integer> patientIds) {
        ECGDataGenerator ecgDataGenerator = new ECGDataGenerator(patientCount);
        BloodSaturationDataGenerator bloodSaturationDataGenerator =
                new BloodSaturationDataGenerator(patientCount);
        BloodPressureDataGenerator bloodPressureDataGenerator =
                new BloodPressureDataGenerator(patientCount);
        BloodLevelsDataGenerator bloodLevelsDataGenerator =
                new BloodLevelsDataGenerator(patientCount);
        AlertGenerator alertGenerator = new AlertGenerator(patientCount);

        for (int patientId : patientIds) {
            scheduleTask(() -> ecgDataGenerator.generate(patientId, outputStrategy),
                    1, TimeUnit.SECONDS);
            scheduleTask(() -> bloodSaturationDataGenerator.generate(patientId, outputStrategy),
                    1, TimeUnit.SECONDS);
            scheduleTask(() -> bloodPressureDataGenerator.generate(patientId, outputStrategy),
                    1, TimeUnit.MINUTES);
            scheduleTask(() -> bloodLevelsDataGenerator.generate(patientId, outputStrategy),
                    2, TimeUnit.MINUTES);
            scheduleTask(() -> alertGenerator.generate(patientId, outputStrategy),
                    20, TimeUnit.SECONDS);
        }
    }

    private void scheduleTask(Runnable task, long period, TimeUnit timeUnit) {
        scheduler.scheduleAtFixedRate(task, random.nextInt(5), period, timeUnit);
    }

    /**
     * Resets the singleton instance. For testing purposes only.
     */
    static void resetInstance() {
        instance = null;
    }
}