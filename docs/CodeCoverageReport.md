# Test Coverage Status

**Current Coverage:** ~90% (Core Logic & Alerting)

### Component Breakdown
| Area | Tested Classes |
| :--- | :--- |
| **Data Mgmt** | `DataStorage`, `Patient`, `PatientRecord`, `FileDataReader` |
| **Alerts** | `AlertGenerator`, `BaseAlert`, `*AlertFactory`, `*AlertStrategy`, `*AlertDecorator` |

### Untested Components (Rationale)
*   **External I/O:** `HealthDataSimulator` and all `OutputStrategy` types (File, WebSocket, TCP, Console) were skipped. These involve live filesystem/network dependencies and threading that require integration infra rather than unit tests.
*   **Internal Tools:** `resetInstance()` methods are package-private utilities for test isolation and are excluded from production metrics.

### Key Logic Assumptions
*   **State Mapping:** `FileDataReader` handles binary states as doubles (`1.0` for triggered, `0.0` for resolved) to fit the `DataStorage` numeric requirement.
*   **Decorator Usage:** Decorators are applied manually by the caller to maintain compatibility with existing string-based alert tests.
*   **Alert Format:** Condition IDs are passed as plain text in the `OutputStrategy` `data` field.