# ConfigIQ Ops

<!-- public-repo-status -->
> Status: Active JetBrains Marketplace plugin. Issues are open for bugs and focused feature requests.


`ConfigIQ Ops` is the Marketplace-facing name for this plugin project. It is a narrow JetBrains plugin for config-heavy DataOps authoring and helps catch a small set of high-value Airflow DAG and Kafka Connect mistakes before deploy.

The repository name stays `configiq_airflow_kafka_connect`; the published plugin name is `ConfigIQ Ops`.

The current package is built against the JetBrains 2024.1 platform (`since-build 241`) with a Java 17 toolchain.
PyCharm remains the primary Airflow smoke target; other 241-line IDEs need Python support.

## What the plugin does

- validates Airflow schedule strings in obvious DAG contexts
- shows the parsed Airflow schedule result and previews the next 5 runs for valid cron schedules and supported macros
- detects Kafka Connect config files by signature keys
- injects RegExp language into Kafka Connect regex fields in JSON and YAML configs
- flags common Kafka Connect authoring mistakes in JSON, YAML, and `.properties`
- adds targeted quick fixes for the first high-value config errors
- lets users enable or disable the Airflow and Kafka Connect packs independently

## What the plugin does not do

- no Airflow cluster management
- no Kafka Connect cluster management
- no REST client
- no generic YAML or JSON linting framework
- no full connector ecosystem validation
- no giant rules DSL
- no Airflow Jinja injection in v1
- no Kafka Connect `.properties` regex injection in v1

## Supported contexts in v1

### Airflow

- Python files with obvious `DAG(...)` or `with DAG(...)` patterns
- string literal `schedule` / `schedule_interval` values
- schedule-result intention for valid 5-field cron strings and supported macros

### Kafka Connect

- flat top-level JSON config files with connector signature keys
- flat top-level YAML config files with connector signature keys
- flat top-level `.properties` connector config files for inspections and quick fixes
- RegExp injection for `topics.regex` and `transforms.<alias>.regex` in JSON and YAML

## Screenshots

- Capture plan: [docs/marketplace-screenshot-pack.md](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/docs/marketplace-screenshot-pack.md)
- Screenshot output folder: [assets/marketplace/screenshots/README.md](/C:/Users/User/IdeaProjects/configiq_airflow_kafka_connect/assets/marketplace/screenshots/README.md)

## How to run

```powershell
./gradlew.bat -g .gradle-user-home runIde
```

## How to test

```powershell
./gradlew.bat -g .gradle-user-home test --console=plain
./gradlew.bat -g .gradle-user-home buildPlugin --console=plain
./gradlew.bat -g .gradle-user-home verifyPlugin --console=plain
```

## How to publish

- Publishing guide: `docs/publishing.md`
- Current workflow: bump the version, run `test buildPlugin verifyPlugin`, then run `publishPlugin`
- Marketplace page: https://plugins.jetbrains.com/plugin/31040-configiq-ops

## Project links

- Source code: https://github.com/queukat/configiq-airflow-kafka-connect
- Issues: https://github.com/queukat/configiq-airflow-kafka-connect/issues
- EULA: ./docs/EULA.md
- Privacy: ./docs/PRIVACY.md
- Quick Start: ./docs/quick-start.md
- Sample configs: ./samples/README.md

## Roadmap

- add Airflow Jinja injection only in a few obvious templated fields
- add Kafka Connect regex injection for `.properties` when the PSI host path is worth it
- broaden Airflow schedule semantics only after the current schedule-result flow is stable
- improve Marketplace assets and screenshots around the existing demo flows
