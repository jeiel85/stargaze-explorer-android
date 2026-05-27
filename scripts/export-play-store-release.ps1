param(
    [string]$Version = "",
    [string]$AabPath = "",
    [string]$DesktopPath = ""
)

$ErrorActionPreference = "Stop"

function Resolve-Version {
    param([string]$ExplicitVersion)

    if ($ExplicitVersion.Trim().Length -gt 0) {
        return $ExplicitVersion.TrimStart("v")
    }

    $buildFile = Join-Path $PSScriptRoot "..\app\build.gradle.kts"
    $versionLine = Select-String -Path $buildFile -Pattern 'versionName\s*=' | Select-Object -First 1
    if ($null -eq $versionLine -or $versionLine.Line -notmatch '"([^"]+)"') {
        throw "Could not resolve versionName from app/build.gradle.kts"
    }

    return $Matches[1]
}

function Resolve-VersionCode {
    $buildFile = Join-Path $PSScriptRoot "..\app\build.gradle.kts"
    $codeLine = Select-String -Path $buildFile -Pattern 'versionCode\s*=' | Select-Object -First 1
    if ($null -eq $codeLine -or $codeLine.Line -notmatch '=\s*(\d+)') {
        return "1"
    }
    return $Matches[1].Trim()
}

function Resolve-DesktopPath {
    param([string]$ExplicitDesktopPath)

    $candidates = @()
    if ($ExplicitDesktopPath.Trim().Length -gt 0) {
        $candidates += $ExplicitDesktopPath
    }
    if ($env:OneDrive) {
        $candidates += (Join-Path $env:OneDrive "바탕 화면")
        $candidates += (Join-Path $env:OneDrive "Desktop")
    }
    $shellDesktop = [Environment]::GetFolderPath("Desktop")
    if ($shellDesktop) {
        $candidates += $shellDesktop
    }
    $candidates += (Join-Path $HOME "Desktop")

    foreach ($candidate in $candidates) {
        if ($candidate -and (Test-Path -LiteralPath $candidate -PathType Container)) {
            return (Resolve-Path -LiteralPath $candidate).Path
        }
    }

    if ($shellDesktop) {
        New-Item -ItemType Directory -Force -Path $shellDesktop | Out-Null
        return (Resolve-Path -LiteralPath $shellDesktop).Path
    }

    throw "Could not resolve a Desktop path."
}

function Resolve-AabPath {
    param([string]$ExplicitAabPath)

    if ($ExplicitAabPath.Trim().Length -gt 0) {
        if (-not (Test-Path -LiteralPath $ExplicitAabPath -PathType Leaf)) {
            throw "AAB file not found: $ExplicitAabPath"
        }
        return (Resolve-Path -LiteralPath $ExplicitAabPath).Path
    }

    # Search bundle folder recursively for robustness
    $releaseBundleDir = Join-Path $PSScriptRoot "..\app\build\outputs\bundle"
    $aab = Get-ChildItem -Path $releaseBundleDir -Filter "*.aab" -Recurse -File -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

    if ($null -eq $aab) {
        throw "Release AAB not found. Build it first: .\gradlew.bat bundleRelease"
    }

    return $aab.FullName
}

$resolvedVersion = Resolve-Version -ExplicitVersion $Version
$resolvedCode = Resolve-VersionCode
$desktop = Resolve-DesktopPath -ExplicitDesktopPath $DesktopPath
$sourceAab = Resolve-AabPath -ExplicitAabPath $AabPath
$notesPath = Join-Path $PSScriptRoot "..\play_store\release_notes\v$resolvedVersion.txt"

if (-not (Test-Path -LiteralPath $notesPath -PathType Leaf)) {
    throw "Play Store release notes not found: $notesPath"
}

$targetAab = Join-Path $desktop "StargazeExplorer-v$resolvedVersion-vc$resolvedCode.aab"
$targetNotes = Join-Path $desktop "StargazeExplorer-v$resolvedVersion-vc$resolvedCode-release-notes.txt"

Copy-Item -LiteralPath $sourceAab -Destination $targetAab -Force
Copy-Item -LiteralPath $notesPath -Destination $targetNotes -Force

$result = Get-Item $targetAab, $targetNotes | Select-Object Name, Length, LastWriteTime
$result | Format-Table -AutoSize

Write-Host "Exported Play Store files successfully to Desktop:" -ForegroundColor Green
Write-Host "- AAB:   $targetAab" -ForegroundColor Cyan
Write-Host "- Notes: $targetNotes" -ForegroundColor Cyan
