# UML Models – Cardiovascular Health Monitoring System (CHMS)

This directory contains four UML class diagrams modeling the core subsystems of the CHMS. These models are based on the current source code within the `com.alerts`, `com.data_management`, and `com.cardio_generator` packages. Proposed architectural extensions are identified by dashed borders or «proposed» tags.

## Diagrams Overview

| # | Subsystem | File |
| :--- | :--- | :--- |
| 1 | Alert Generation System | `1_alert_generation.svg` |
| 2 | Data Storage System | `2_data_storage.svg` |
| 3 | Patient Identification System | `3_patient_identification.svg` |
| 4 | Data Access Layer | `4_data_access_layer.svg` |

---

## 1. Alert Generation System
**Scope:** `com.alerts`, `com.data_management`

### Core Components
* **AlertGenerator:** The primary engine that evaluates patient records to identify critical conditions.
* **Alert:** An immutable value object containing the patient ID, detected condition, and timestamp.
* **DataStorage & Patient:** The system's source of truth; `DataStorage` provides the patient registry, while `Patient` objects manage time-filtered record sets.

### Design Logic
The `AlertGenerator` is designed to be injected with a `DataStorage` instance at runtime, allowing it to iterate through patients and evaluate historical data. A notable design detail is the type mismatch identified in the current code: `Alert` uses a `String` for IDs, whereas the rest of the system uses `int`. By utilizing the `DataReader` interface, the system remains decoupled—storage logic functions independently of whether data arrives via log files or live network streams.

---

## 2. Data Storage System
**Scope:** `com.data_management`

### Core Components
* **PatientRecord:** A strictly immutable data point with private fields and public getters.
* **DataStorage:** Functions as a "Façade," protecting the internal `patientMap` from direct external modification.
* **RetentionPolicy (Proposed):** A logic layer designed to prevent memory leaks by purging records once they exceed a specific age.

### Design Logic
The storage layer prioritizes data integrity through the immutability of `PatientRecord`. While the current `Patient.getRecords()` implementation uses a linear scan for filtering—sufficient for simulation scales—the architecture is designed to support more complex indexing in the future. The proposed `RetentionPolicy` addresses a critical operational requirement: preventing unbounded memory growth during long-term monitoring sessions.

---

## 3. Patient Identification System
**Scope:** `com.data_management` and proposed extensions

### Core Components
* **IdentityManager (Proposed):** The central controller responsible for resolving raw IDs into validated patient profiles.
* **PatientIdentifier (Proposed):** A specialized lookup tool that interfaces with `DataStorage`.
* **MismatchHandler (Proposed):** A safety mechanism for managing "unknown" IDs that do not match the existing hospital records.

### Design Logic
The existing codebase identifies patients using simple integers. This subsystem introduces a formal identification service layer. By separating the lookup logic (`PatientIdentifier`) from error handling (`MismatchHandler`), the system becomes significantly more robust. If unrecognized IDs are encountered, the `MismatchHandler` ensures the data is quarantined and administrators are notified, preventing silent data loss.

---

## 4. Data Access Layer
**Scope:** `com.cardio_generator` (outputs and generators), `com.data_management`

### Core Components
* **OutputStrategy:** An abstraction that allows generators to remain agnostic of the output medium (Console, File, TCP, or WebSocket).
* **PatientDataGenerator:** An interface for various signal simulators (ECG, Blood Pressure, etc.).
* **HealthDataSimulator:** The entry point that parses CLI arguments and orchestrates the scheduling of data generation tasks.

### Design Logic
The "Strategy" pattern is the foundation of this layer. Because generators interact only with the `OutputStrategy` interface, the simulation logic remains identical regardless of the output destination. `HealthDataSimulator` manages task frequency—ranging from 1-second intervals for ECG data to 2-minute intervals for blood levels. This modularity ensures that new output formats or sensor types can be integrated with zero changes to existing simulation code.
