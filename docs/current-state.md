# Current State

## Snapshot

- Date: 2026-05-03
- Current release line: `0.1.5`
- Marketplace plugin: `ConfigIQ Ops`
- Plugin XML id: `com.configiq.airflowkconnect`
- Current stage:
  - MVP is shipped
  - `0.1.5` is prepared locally as a narrow hardening patch
  - compatibility hardening for IntelliJ IDEA is done
  - the project is ready for Stage 2 planning

## What the plugin already does

### Airflow

- Detects obvious `DAG(...)` and `with DAG(...)` contexts in Python files.
- Finds `schedule` and `schedule_interval` string literals.
- Validates supported schedule strings.
- Shows a schedule-result intention with the next 5 runs for valid schedules.
- Reports invalid and suspicious schedule values directly in the editor.

### Kafka Connect

- Detects flat connector configs in JSON, YAML, and `.properties`.
- Highlights:
  - `topics` and `topics.regex` conflicts
  - missing `transforms.<alias>.type`
  - missing `name`
  - missing `connector.class`
  - missing `tasks.max`
- Provides focused quick fixes for the first high-value cases.
- Injects `RegExp` support into JSON, YAML, and `.properties` regex fields.

### Shared plugin behavior

- Exposes pack-level settings for Airflow and Kafka Connect.
- Builds and verifies from Gradle in a repo-local environment.
- Has screenshot/demo samples and Marketplace assets in the repository.
- Has an explicit Marketplace publishing flow documented in `docs/publishing.md`.

## Current quality bar

- Automated tests exist for:
  - Airflow schedule parsing
  - Airflow DAG matching
  - Kafka Connect inspections
  - compatibility descriptor behavior
- Kotlin style and static analysis are wired into Gradle:
  - `ktlintCheck`
  - `detekt`
- `verifyPlugin` passes for:
  - the recommended PyCharm matrix
  - `IntelliJ IDEA Ultimate 261.23567.71`
- Version `0.1.4` was re-uploaded to JetBrains Marketplace as update `1038071`.
- Version `0.1.5` is built and verifier-compatible locally with Kafka Connect `.properties` regex injection.

## What justified 0.1.5

`0.1.5` is a narrow patch because it closes the clearest MVP gap without broadening the product:

- Kafka Connect `.properties` RegExp injection for `topics.regex` and `transforms.<alias>.regex`.
- `ktlintCheck` and `detekt` as documented release gates.
- Release notes and docs updated for the new supported surface.

## Better as `0.2.0` or later

- Broader Airflow schedule semantics.
- Connector-class-specific Kafka Connect validation.
- Airflow Jinja injection.
- Nested YAML or connector-specific model expansion.

## What is intentionally still out of scope

- Airflow Jinja injection
- Airflow timetable or broader scheduler semantics
- connector-class-specific Kafka Connect validation
- nested Kafka Connect config modeling
- cluster management, REST integrations, dashboards, or telemetry

## Current product assessment

The plugin is no longer just a scaffold or internal MVP. It is now a real published narrow authoring assistant with:

- one solid Airflow workflow
- one solid Kafka Connect workflow
- tests around the critical paths
- Marketplace packaging and release flow in place

The main limitation now is not "does it exist?" but "where should it deepen next without losing focus?"

## Recommended next stage

Recommended Stage 2: post-MVP hardening plus one carefully chosen depth increase.

The safest order is:

1. Finish the product-facing polish.
   - tighten screenshots
   - tighten listing copy
   - verify the docs reflect the current shipped behavior
2. Choose exactly one product expansion track.
   - Airflow track:
     - improve schedule coverage a little
     - add one more small, high-signal inspection
   - Kafka Connect track:
     - improve validation depth for a few core fields
3. Keep the plugin narrow.
   - avoid multi-feature expansion in one stage
   - avoid generic framework work unless repeated pain forces it

## Recommended concrete Stage 2 default

If we want the next stage to be low-risk and useful, the default recommendation is:

- Stage 2A: hardening and product polish
  - finalize screenshot set
  - clean up docs so current status, scope, and release flow are easy to read
  - do one more manual IDE pass on the sample pack
  - only after that, choose either the Airflow or Kafka Connect expansion

## Decision question for the next planning pass

The next planning session should answer one thing clearly:

- Do we want the next real product gain to be primarily on the Airflow side or on the Kafka Connect side?

That choice should drive the next implementation stage instead of broadening both packs at once.
