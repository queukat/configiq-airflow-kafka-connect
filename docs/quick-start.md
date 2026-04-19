# Quick Start

## Context anchor

- Goal: get `ConfigIQ Ops` running quickly and verify the main Airflow and Kafka Connect flows in one short pass.
- Explicit constraints:
  - target IDE line is JetBrains 2024.1 / build 241
  - PyCharm is still the easiest smoke target for the Airflow slice
  - use the bundled samples in this repository
  - focus on the MVP flows that are already implemented

## 1. Build the plugin

From the repository root:

```powershell
./gradlew.bat -g .gradle-user-home buildPlugin --console=plain
```

Publishable ZIP output:

- `build/distributions/configiq-airflow-kafka-connect-<pluginVersion>.zip`

## 2. Run the sandbox IDE locally

```powershell
./gradlew.bat -g .gradle-user-home runIde
```

This starts a 241-line PyCharm-based sandbox IDE with `ConfigIQ Ops` installed.

## 3. Open the sample files

Open these files inside the sandbox IDE:

- [invalid_cron_schedule_dag.py](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/airflow/invalid_cron_schedule_dag.py)
- [invalid_empty_schedule_interval_dag.py](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/airflow/invalid_empty_schedule_interval_dag.py)
- [valid_preview_dag.py](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/airflow/valid_preview_dag.py)
- [invalid_conflicting_topics.json](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/kafka-connect/invalid_conflicting_topics.json)
- [invalid_missing_transform_type.yaml](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/kafka-connect/invalid_missing_transform_type.yaml)
- [invalid_missing_tasks_max.properties](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/kafka-connect/invalid_missing_tasks_max.properties)
- [invalid_missing_identity_fields.yaml](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/kafka-connect/invalid_missing_identity_fields.yaml)
- [happy_path_regex.yaml](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/kafka-connect/happy_path_regex.yaml)

## 4. Verify the main flows

### Airflow

- In [invalid_cron_schedule_dag.py](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/airflow/invalid_cron_schedule_dag.py), confirm that the broken cron string is highlighted.
- In [invalid_empty_schedule_interval_dag.py](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/airflow/invalid_empty_schedule_interval_dag.py), confirm that the empty schedule warning is highlighted.
- In [valid_preview_dag.py](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/airflow/valid_preview_dag.py), place the caret inside the schedule string and run the intention:
  - `Show Airflow schedule result`

### Kafka Connect

- In [invalid_conflicting_topics.json](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/kafka-connect/invalid_conflicting_topics.json), confirm the `topics` / `topics.regex` conflict and inspect the quick fixes.
- In [invalid_missing_transform_type.yaml](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/kafka-connect/invalid_missing_transform_type.yaml), confirm the missing `transforms.<alias>.type` warning.
- In [invalid_missing_tasks_max.properties](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/kafka-connect/invalid_missing_tasks_max.properties), confirm the missing required field warning and quick fix.
- In [invalid_missing_identity_fields.yaml](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/kafka-connect/invalid_missing_identity_fields.yaml), confirm the missing `name` and `connector.class` warnings and quick fixes.
- In [happy_path_regex.yaml](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/kafka-connect/happy_path_regex.yaml), place the caret inside the regex value and confirm `RegExp` language support is active.

## 5. Verify pack settings

Open:

- `Settings | Tools | ConfigIQ`

Check:

- `Enable Airflow authoring pack`
- `Enable Kafka Connect authoring pack`

Disable one pack at a time and confirm the corresponding inspections stop firing on the sample files.

## 6. Run release checks

```powershell
./gradlew.bat -g .gradle-user-home test --console=plain
./gradlew.bat -g .gradle-user-home verifyPlugin --console=plain
```

Expected result:

- tests pass
- Plugin Verifier reports `Compatible` for the configured IDE matrix

## Not included

- Marketplace ZIP signing.
  - Not included because it requires external signing credentials.
- Real screenshot capture.
  - Not included because actual image capture is done from a live IDE session.
