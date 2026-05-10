# Cardio Data Simulator

The Cardio Data Simulator is a Java-based application designed to simulate real-time cardiovascular data for multiple patients. This tool is particularly useful for educational purposes, enabling students to interact with real-time data streams of ECG, blood pressure, blood saturation, and other cardiovascular signals.

## Features

- Simulate real-time ECG, blood pressure, blood saturation, and blood levels data.
- Supports multiple output strategies:
  * Console output for direct observation.
  * File output for data persistence.
  * WebSocket and TCP output for networked data streaming.
- Configurable patient count and data generation rate.
- Randomized patient ID assignment for simulated data diversity.

## Getting Started

### Prerequisites

- Java JDK 11 or newer.
- Maven for managing dependencies and compiling the application.

### Installation

1. Clone the repository:

    ```
    git clone https://github.com/dymby/signal_project.git
    ```

2. Navigate to the project directory:

    ```
    cd signal_project
    ```

3. Compile and package the application using Maven:

    ```
    mvn clean package
    ```

    This step compiles the source code and packages the application into an executable JAR file located in the `target/` directory.

### Running the Simulator

After packaging, you can run the simulator directly from the executable JAR:

```
java -jar target/cardio_generator-1.0-SNAPSHOT.jar
```

To run with specific options (e.g., to set the patient count and choose an output strategy):

```
java -jar target/cardio_generator-1.0-SNAPSHOT.jar --patient-count 100 --output file:./output
```

### Supported Output Options

- `console`: Directly prints the simulated data to the console.
- `file:<directory>`: Saves the simulated data to files within the specified directory.
- `websocket:<port>`: Streams the simulated data to WebSocket clients connected to the specified port.
- `tcp:<port>`: Streams the simulated data to TCP clients connected to the specified port.

---

## UML Models – Week 2

The [`uml_models/`](./uml_models/) directory contains four UML class diagrams modeling the key subsystems of the Cardiovascular Health Monitoring System (CHMS). These diagrams are grounded in the **actual source code** of this repository. Existing classes are shown with solid borders; proposed extensions use dashed borders and are labeled «proposed».

### Subsystems Modeled

| # | Subsystem | Source packages | Diagram |
|---|-----------|----------------|---------|
| 1 | [Alert Generation System](./uml_models/Alert-Generation-System.png) | `com.alerts`, `com.data_management` | Existing `AlertGenerator`, `Alert`, `DataStorage`, `Patient`, `PatientRecord`, `DataReader` |
| 2 | [Data Storage System](./uml_models/Data-Storage-System.png) | `com.data_management` | Existing `DataStorage`, `Patient`, `PatientRecord`, `DataReader`; proposed `RetentionPolicy`, `FileDataReader` |
| 3 | [Patient Identification System](./uml_models/Patient-Identification.png) | `com.data_management` + proposed | Existing `Patient`, `DataStorage`; proposed `PatientIdentifier`, `IdentityManager`, `MismatchHandler` |
| 4 | [Data Access Layer](./uml_models/Data-Access-Layer.png) | `com.cardio_generator.outputs`, `com.cardio_generator.generators` | All `OutputStrategy` implementations; all `PatientDataGenerator` implementations; `HealthDataSimulator` entry point |

Full design rationale for each diagram is in [`uml_models/uml_readme.md`](./uml_models/uml_readme.md).

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Project Members

- Student ID: 6428735
