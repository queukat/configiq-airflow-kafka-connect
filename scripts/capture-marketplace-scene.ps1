param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("airflow-invalid", "airflow-preview", "kconnect-json", "kconnect-yaml", "settings")]
    [string]$Scene
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing
Add-Type @"
using System;
using System.Text;
using System.Runtime.InteropServices;

public static class Win32Capture {
    public delegate bool EnumWindowsProc(IntPtr hWnd, IntPtr lParam);

    [DllImport("user32.dll")]
    public static extern bool EnumWindows(EnumWindowsProc lpEnumFunc, IntPtr lParam);

    [DllImport("user32.dll")]
    public static extern bool IsWindowVisible(IntPtr hWnd);

    [DllImport("user32.dll", CharSet = CharSet.Unicode)]
    public static extern int GetWindowText(IntPtr hWnd, StringBuilder lpString, int nMaxCount);

    [DllImport("user32.dll")]
    public static extern bool GetWindowRect(IntPtr hWnd, out RECT lpRect);

    [DllImport("user32.dll")]
    public static extern bool SetForegroundWindow(IntPtr hWnd);

    [DllImport("user32.dll")]
    public static extern bool ShowWindow(IntPtr hWnd, int nCmdShow);

    public struct RECT {
        public int Left;
        public int Top;
        public int Right;
        public int Bottom;
    }
}
"@

function Get-WorkspaceRoot {
    return (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
}

function Convert-ToIdeaPath {
    param([string]$Path)

    return $Path.Replace("\", "/")
}

function Find-IdeLauncher {
    param([string]$WorkspaceRoot)

    $launcher = Get-ChildItem -Path (Join-Path $WorkspaceRoot ".gradle-user-home\caches") -Recurse -Filter pycharm.bat |
        Where-Object { $_.FullName -like "*pycharm-professional-2025.1-win*" } |
        Select-Object -First 1

    if (-not $launcher) {
        throw "Unable to find the PyCharm 2025.1 launcher under .gradle-user-home."
    }

    return $launcher.FullName
}

function Get-SceneSpec {
    param([string]$WorkspaceRoot, [string]$Scene)

    switch ($Scene) {
        "airflow-invalid" {
            return @{
                TargetPath = Join-Path $WorkspaceRoot "samples\airflow\invalid_cron_schedule_dag.py"
                FileName = "invalid_cron_schedule_dag.py"
                WindowText = "invalid_cron_schedule_dag.py"
                OutputPath = Join-Path $WorkspaceRoot "assets\marketplace\screenshots\01-airflow-invalid-cron.png"
                AfterOpen = {
                    Send-IdeKeys "^g"
                    Send-IdeKeys "9{ENTER}" 1200
                    Send-IdeKeys "^{F1}" 2000
                }
            }
        }
        "airflow-preview" {
            return @{
                TargetPath = Join-Path $WorkspaceRoot "samples\airflow\valid_preview_dag.py"
                FileName = "valid_preview_dag.py"
                WindowText = "valid_preview_dag.py"
                OutputPath = Join-Path $WorkspaceRoot "assets\marketplace\screenshots\02-airflow-preview.png"
                AfterOpen = {
                    Send-IdeKeys "^f"
                    Send-IdeKeys "0 6 * * 1-5{ENTER}" 1200
                    Send-IdeKeys "{ESC}" 600
                    Send-IdeKeys "%{ENTER}" 1200
                    Send-IdeKeys "{ENTER}" 2500
                }
            }
        }
        "kconnect-json" {
            return @{
                TargetPath = Join-Path $WorkspaceRoot "samples\kafka-connect\invalid_conflicting_topics.json"
                FileName = "invalid_conflicting_topics.json"
                WindowText = "invalid_conflicting_topics.json"
                OutputPath = Join-Path $WorkspaceRoot "assets\marketplace\screenshots\03-kconnect-json-conflict.png"
                AfterOpen = {
                    Send-IdeKeys "^f"
                    Send-IdeKeys "topics.regex{ENTER}" 1200
                    Send-IdeKeys "{ESC}" 600
                    Send-IdeKeys "%{ENTER}" 2000
                }
            }
        }
        "kconnect-yaml" {
            return @{
                TargetPath = Join-Path $WorkspaceRoot "samples\kafka-connect\invalid_missing_transform_type.yaml"
                FileName = "invalid_missing_transform_type.yaml"
                WindowText = "invalid_missing_transform_type.yaml"
                OutputPath = Join-Path $WorkspaceRoot "assets\marketplace\screenshots\04-kconnect-yaml-transform-type.png"
                AfterOpen = {
                    Send-IdeKeys "^f"
                    Send-IdeKeys "transforms: maskTopic,routeRecord{ENTER}" 1200
                    Send-IdeKeys "{ESC}" 600
                    Send-IdeKeys "%{ENTER}" 2000
                }
            }
        }
        "settings" {
            return @{
                TargetPath = Join-Path $WorkspaceRoot "samples\airflow\valid_preview_dag.py"
                FileName = "valid_preview_dag.py"
                WindowText = "Settings"
                OutputPath = Join-Path $WorkspaceRoot "assets\marketplace\screenshots\06-settings-toggles.png"
                AfterOpen = {
                    Send-IdeKeys "^%s" 2500
                    Send-IdeKeys "ConfigIQ{ENTER}" 2500
                }
            }
        }
        default {
            throw "Unsupported scene '$Scene'."
        }
    }
}

function Ensure-SandboxConfig {
    param([string]$WorkspaceRoot)

    $sandboxRoot = Join-Path $WorkspaceRoot ".intellijPlatform\sandbox\PY-2025.1"
    $configDir = Join-Path $sandboxRoot "config"
    $systemDir = Join-Path $sandboxRoot "system"
    $pluginsDir = Join-Path $sandboxRoot "plugins"
    $logDir = Join-Path $sandboxRoot "log"
    $optionsDir = Join-Path $configDir "options"

    New-Item -ItemType Directory -Force -Path $configDir, $systemDir, $pluginsDir, $logDir, $optionsDir | Out-Null

    $ideaPropertiesPath = Join-Path $WorkspaceRoot "scripts\pycharm-sandbox.properties"
    @(
        "idea.config.path=$(Convert-ToIdeaPath $configDir)"
        "idea.system.path=$(Convert-ToIdeaPath $systemDir)"
        "idea.plugins.path=$(Convert-ToIdeaPath $pluginsDir)"
        "idea.log.path=$(Convert-ToIdeaPath $logDir)"
    ) | Set-Content -Path $ideaPropertiesPath -Encoding ASCII

    @'
<application>
  <component name="GeneralSettings">
    <option name="showTipsOnStartup" value="false" />
    <option name="confirmOpenNewProject2" value="-1" />
  </component>
</application>
'@ | Set-Content -Path (Join-Path $optionsDir "ide.general.xml") -Encoding ASCII

    @'
<application>
  <component name="LafManager" autodetect="false">
    <laf themeId="JetBrainsLightTheme" />
  </component>
</application>
'@ | Set-Content -Path (Join-Path $optionsDir "laf.xml") -Encoding ASCII

    @'
<application>
  <component name="EditorColorsManagerImpl">
    <global_color_scheme name="IntelliJ Light" />
  </component>
</application>
'@ | Set-Content -Path (Join-Path $optionsDir "colors.scheme.xml") -Encoding ASCII

    $trustedPathsSource = Join-Path $env:APPDATA "JetBrains\PyCharm2023.3\options\trusted-paths.xml"
    $trustedPathsTarget = Join-Path $optionsDir "trusted-paths.xml"
    if (Test-Path $trustedPathsSource) {
        Copy-Item -LiteralPath $trustedPathsSource -Destination $trustedPathsTarget -Force
    }
    else {
        @"
<application>
  <component name="Trusted.Paths">
    <option name="TRUSTED_PROJECT_PATHS">
      <map>
        <entry key="$WorkspaceRoot" value="true" />
      </map>
    </option>
  </component>
</application>
"@ | Set-Content -Path $trustedPathsTarget -Encoding UTF8
    }

    $globalConsent = Join-Path $env:APPDATA "JetBrains\consentOptions\accepted"
    if (Test-Path $globalConsent) {
        $consentOptionsDir = Join-Path $configDir "consentOptions"
        New-Item -ItemType Directory -Force -Path $consentOptionsDir | Out-Null
        Copy-Item -LiteralPath $globalConsent -Destination (Join-Path $consentOptionsDir "accepted") -Force
    }

    $localConsentSource = Join-Path $env:APPDATA "JetBrains\Clion\localConsents\accepted"
    if (Test-Path $localConsentSource) {
        $localConsentsDir = Join-Path $configDir "localConsents"
        New-Item -ItemType Directory -Force -Path $localConsentsDir | Out-Null
        Copy-Item -LiteralPath $localConsentSource -Destination (Join-Path $localConsentsDir "accepted") -Force
    }

    return $ideaPropertiesPath
}

function Send-IdeKeys {
    param(
        [string]$Keys,
        [int]$PauseMs = 600
    )

    [System.Windows.Forms.SendKeys]::SendWait($Keys)
    Start-Sleep -Milliseconds $PauseMs
}

function Open-SceneFile {
    param([string]$FileName)

    Send-IdeKeys "^+n" 1600
    Send-IdeKeys "$FileName{ENTER}" 2500
}

function Hide-ToolWindows {
    Send-IdeKeys "^+a" 1500
    Send-IdeKeys "Hide All Tool Windows{ENTER}" 2000
}

function Get-VisibleWindowHandle {
    param([string]$WindowText)

    $script:candidate = $null
    $callback = [Win32Capture+EnumWindowsProc]{
        param([IntPtr]$hWnd, [IntPtr]$lParam)

        if (-not [Win32Capture]::IsWindowVisible($hWnd)) {
            return $true
        }

        $builder = New-Object System.Text.StringBuilder 512
        [void][Win32Capture]::GetWindowText($hWnd, $builder, $builder.Capacity)
        $title = $builder.ToString()
        if ($title -and $title -like "*$WindowText*") {
            $script:candidate = [pscustomobject]@{
                Handle = $hWnd
                Title = $title
            }
            return $false
        }

        return $true
    }

    [void][Win32Capture]::EnumWindows($callback, [IntPtr]::Zero)
    return $script:candidate
}

function Wait-ForWindow {
    param(
        [string]$WindowText,
        [int]$TimeoutSeconds = 120
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        $window = Get-VisibleWindowHandle -WindowText $WindowText
        if ($window) {
            return $window
        }

        Start-Sleep -Seconds 1
    } while ((Get-Date) -lt $deadline)

    throw "Timed out waiting for a visible window containing '$WindowText'."
}

function Focus-IdeWindow {
    param([IntPtr]$Handle)

    [void][Win32Capture]::ShowWindow($Handle, 3)
    Start-Sleep -Milliseconds 400
    [void][Win32Capture]::SetForegroundWindow($Handle)
    Start-Sleep -Milliseconds 800
}

function Save-WindowCapture {
    param(
        [IntPtr]$Handle,
        [string]$OutputPath
    )

    $rect = New-Object Win32Capture+RECT
    if (-not [Win32Capture]::GetWindowRect($Handle, [ref]$rect)) {
        throw "Failed to get the active PyCharm window rectangle."
    }

    $width = $rect.Right - $rect.Left
    $height = $rect.Bottom - $rect.Top
    if ($width -le 0 -or $height -le 0) {
        throw "The active PyCharm window rectangle is empty."
    }

    $bitmap = New-Object System.Drawing.Bitmap $width, $height
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.CopyFromScreen($rect.Left, $rect.Top, 0, 0, $bitmap.Size)
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $OutputPath) | Out-Null
    $bitmap.Save($OutputPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $graphics.Dispose()
    $bitmap.Dispose()
}

$workspaceRoot = Get-WorkspaceRoot
$sceneSpec = Get-SceneSpec -WorkspaceRoot $workspaceRoot -Scene $Scene
$ideaPropertiesPath = Ensure-SandboxConfig -WorkspaceRoot $workspaceRoot
$launcher = Find-IdeLauncher -WorkspaceRoot $workspaceRoot

$env:PYCHARM_PROPERTIES = $ideaPropertiesPath
if ($env:JAVA_HOME) {
    $env:PYCHARM_JDK = $env:JAVA_HOME
}

Start-Process -FilePath $launcher -ArgumentList @($sceneSpec.TargetPath) -WorkingDirectory $workspaceRoot | Out-Null

$window = Wait-ForWindow -WindowText $sceneSpec.WindowText
Focus-IdeWindow -Handle $window.Handle
Start-Sleep -Seconds 18
Send-IdeKeys "{ESC}" 800
Focus-IdeWindow -Handle $window.Handle
Open-SceneFile -FileName $sceneSpec.FileName
if ($sceneSpec.WindowText -ne "Settings") {
    $window = Wait-ForWindow -WindowText $sceneSpec.WindowText
    Focus-IdeWindow -Handle $window.Handle
    Hide-ToolWindows
}
& $sceneSpec.AfterOpen

$window = Wait-ForWindow -WindowText $sceneSpec.WindowText
Focus-IdeWindow -Handle $window.Handle
Save-WindowCapture -Handle $window.Handle -OutputPath $sceneSpec.OutputPath
Write-Output $sceneSpec.OutputPath
