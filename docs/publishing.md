# Publishing

## Current rule for this repository

- Marketplace plugin page: `https://plugins.jetbrains.com/plugin/31040-configiq-ops`
- Marketplace edit page: `https://plugins.jetbrains.com/plugin/31040-configiq-ops/edit`
- Marketplace numeric plugin id: `31040`
- Plugin XML id: `com.configiq.airflowkconnect`
- First upload was done manually in Marketplace UI.
- All next versions are published from this repository with Gradle `publishPlugin`.

## Where publishing is configured

- Version source: `gradle.properties`
  - `pluginVersion = ...`
- Marketplace metadata and release notes:
  - `build.gradle.kts`
  - `src/main/resources/META-INF/plugin.xml`
- Gradle 2.x publishing token wiring:
  - `build.gradle.kts`
  - `intellijPlatform { publishing { token = providers.gradleProperty("intellijPlatformPublishingToken") } }`

## Required local token

Publishing uses the Gradle project property:

```powershell
ORG_GRADLE_PROJECT_intellijPlatformPublishingToken
```

One way to set it on Windows:

```powershell
[System.Environment]::SetEnvironmentVariable(
  "ORG_GRADLE_PROJECT_intellijPlatformPublishingToken",
  "<your-token>",
  "User"
)

$env:ORG_GRADLE_PROJECT_intellijPlatformPublishingToken =
  [System.Environment]::GetEnvironmentVariable(
    "ORG_GRADLE_PROJECT_intellijPlatformPublishingToken",
    "User"
)
```

This workstation also keeps the Marketplace token under:

```powershell
PUBLISH_TOKEN_PLUGIN
```

Before running Gradle publishing commands, map it into the Gradle property:

```powershell
$env:ORG_GRADLE_PROJECT_intellijPlatformPublishingToken =
  [System.Environment]::GetEnvironmentVariable("PUBLISH_TOKEN_PLUGIN", "User")
```

## Release checklist

1. Bump `pluginVersion` in `gradle.properties`.
2. Update release notes in `build.gradle.kts` and keep `plugin.xml` metadata in sync.
3. Run the release checks:

```powershell
./gradlew.bat -g .gradle-user-home ktlintCheck detekt test buildPlugin verifyPlugin --console=plain --no-daemon
```

4. Publish the update:

```powershell
./gradlew.bat -g .gradle-user-home publishPlugin --console=plain --no-daemon
```

## Current verification matrix

Before publishing, `verifyPlugin` must pass for:

- the recommended PyCharm matrix
- `IntelliJ IDEA Ultimate 261.23567.71`

This extra IntelliJ IDEA target exists because `0.1.4` fixed a real compatibility issue in IU where the plugin previously had a mandatory Python dependency.

## How to confirm the upload

After `publishPlugin`, verify that the new version is downloadable:

```powershell
Invoke-WebRequest `
  -Uri "https://plugins.jetbrains.com/plugin/download?pluginId=com.configiq.airflowkconnect&version=<version>" `
  -Method Head `
  -MaximumRedirection 0
```

Expected result:

- `301` means Marketplace has accepted the uploaded version and redirects to the concrete artifact.

To inspect the concrete update record:

```powershell
$headers = @{
  Authorization = "Bearer " + [System.Environment]::GetEnvironmentVariable("PUBLISH_TOKEN_PLUGIN", "User")
}

Invoke-WebRequest `
  -Uri "https://plugins.jetbrains.com/api/plugins/31040/updates" `
  -Headers $headers `
  -UseBasicParsing
```

## Re-uploading the same version

Marketplace does not accept uploading the same plugin version twice:

```text
The com.configiq.airflowkconnect plugin already contains version <version> in channel
```

If the already-uploaded archive must be replaced, delete that exact update first. Do this only intentionally: deleting an approved/listed update removes it from the public version list, and the re-uploaded version returns to Marketplace review with `approve=false` and `listed=false` until JetBrains approves it again.

1. Find the update id:

```powershell
$headers = @{
  Authorization = "Bearer " + [System.Environment]::GetEnvironmentVariable("PUBLISH_TOKEN_PLUGIN", "User")
}

Invoke-WebRequest `
  -Uri "https://plugins.jetbrains.com/api/plugins/31040/updates" `
  -Headers $headers `
  -UseBasicParsing
```

2. Delete the exact update:

```powershell
Invoke-WebRequest `
  -Uri "https://plugins.jetbrains.com/api/updates/<updateId>" `
  -Method Delete `
  -Headers $headers `
  -UseBasicParsing
```

Expected successful response:

```json
{"message":"Update has been removed"}
```

3. Rebuild and publish the same version:

```powershell
$env:ORG_GRADLE_PROJECT_intellijPlatformPublishingToken =
  [System.Environment]::GetEnvironmentVariable("PUBLISH_TOKEN_PLUGIN", "User")

./gradlew.bat -g .gradle-user-home clean buildPlugin --console=plain --no-daemon
./gradlew.bat -g .gradle-user-home publishPlugin --console=plain --no-daemon
```

4. Confirm the new update id and archive:

```powershell
Invoke-WebRequest `
  -Uri "https://plugins.jetbrains.com/plugin/download?pluginId=com.configiq.airflowkconnect&version=<version>" `
  -Method Head `
  -MaximumRedirection 0

tar -tf build\distributions\configiq-airflow-kafka-connect-<version>.zip
```

## Last known successful release

- Version: `0.1.4`
- Published from this repository with:

```powershell
./gradlew.bat -g .gradle-user-home ktlintCheck detekt test buildPlugin verifyPlugin --console=plain --no-daemon
./gradlew.bat -g .gradle-user-home publishPlugin --console=plain --no-daemon
```

## Last known same-version re-upload

- Date: 2026-05-03
- Version: `0.1.4`
- Deleted update id: `1026776`
- Re-uploaded update id: `1038071`
- Immediate Marketplace state after upload: `approve=false`, `listed=false`
- Local checks:
  - `tar -tf build\distributions\configiq-airflow-kafka-connect-0.1.4.zip`
  - downloaded Marketplace archive for update `1038071`
  - `tar`, `jar`, and `Expand-Archive` all extracted the downloaded archive successfully
