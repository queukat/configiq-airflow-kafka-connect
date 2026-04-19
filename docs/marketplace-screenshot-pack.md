# Marketplace Screenshot Pack

## Context anchor

- Goal: prepare 4-6 screenshot-ready scenes for the JetBrains Marketplace listing of `ConfigIQ Ops`.
- Explicit constraints:
  - only use implemented MVP flows
  - keep each screenshot focused on one user-visible value
  - prefer clean, readable editor crops over crowded full-window shots

## Recommended set

### 1. Airflow invalid cron warning

- Output file:
  - `assets/marketplace/screenshots/01-airflow-invalid-cron.png`
- Open:
  - [invalid_cron_schedule_dag.py](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/airflow/invalid_cron_schedule_dag.py)
- Capture:
  - the `schedule="0 12 * *"` warning underline and tooltip
- User-visible value:
  - obvious Airflow schedule mistakes are caught inline

### 2. Airflow next-run preview

- Output file:
  - `assets/marketplace/screenshots/02-airflow-preview.png`
- Open:
  - [valid_preview_dag.py](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/airflow/valid_preview_dag.py)
- Capture:
  - caret inside the schedule string plus the `Show Airflow schedule result` popup
- User-visible value:
  - valid schedules get a quick preview without leaving the editor

### 3. Kafka Connect JSON conflict + quick fix

- Output file:
  - `assets/marketplace/screenshots/03-kconnect-json-conflict.png`
- Open:
  - [invalid_conflicting_topics.json](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/kafka-connect/invalid_conflicting_topics.json)
- Capture:
  - `topics` / `topics.regex` conflict and the lightbulb quick fix menu
- User-visible value:
  - conflicting connector config fields are detected and fixable

### 4. Kafka Connect YAML missing transform type

- Output file:
  - `assets/marketplace/screenshots/04-kconnect-yaml-transform-type.png`
- Open:
  - [invalid_missing_transform_type.yaml](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/kafka-connect/invalid_missing_transform_type.yaml)
- Capture:
  - missing `transforms.<alias>.type` warning
- User-visible value:
  - transform wiring mistakes are visible in YAML configs

### 5. Kafka Connect regex editing support

- Output file:
  - `assets/marketplace/screenshots/05-kconnect-regex-injection.png`
- Open:
  - [happy_path_regex.yaml](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/kafka-connect/happy_path_regex.yaml)
- Capture:
  - caret inside `topics.regex` or `transforms.routeTopic.regex` with RegExp support active
- User-visible value:
  - regex-heavy config fields are easier to edit safely

### 6. Pack settings toggles

- Output file:
  - `assets/marketplace/screenshots/06-settings-toggles.png`
- Open:
  - `Settings | Tools | ConfigIQ`
- Capture:
  - both pack toggles in one clean settings panel view
- User-visible value:
  - users can keep only the relevant domain pack enabled

## Capture notes

- Use the default light theme for the first Marketplace pass.
- Keep line numbers visible.
- Prefer 1 strong warning or popup per screenshot.
- Avoid placeholder files that do not trigger the intended behavior.
- Crop tightly enough that the editor content remains readable on the Marketplace page.

## Suggested order on the Marketplace page

1. Airflow invalid cron
2. Airflow preview
3. Kafka JSON conflict + quick fix
4. Kafka YAML missing transform type
5. Kafka regex injection
6. Settings toggles

## Not included

- Actual `.png` image files.
  - Not included because capturing live IDE screenshots requires a GUI session.
