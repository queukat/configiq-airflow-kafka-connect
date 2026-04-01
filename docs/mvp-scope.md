# MVP Scope

## In

- JetBrains plugin MVP for authoring help only
- PyCharm 2025.1-based target for the first shippable build
- Airflow Python schedule detection in obvious DAG contexts
- Airflow cron validation
- Airflow schedule preview for valid cron strings
- Kafka Connect config detection in JSON, YAML, and `.properties`
- Kafka Connect RegExp injection in JSON and YAML for `topics.regex` and `transforms.<alias>.regex`
- Kafka Connect inspections for:
  - `topics` vs `topics.regex`
  - missing `transforms.<alias>.type`
  - missing core identity fields
- A few direct quick fixes
- Pack-level settings toggles
- Samples, tests, and README

## Out

- Remote Airflow/Kafka Connect access
- Cluster state, task state, or deployment management
- REST clients
- Generic config linting
- Connector-specific deep validation
- Full Airflow scheduler semantics
- Airflow Jinja injection
- Kafka Connect `.properties` regex injection
- Tool windows
- Analytics or telemetry

## Demo promise

1. Open the sample Airflow DAG.
2. See an invalid schedule warning or run a preview on a valid one.
3. Open the sample Kafka Connect config.
4. See regex-aware editing in JSON or YAML plus a conflict or missing transform problem.
5. Apply a quick fix directly in the editor.

## Scope guard

If a proposed feature does not improve one of the demo steps above, it is postponed.
