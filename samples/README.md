# Sample Configs

These files are the demo and smoke-test fixtures for `ConfigIQ Ops`.

## Broken Airflow demos

- [invalid_cron_schedule_dag.py](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/airflow/invalid_cron_schedule_dag.py)
  - broken 4-field cron string inside `DAG(...)`
- [invalid_empty_schedule_interval_dag.py](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/airflow/invalid_empty_schedule_interval_dag.py)
  - empty legacy `schedule_interval` value

## Broken Kafka Connect demos

- [invalid_conflicting_topics.json](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/kafka-connect/invalid_conflicting_topics.json)
  - `topics` conflicts with `topics.regex`
- [invalid_missing_transform_type.yaml](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/kafka-connect/invalid_missing_transform_type.yaml)
  - declared transforms are missing `transforms.<alias>.type`
- [invalid_missing_tasks_max.properties](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/kafka-connect/invalid_missing_tasks_max.properties)
  - missing required `tasks.max` field
- [invalid_missing_identity_fields.yaml](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/kafka-connect/invalid_missing_identity_fields.yaml)
  - missing required `name` and `connector.class` fields

## Happy-path demos

- [valid_preview_dag.py](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/airflow/valid_preview_dag.py)
  - next-run preview screenshot
- [happy_path_regex.yaml](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/kafka-connect/happy_path_regex.yaml)
  - regex editing screenshot with no conflict noise
- [jinja_template_dag.py](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/samples/airflow/jinja_template_dag.py)
  - explicit non-goal sample for current MVP

## Recommended smoke order

1. invalid Airflow cron schedule
2. empty `schedule_interval`
3. valid Airflow preview
4. JSON topic conflict
5. YAML missing transform type
6. properties missing `tasks.max`
7. YAML missing `name` and `connector.class`
8. regex editing support
