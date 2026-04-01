# ConfigIQ for Airflow & Kafka Connect MVP Plan

## Context anchor

- Goal: ship a small public JetBrains plugin that helps authors catch high-value Airflow DAG and Kafka Connect config mistakes before deploy.
- Explicit constraints:
  - keep the MVP narrow and screenshot-friendly
  - no remote integrations, cluster management, or generic linting framework
  - prefer explicit domain code over a reusable rules engine
- Repository state on 2026-03-31:
  - Gradle Kotlin DSL plugin scaffold already exists
  - target IDE is PyCharm 2025.1 with Python, YAML, and JSON dependencies
  - docs and demo samples already exist
  - there is no meaningful plugin implementation yet beyond metadata
  - the workspace is not a Git repository
- Decision: continue from the existing scaffold and normalize it instead of re-scaffolding.
- Constraint: prefer one coherent, demoable MVP over a generic validation framework.
- Scope cut after sanity checks:
  - Airflow Jinja injection is postponed from v1
  - Kafka Connect regex injection is limited to JSON and YAML in v1

## Product goal

`ConfigIQ for Airflow & Kafka Connect` is a narrow authoring assistant for config-heavy DataOps workflows. It adds domain-aware detection, inspections, quick fixes, and lightweight previews inside JetBrains IDEs.

## Target user

- Data engineers and platform engineers editing Airflow DAGs in Python.
- Data engineers editing Kafka Connect connector configs in JSON, YAML, or `.properties`.
- Early adopter profile: wants inline feedback during authoring, not cluster management.

## MVP scope

### Airflow pack

- Detect likely Airflow DAG contexts inside Python files.
- Detect schedule values in common keyword arguments:
  - `schedule`
  - `schedule_interval`
- Validate cron expressions in string literals.
- Provide a preview action/intention that shows the next few run times for valid cron schedules.
- Keep Airflow authoring help focused on schedules for v1; Jinja injection is postponed.

### Kafka Connect pack

- Detect connector config files by signature keys such as `connector.class`.
- Support JSON, YAML, and `.properties` authoring surfaces for inspections and quick fixes.
- Inject RegExp language into JSON and YAML fields:
  - `topics.regex`
  - `transforms.<alias>.regex`
- Postpone `.properties` regex injection until it is clearly worth the PSI complexity.
- Implement these first inspections:
  - `topics` and `topics.regex` conflict
  - `transforms` aliases declared but required `transforms.<alias>.type` missing
  - suspicious/missing core identity fields: `name`, `connector.class`, `tasks.max`
- Implement a few high-value quick fixes.

### Shared

- Settings page with pack toggles:
  - enable/disable Airflow pack
  - enable/disable Kafka Connect pack
- Concise notifications only when an action needs explicit user feedback.
- Sample files for screenshots and demo flows.
- Automated tests for detection, inspections, quick fixes, and preview helpers.

## Non-goals

- Remote Airflow or Kafka Connect connections.
- REST API clients.
- Cluster browsing or management.
- Full schema coverage for every Airflow scheduling mode.
- Full Kafka Connect connector catalog knowledge.
- Generic YAML/JSON linting beyond the narrow domain rules above.
- Tool window, dashboards, analytics, telemetry, or persistence beyond simple settings.
- Hyper-generic rules engine.

## Not included (because it weakens MVP)

- Airflow Jinja language support in v1.
  - Not included because the first demo already lands with schedule inspection and preview.
- Validation of Kafka SMT semantics beyond alias/type presence.
  - Not included because it quickly becomes connector- and transform-specific framework work.
- Kafka Connect `.properties` regex injection in v1.
  - Not included because JSON/YAML injection already covers the first screenshots and authoring win.
- Automatic cron fixing beyond small syntax cleanups and targeted guidance.
  - Not included because a wrong “smart” fix is worse than a clear warning in v1.
- Multi-IDE compatibility matrix work.
  - Not included because one installable target is enough for first Marketplace delivery.

## Recommended product shape

- Target IDE for MVP: PyCharm 2025.1-based IDEs.
- Reason: Airflow support is core, and PyCharm is the smallest official target with straightforward Python APIs.
- Language dependencies expected:
  - Python support for Airflow pack
  - JSON, Properties, YAML support for Kafka Connect pack
  - IntelliLang postponed unless injection needs it

## Architecture

Prefer explicit domain code with small reusable helpers.

### Packages

- `com.configiq.domain.common`
  - file signatures
  - key-path helpers
  - settings model
  - preview utilities
- `com.configiq.domain.airflow`
  - DAG context detection
  - schedule extraction
  - cron validator
  - Jinja context matcher
- `com.configiq.domain.kconnect`
  - config file signature detection
  - connector key extraction
  - transform alias parsing
  - field conflict logic
- `com.configiq.injection`
  - regex injection for Kafka Connect fields
  - Jinja injection for selected Airflow string contexts
- `com.configiq.inspection`
  - inspection entry points
  - shared problem helpers
- `com.configiq.quickfix`
  - focused quick fixes
- `com.configiq.preview`
  - schedule preview intention and popup text builder
- `com.configiq.settings`
  - persistent state + configurable UI
- `com.configiq.samples`
  - optional helpers if sample metadata is needed

## Data flow / matching flow

### Airflow flow

1. PSI visitor inspects Python call expressions.
2. Match likely DAG constructors or `with DAG(...)` contexts using explicit heuristics.
3. Extract `schedule` / `schedule_interval` string literal values.
4. Validate cron only for plain string cases.
5. If invalid, register inspection problem on the literal.
6. If valid, offer a preview intention using the parsed schedule.
7. Stop there for v1; do not expand into broad Airflow template injection.

### Kafka Connect flow

1. File-level detector identifies candidate config file from keys such as `connector.class`, `tasks.max`, `topics`, `topics.regex`, `transforms`.
2. Per format adapter exposes a common key-value view:
  - JSON object properties
  - YAML top-level scalar mappings
  - `.properties` key-value pairs
3. Domain matcher evaluates:
  - required core fields
  - `topics` vs `topics.regex`
  - `transforms` alias declarations vs `transforms.<alias>.type`
4. Inspections register problems on the most relevant PSI/key element.
5. Quick fixes mutate only the local file and only the minimum needed fields.
6. JSON/YAML regex-capable fields receive injected RegExp language for better editing and highlighting.

## Inspections to implement first

### Airflow

1. Invalid cron expression in `schedule` or `schedule_interval`.
2. Suspicious empty schedule string.

### Kafka Connect

1. `topics` and `topics.regex` both set.
2. Declared transform alias missing `transforms.<alias>.type`.
3. Missing core identity field:
  - `connector.class`
  - `tasks.max`
  - `name`

## Quick fixes to implement first

### Airflow

1. Show preview of next 5 run times for valid cron schedule.
2. Replace empty schedule string with `@daily` only if the literal is empty and the fix is unambiguous.

### Kafka Connect

1. Remove conflicting `topics.regex` when `topics` is present.
2. Remove conflicting `topics` when `topics.regex` is present.
3. Add missing `transforms.<alias>.type=` placeholder.
4. Add missing `tasks.max=1` placeholder.

## Tests to implement first

1. Airflow schedule matcher detects DAG schedule kwargs in Python snippets.
2. Airflow invalid cron inspection highlights bad schedule literals.
3. Airflow preview utility returns the next N run times for a valid cron.
4. Kafka Connect detector identifies JSON/YAML/properties files with connector signatures.
5. Kafka Connect conflict inspection flags `topics` + `topics.regex`.
6. Kafka Connect transform alias inspection flags missing `.type`.
7. Kafka Connect regex injector requests `RegExp` for JSON regex fields.
8. Quick fix tests for:
  - removing conflicting topic field
  - adding missing `transforms.<alias>.type`

## Sample / demo assets

- `samples/airflow/invalid_schedule_dag.py`
- `samples/airflow/jinja_template_dag.py`
- `samples/kafka-connect/conflicting_topics.json`
- `samples/kafka-connect/missing_transform_type.yaml`
- `samples/kafka-connect/minimal-source.properties`
- screenshot placeholder section in `README.md`

## Milestones

### Milestone 1

- Normalize the existing scaffold so it builds cleanly.
- Correct plan/progress docs to match repository reality.
- Keep sample assets and metadata aligned with the demo scope.

### Milestone 2

- Implement minimal detection layer.
- Implement first Airflow cron inspection and preview.

### Milestone 3

- Implement Kafka Connect config adapters, inspections, and first quick fixes.

### Milestone 4

- Add settings, injection, tests, README polish, and stabilization.

## Acceptance criteria

1. A sample Airflow file shows at least one useful warning or preview in the IDE.
2. A sample Kafka Connect config shows at least one useful warning or quick fix.
3. The plugin builds successfully with Gradle.
4. Automated tests cover core matching and inspection behavior.
5. README explains scope, supported contexts, and non-goals plainly.
6. The code remains domain-specific and does not pretend to support unrelated ecosystems.
7. `docs/progress.md` records real decisions, scope cuts, and risks.

## Risks

1. JetBrains PSI differences across JSON/YAML/properties can cause adapter code sprawl.
  - Mitigation: keep a tiny common key-value abstraction and support only flat top-level mappings in v1.
2. Python Airflow detection can drift into semantic analysis.
  - Mitigation: keep to literal kwargs in obvious DAG constructor contexts.
3. Cron parsing library choice may add avoidable dependency complexity.
  - Mitigation: first try a small, well-supported Java cron parser; if integration gets messy, cut to syntax validation plus preview for basic cron only.
4. Jinja injection across Python strings may be costly.
  - Mitigation: postpone Jinja injection and keep the first authoring-help win on Kafka regex injection plus Airflow schedule feedback.
5. Multi-product plugin compatibility can create build and Marketplace friction.
  - Mitigation: target PyCharm first and document the supported IDE honestly.
6. Local build/test verification may be polluted by broken global Gradle or Kotlin caches.
  - Mitigation: run Gradle with a repo-local `-g .gradle-user-home` until the scaffold is stable.
7. Plugin metadata mistakes can block all later work.
  - Mitigation: keep scaffold verification in Milestone 1 and fail fast on `tasks`, `test`, and `verifyPluginProjectConfiguration`.

## Explicitly postponed

- Airflow Jinja injection.
- Optional support for more Airflow scheduling primitives such as timetables.
- Semantic inspection of operator-specific kwargs.
- Connector-class-specific Kafka Connect validation.
- Kafka Connect `.properties` regex injection.
- YAML nested transform structure handling beyond flat key forms.
- Config generation wizards.
- Tool windows and run configurations.
- IntelliJ IDEA compatibility work beyond what PyCharm-first code naturally preserves.
- Marketplace publishing automation.

## Decision-first recommendation

- Build the smallest installable plugin around two screenshot-friendly flows:
  - invalid Airflow cron with preview support
  - broken Kafka Connect transform/topics config with quick fix
- Everything else is secondary and should be cut if it threatens those flows.
