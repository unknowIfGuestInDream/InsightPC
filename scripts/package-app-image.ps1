<# 
.SYNOPSIS
    Create a native app image using jpackage (Windows).

.DESCRIPTION
    Builds a native application image from the staging directory using jpackage.
    This produces a self-contained application with a native .exe launcher.
    Requires Java 21 with jpackage support.

.PARAMETER StagingDir
    Path to the staging directory containing the application jar and lib.
    Defaults to 'staging'.
#>

#
# Copyright (c) 2026 unknowIfGuestInDream.
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#     * Redistributions of source code must retain the above copyright
# notice, this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above copyright
# notice, this list of conditions and the following disclaimer in the
# documentation and/or other materials provided with the distribution.
#     * Neither the name of unknowIfGuestInDream, any associated website, nor the
# names of its contributors may be used to endorse or promote products
# derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL UNKNOWIFGUESTINDREAM BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#

param(
    [string]$StagingDir = 'staging'
)

$ErrorActionPreference = 'Stop'
$stepStart = Get-Date

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " InsightPC - App Image (jpackage)" -ForegroundColor Cyan
Write-Host " Started: $($stepStart.ToString('yyyy-MM-dd HH:mm:ss'))" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

if (-not (Test-Path $StagingDir)) {
    throw "Staging directory not found: $StagingDir"
}
Write-Host "  Staging dir: $StagingDir" -ForegroundColor Gray

# Step 1: Locate JDK 21
$jdkDir = $null
try {
    $savedEAP = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    $javaVerOutput = & java -version 2>&1
    $ErrorActionPreference = $savedEAP
    $versionLine = ($javaVerOutput | Select-Object -First 1).ToString()
    if ($versionLine -match '"(\d+)[.+]') {
        $majorVersion = [int]$Matches[1]
        if ($majorVersion -eq 21) {
            if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin'))) {
                $jdkDir = $env:JAVA_HOME
            } else {
                $javaCmd = Get-Command java -ErrorAction SilentlyContinue
                if ($javaCmd) {
                    $jdkDir = Split-Path (Split-Path $javaCmd.Source -Parent) -Parent
                }
            }
            if ($jdkDir) {
                $jpackageCheck = Join-Path (Join-Path $jdkDir 'bin') 'jpackage.exe'
                if (-not (Test-Path $jpackageCheck)) { $jdkDir = $null }
            }
        }
    }
} catch { }

if (-not $jdkDir) {
    throw "Java 21 with jpackage is required but not found. Set JAVA_HOME to a JDK 21 installation."
}

$jpackageCmd = Join-Path (Join-Path $jdkDir 'bin') 'jpackage.exe'
Write-Host "`n[1/4] Using Java 21 from: $jdkDir" -ForegroundColor Cyan

# Step 2: Resolve version
Write-Host "`n[2/4] Resolving version..." -ForegroundColor Cyan
$version = $env:APP_VERSION
if (-not $version) {
    Write-Host "  APP_VERSION not set, extracting from Maven..." -ForegroundColor Yellow
    $version = & mvn -q -DforceStdout 'help:evaluate' -Dexpression='project.version' 2>$null
    $version = $version.Trim()
    if (-not $version) { throw 'APP_VERSION not set and failed to extract version from Maven' }
}
Write-Host "  Version: $version" -ForegroundColor Gray

# Step 3: Create app image
Write-Host "`n[3/4] Creating app image with jpackage..." -ForegroundColor Cyan

$jarPath = Join-Path $StagingDir 'insightpc.jar'
if (-not (Test-Path $jarPath)) {
    throw "Application jar not found: $jarPath"
}

# Look for icon file
$iconArgs = @()
$iconPath = 'src\main\resources\com\tlcsdm\insightpc\icons\insightpc.ico'
if (Test-Path $iconPath) {
    $iconArgs = @('--icon', $iconPath)
    Write-Host "  Icon: $iconPath" -ForegroundColor Gray
}

if (Test-Path 'app-image') { Remove-Item -Path 'app-image' -Recurse -Force }

# Create the app image with jpackage
# --type app-image creates a directory-based application image
# JVM options for memory optimization
$jpackageArgs = @(
    '--type', 'app-image',
    '--name', 'InsightPC',
    '--app-version', $version,
    '--vendor', 'Tlcsdm',
    '--description', 'A cross-platform system information visualizer',
    '--input', $StagingDir,
    '--main-jar', 'insightpc.jar',
    '--main-class', 'com.tlcsdm.insightpc.Launcher',
    '--java-options', '-Xms64m',
    '--java-options', '-Xmx256m',
    '--java-options', '-XX:MaxMetaspaceSize=128m',
    '--java-options', '-XX:+UseG1GC',
    '--java-options', '--enable-native-access=javafx.graphics,com.sun.jna,ALL-UNNAMED',
    '--dest', 'app-image'
) + $iconArgs

& $jpackageCmd @jpackageArgs
if ($LASTEXITCODE -ne 0) { throw "jpackage failed with exit code $LASTEXITCODE" }

Write-Host "  App image created at: app-image\InsightPC" -ForegroundColor Gray

# Step 4: Package into zip
Write-Host "`n[4/4] Creating distributable zip..." -ForegroundColor Cyan
if (-not (Test-Path 'dist')) { New-Item -ItemType Directory -Path 'dist' | Out-Null }

$zipName = "insightpc-windows-$version.zip"
$appImageFull = (Resolve-Path 'app-image\InsightPC').Path
$zipFull = Join-Path (Resolve-Path 'dist') $zipName
if (Test-Path $zipFull) { Remove-Item $zipFull -Force }

Add-Type -AssemblyName System.IO.Compression
$zipStream = [System.IO.File]::Create($zipFull)
$zip = New-Object System.IO.Compression.ZipArchive($zipStream, [System.IO.Compression.ZipArchiveMode]::Create)
try {
    foreach ($file in (Get-ChildItem $appImageFull -Recurse -File)) {
        $entryName = $file.FullName.Substring($appImageFull.Length).TrimStart('\', '/').Replace('\', '/')
        $entry = $zip.CreateEntry($entryName, [System.IO.Compression.CompressionLevel]::Optimal)
        $es = $entry.Open()
        try {
            $fs = [System.IO.FileStream]::new(
                $file.FullName,
                [System.IO.FileMode]::Open,
                [System.IO.FileAccess]::Read,
                ([System.IO.FileShare]::ReadWrite -bor [System.IO.FileShare]::Delete)
            )
            try { $fs.CopyTo($es) } finally { $fs.Dispose() }
        } finally { $es.Dispose() }
    }
} finally {
    $zip.Dispose()
    $zipStream.Dispose()
}

$zipSize = (Get-Item $zipFull).Length
Write-Host "  Created: dist\$zipName ($([math]::Round($zipSize / 1MB, 1)) MB)" -ForegroundColor Gray

# Cleanup
Remove-Item -Path 'app-image' -Recurse -Force
Remove-Item -Path $StagingDir -Recurse -Force
Write-Host "  Cleaned up temporary directories" -ForegroundColor Gray

$elapsed = (Get-Date) - $stepStart
Write-Host "`nApp image packaged successfully. ($('{0:mm\:ss}' -f $elapsed) elapsed)" -ForegroundColor Green
