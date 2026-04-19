# Marketplace First Upload

## Context anchor

- Goal: prepare the first JetBrains Marketplace upload for the current MVP build.
- Explicit constraints:
  - keep the first release narrow and honest
  - do not upload before manual smoke in a fresh PyCharm confirms the main flows
  - choose a short Marketplace plugin name before the first upload
  - keep the repository name independent from the Marketplace plugin name

## Decision-first recommendation

- Treat the first upload as blocked on naming, manual smoke, verifier, signing, and production metadata.
- Keep the repository name as-is.
- Change the Marketplace plugin name to a short brand-style name and move the detailed use case into description, screenshots, and listing copy.

## What must be done before the first upload

### 1. Rename the plugin before upload

- Chosen Marketplace-facing name:
  - `ConfigIQ Ops`
- Approval constraints to respect:
  - original and unique
  - 30 characters or fewer
  - no word `Plugin`
  - no JetBrains or IDE product names in the plugin name
- Recommendation:
  - keep the repository name unchanged
  - shorten only the Marketplace plugin name

### 2. Run a manual smoke test in a fresh PyCharm

- This is the main factual release gate before first upload.
- Minimum smoke checklist:
  - open a sample Airflow file
  - open sample Kafka Connect JSON and YAML files
  - verify the happy path
  - verify false positives are not obvious
  - verify quick fixes
  - verify settings toggles
  - verify the schedule preview or intention flow
- If 1-2 papercuts show up here, treat them as the real micro-stage-2.

### 3. Run `verifyPlugin`

- `test` and `buildPlugin` are necessary but not sufficient for Marketplace readiness.
- Required verification command:

```powershell
./gradlew.bat -g .gradle-user-home verifyPlugin --console=plain
```

### 4. Sign the distribution ZIP

- Marketplace publishing requires a signed plugin.
- Build artifact:

```powershell
./gradlew.bat -g .gradle-user-home buildPlugin --console=plain
```

- Signing step:

```powershell
./gradlew.bat -g .gradle-user-home signPlugin --console=plain
```

- Hidden constraint:
  - signing is blocked until certificate material and secrets are configured

### 5. Finalize `plugin.xml` as production metadata

- Core listing data comes from `plugin.xml` and cannot be edited retroactively without uploading a new plugin version.
- Check before upload:
  - `name`
  - `description`
  - `change-notes`
  - `vendor` URL and email
  - `idea-version`
  - plugin `version`

### 6. Replace the default icon with a production SVG

- Approval requires a real SVG logo sized for the Marketplace listing.
- The default template icon must not ship.

### 7. Prepare external links and docs

- External links must be valid and relevant to the plugin or its author.
- Prepare at least:
  - docs link
  - issue tracker link
  - source code link, if public

### 8. Resolve EULA, source, and privacy posture

- Required posture before listing:
  - developer EULA
  - source code link if the plugin is open source
  - privacy policy only if the plugin collects personal data
- Practical release rule:
  - keep telemetry out of `0.1.0`
  - if telemetry is added later, require explicit user permission

## Current status

- Manual note from the latest validation pass:
  - PyCharm runs are currently okay
- Still not complete for first Marketplace upload because:
  - ZIP signing credentials are not configured in this workspace
  - Marketplace listing links still need to be set in JetBrains Marketplace
  - actual screenshot image files still need to be captured from the IDE

## Current metadata gaps

- `src/main/resources/META-INF/plugin.xml` currently has:
  - `name`
  - `vendor`
  - `description`
- Still expected before upload:
  - Marketplace-side listing links
  - signing credentials for `signPlugin`
  - captured screenshot assets
- Marketplace name decision is fixed as `ConfigIQ Ops`.

## Name shortlist

| Name | When to choose | Pros | Cons | Risk | Applicability |
| --- | --- | --- | --- | --- | --- |
| `ConfigIQ` | Choose if you want a brand-first listing and room to expand later | shortest, clean, reusable for future scope | least descriptive in search results | medium: may need stronger subtitle and screenshots | Yes |
| `ConfigIQ Ops` | Choose if you want a slightly broader operational flavor without tying the name to one stack | still short, sounds product-like, keeps room for scope changes | `Ops` is vague and may read broader than the plugin actually is | medium | Yes |
| `ConfigIQ Configs` | Choose if you want the name to hint at authoring/config validation | clearer than pure brand naming, still under the limit | a bit generic and less premium than `ConfigIQ` | low-medium | Yes |
| `ConfigIQ Airflow` | Choose only if the Kafka Connect side becomes secondary | clearer for Airflow users | misrepresents the Kafka Connect slice | high | No |
| `ConfigIQ Kafka Connect` | Choose only if Airflow is cut from the first listing | very descriptive for one domain | too long to keep flexibility and narrows the brand too early | high | No |

## Chosen naming direction

- Selected name: `ConfigIQ Ops`
- Reason:
  - it stays within the approval constraints
  - it leaves room for broader scope later
  - the Airflow and Kafka Connect specifics now live in metadata, docs, and media instead of the title

## Not included

- Repository rename.
  - Not included because the issue is the Marketplace-facing plugin name, not the repository name.
- Marketplace automation or CI release pipeline.
  - Not included because the first upload still needs manual metadata, smoke, and signing decisions.
- Telemetry or usage collection.
  - Not included because it creates privacy and approval overhead without helping `0.1.0`.
