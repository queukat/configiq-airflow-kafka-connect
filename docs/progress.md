# Progress Log

## Milestone 1: Planning and scaffold normalization

### Repository inspection

- Date: 2026-03-31
- Workspace path: `C:\Users\User\IdeaProjects\configiq_airflow_kafka_connect`
- Findings:
  - Gradle-based JetBrains plugin scaffold already exists
  - target IDE is PyCharm 2025.1 with Python, YAML, and JSON dependencies
  - docs and sample files already exist
  - `src/main` only contains plugin metadata and a constant holder
  - `src/test/kotlin` exists but has no test files
  - no `.git` directory
  - Java 21 is available locally
  - the project must use the Gradle wrapper
- Decision:
  - continue from the existing scaffold
  - keep the first supported target narrow instead of chasing multi-product support
  - treat scaffold normalization as Milestone 1 work because the current state is not build-honest

### А не фигню ли я делаю?
## Sanity check
- Current step: inspect repository state and decide whether to normalize or re-scaffold
- User-visible value: avoids throwing away usable scaffold work or building on false assumptions
- Why this still fits MVP: the fastest path is the smallest path that reaches a real demoable plugin
- Simpler alternative I considered: ignore the existing scaffold and start over from a blank project
- What I am deliberately NOT building right now: plugin code, build logic abstractions, IDE compatibility matrix
- Decision: continue

### Planning artifacts created

- `docs/plan.md`
- `docs/progress.md`
- `docs/mvp-scope.md`

### А не фигню ли я делаю?
## Sanity check
- Current step: write the plan and scope boundaries before production code
- User-visible value: makes the MVP promise concrete and keeps future screenshots, README, and code aligned
- Why this still fits MVP: planning here reduces scope drift later, especially around "framework-first" mistakes
- Simpler alternative I considered: keep the plan in comments or issue-style notes
- What I am deliberately NOT building right now: generic rule engines, connector catalogs, remote integrations
- Decision: continue

### Current risks

- The exact JetBrains platform dependency set still needs to be validated by a successful local build.
- Cross-language injection may need a narrower implementation than the initial plan.

### Next smallest useful step

- Verify the scaffold with the Gradle wrapper and correct any build-blocking metadata mistakes before feature work.

## Milestone 2: Build verification and blocker removal

### А не фигню ли я делаю?
## Sanity check
- Current step: verify the existing scaffold and remove the smallest blockers to a real build
- User-visible value: turns a plausible scaffold into an actually runnable plugin project
- Why this still fits MVP: without a working scaffold, every later inspection or quick fix is fake progress
- Simpler alternative I considered: start implementing domain logic before the build is healthy
- What I am deliberately NOT building right now: generic parser infrastructure, Marketplace publishing pipeline, multi-module split
- Decision: continue

### Build verification findings

- `git status` confirms the workspace is not under Git.
- Global Gradle state is unreliable:
  - `./gradlew.bat test --console=plain` failed with a Kotlin Gradle plugin classloading error caused by broken cache state outside the workspace.
- Repo-local Gradle state works better:
  - `./gradlew.bat -g .gradle-user-home tasks --all --console=plain` succeeded.
  - `./gradlew.bat -g .gradle-user-home test --console=plain --info` reached project configuration and compilation.
- Current build blocker:
  - `:patchPluginXml` fails because `src/main/resources/META-INF/plugin.xml` contains an unescaped `&` in the plugin name.

### А не фигню ли я делаю?
## Sanity check
- Current step: fix the smallest metadata issue that blocks all build and test work
- User-visible value: makes the scaffold honest and unblocks every later milestone
- Why this still fits MVP: this is mandatory plumbing, not scope expansion
- Simpler alternative I considered: ignore the error and proceed with feature code anyway
- What I am deliberately NOT building right now: custom build conventions, CI, release automation
- Decision: continue

### Current risks

- `test` also shows a non-fatal Kotlin daemon access issue under `C:\Users\User\AppData\Local\kotlin\daemon`; it falls back to out-of-process compilation, but it may slow the loop.
- No production feature code exists yet, so the next milestone must produce visible editor behavior quickly.

### Next smallest useful step

- Escape the plugin name in `plugin.xml`, rerun the repo-local Gradle checks, then start the minimal Airflow detection path.

### Metadata fix applied

- Escaped the plugin name in `src/main/resources/META-INF/plugin.xml` as `&amp;`.
- Result:
  - `./gradlew.bat -g .gradle-user-home test --console=plain` now succeeds.
  - the scaffold is buildable with a repo-local Gradle home

### А не фигню ли я делаю?
## Sanity check
- Current step: close the scaffold loop before feature work starts
- User-visible value: every later milestone now rests on a runnable plugin project instead of a broken shell
- Why this still fits MVP: this is the minimum credible baseline for inspections, quick fixes, and screenshots
- Simpler alternative I considered: postpone build cleanup until after the first feature
- What I am deliberately NOT building right now: feature code, settings UI, extra build automation
- Decision: continue

### Updated risks

- The project still depends on repo-local Gradle state for reliable verification until the global cache issue is ignored or cleaned up.
- There are still no automated tests or editor features, so the next step must produce actual PSI-based behavior.

### Next smallest useful step

- Implement the smallest Airflow schedule detection and invalid-cron inspection, plus tests for the matcher and inspection.

## Milestone 3: Airflow schedule inspection and preview

### А не фигню ли я делаю?
## Sanity check
- Current step: implement only the obvious Airflow schedule path instead of broader DAG semantics
- User-visible value: users get an inline warning for broken schedules and a preview for valid ones in the first demo
- Why this still fits MVP: schedule mistakes are common, screenshot-friendly, and small enough to verify well
- Simpler alternative I considered: limit the Airflow pack to a warning only and skip preview
- What I am deliberately NOT building right now: Airflow operator intelligence, timetable support, Jinja injection
- Decision: continue

### Implemented

- Added explicit DAG-context matching for `DAG(...)` and `with DAG(...)` cases.
- Added `AirflowScheduleParser` with:
  - basic 5-field cron validation
  - supported macro handling for previewable schedules
- Added `AirflowScheduleInspection` for invalid or suspicious schedule strings.
- Added `PreviewAirflowScheduleIntention` to show the next 5 run times.
- Added tests for:
  - parser validation and preview behavior
  - inspection highlighting in and out of DAG contexts

### А не фигню ли я делаю?
## Sanity check
- Current step: decide whether to expand from schedule feedback into wider Airflow language features
- User-visible value: keeping the preview flow small means it actually ships instead of turning into half-finished Airflow semantics
- Why this still fits MVP: one solid schedule flow beats a vague "Airflow assistant"
- Simpler alternative I considered: add Jinja injection immediately because the sample file already contains templated SQL
- What I am deliberately NOT building right now: template-language support, semantic task/operator inspections, automatic cron rewriting
- Decision: cut scope and continue

### Verification

- `./gradlew.bat -g .gradle-user-home test --console=plain` succeeded after the Airflow slice was in place.

### Current risks

- Airflow preview supports only the chosen 5-field cron subset and a few macros.
- Airflow template strings are still plain strings in v1.

### Next smallest useful step

- Implement Kafka Connect detection, inspections, and the smallest useful quick fixes across JSON, YAML, and `.properties`.

## Milestone 4: Kafka Connect inspections and quick fixes

### А не фигню ли я делаю?
## Sanity check
- Current step: implement a flat Kafka Connect config model and only the first conflict/missing-field rules
- User-visible value: catches real connector authoring mistakes without pretending to understand every connector
- Why this still fits MVP: the first demo only needs one or two strong editor wins in config files
- Simpler alternative I considered: build a reusable config rules engine first
- What I am deliberately NOT building right now: connector catalogs, nested YAML engines, transform-specific validation
- Decision: continue

### Implemented

- Added a flat `KConnectConfigModel` for JSON, YAML, and `.properties` files.
- Added inspections for:
  - `topics` vs `topics.regex`
  - missing `name`, `connector.class`, `tasks.max`
  - missing `transforms.<alias>.type`
- Added quick fixes to:
  - remove conflicting topic entries
  - add missing required keys
  - add missing transform type placeholders
- Added tests for JSON conflict removal, YAML transform placeholder insertion, and `.properties` missing-field highlighting.

### А не фигню ли я делаю?
## Sanity check
- Current step: decide whether to deepen the Kafka pack into connector-specific rules
- User-visible value: stopping at flat, high-signal checks keeps the plugin understandable from screenshots
- Why this still fits MVP: users can already see conflicts, missing identity fields, and direct fixes
- Simpler alternative I considered: add connector-class-specific validation for JDBC and Debezium immediately
- What I am deliberately NOT building right now: schema registries, connector catalogs, remote validation, nested config modeling
- Decision: continue

### Verification

- `./gradlew.bat -g .gradle-user-home test --console=plain` succeeded with the Kafka Connect inspection and quick-fix tests in place.

### Current risks

- The model intentionally supports only flat top-level mappings.
- YAML nested structures are explicitly out of scope for the first build.

### Next smallest useful step

- Add pack-level settings, the smallest real regex injection path, and README/demo polish.

## Milestone 5: Settings, regex injection, and stabilization

### А не фигню ли я делаю?
## Sanity check
- Current step: add only the smallest settings and injection surface that improves the demo
- User-visible value: users can toggle packs, and Kafka regex fields get editor help where it matters
- Why this still fits MVP: this strengthens the first demo without turning into an injection framework
- Simpler alternative I considered: skip injection entirely and document it as future work
- What I am deliberately NOT building right now: Airflow Jinja injection, `.properties` regex injection, IntelliLang-heavy abstractions
- Decision: continue

### Implemented

- Added `ConfigIqSettingsService` and `ConfigIqSettingsConfigurable`.
- Wired Airflow and Kafka Connect features to pack-level settings toggles.
- Added `KConnectRegexInjector` for JSON and YAML regex fields:
  - `topics.regex`
  - `transforms.<alias>.regex`
- Declared the IntelliLang bundled-plugin dependency explicitly so RegExp support is not accidental.
- Added a focused injection test that verifies the injector requests `RegExp` for Kafka Connect JSON regex fields.
- Updated `README.md` and scope docs to match the actual shipped behavior.

### А не фигню ли я делаю?
## Sanity check
- Current step: decide whether to stretch the injection work to every possible host type before calling the MVP buildable
- User-visible value: JSON/YAML injection plus Kafka inspections already delivers the demoable authoring help the Marketplace page needs
- Why this still fits MVP: pushing into `.properties` and Airflow templates now would add complexity faster than value
- Simpler alternative I considered: keep the injector generic and keep debugging host-specific edge cases
- What I am deliberately NOT building right now: `.properties` regex injection, Airflow template injection, searchable options infrastructure
- Decision: cut scope and continue

### Verification

- `./gradlew.bat -g .gradle-user-home test --console=plain` succeeded after the settings and injection slice.
- `./gradlew.bat -g .gradle-user-home buildPlugin --console=plain` succeeded for the final MVP package.

### Remaining risks

- `runIde` was not exercised in this environment, so the final UI pass is still a manual check.
- `.properties` files currently get inspections and quick fixes, but not regex injection.
- `buildSearchableOptions` remains disabled because it is unstable in this environment and not required for the first demo.

### Next smallest useful step

- Open the sample files in a local IDE run, capture screenshots, and prepare the Marketplace-facing polish without expanding scope.

## Milestone 6: Marketplace pre-upload checklist

### Release gating update

- Date: 2026-04-01
- User confirmation:
  - PyCharm runs are okay
- Decision:
  - treat the Marketplace first upload as a separate release-prep slice
  - do not finalize `plugin.xml` name until the short Marketplace name is chosen

### What is now fixed as release scope

- Added a dedicated first-upload checklist in `docs/marketplace-first-upload.md`.
- Captured the mandatory pre-upload gates:
  - short Marketplace name
  - fresh PyCharm smoke
  - `verifyPlugin`
  - signed ZIP
  - production metadata
  - Marketplace SVG icon
  - valid external links
  - EULA/source/privacy posture

### Release prep completed

- Selected the Marketplace name `ConfigIQ Ops`.
- Updated plugin metadata, release notes, and vendor contact details.
- Added license, EULA, privacy note, and custom plugin icons.
- `./gradlew.bat -g .gradle-user-home test --console=plain` succeeded.
- `./gradlew.bat -g .gradle-user-home buildPlugin --console=plain` succeeded.
- `./gradlew.bat -g .gradle-user-home verifyPlugin --console=plain` succeeded after adding a verifier IDE matrix.

### Next smallest useful step

- Publish the Git repository, wire Marketplace listing links to it, and add ZIP signing credentials before the first upload.
