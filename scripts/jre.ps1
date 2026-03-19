<# 
.SYNOPSIS
    Create a minimal custom JRE using jlink.

.DESCRIPTION
    If the current environment already has Java 21, uses it directly.
    Otherwise downloads the Amazon Corretto JDK.
    Uses jdeps to analyze the application jar for required JDK modules, then creates
    a custom runtime image with jlink containing only those modules.
    This significantly reduces the JRE size compared to a full JRE download.

.PARAMETER StagingDir
    Path to the staging directory containing the application jar.
    Defaults to the current directory ('.').
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
    [string]$StagingDir = '.'
)

$ErrorActionPreference = 'Stop'
$stepStart = Get-Date

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " InsightPC - Custom JRE (jlink)" -ForegroundColor Cyan
Write-Host " Started: $($stepStart.ToString('yyyy-MM-dd HH:mm:ss'))" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

if (-not (Test-Path $StagingDir)) {
    throw "Staging directory not found: $StagingDir"
}
Write-Host "  Staging dir: $StagingDir" -ForegroundColor Gray

Push-Location $StagingDir
try {

# Step 1: Locate JDK 21
$downloadedJdk = $false
$jdkDir = $null

# Check if current environment already has Java 21
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
                    # bin/java.exe -> bin -> JDK root
                    $jdkDir = Split-Path (Split-Path $javaCmd.Source -Parent) -Parent
                }
            }
            if ($jdkDir) {
                $jlinkCheck = Join-Path (Join-Path $jdkDir 'bin') 'jlink.exe'
                if (-not (Test-Path $jlinkCheck)) { $jdkDir = $null }
            }
        }
    }
} catch { }

if ($jdkDir) {
    Write-Host "`n[1/4] Using existing Java 21 from environment" -ForegroundColor Cyan
    Write-Host "  JDK directory: $jdkDir" -ForegroundColor Gray
} else {
    # see https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/downloads-list.html
    $winApi = 'https://corretto.aws/downloads/latest/amazon-corretto-21-x64-windows-jdk.zip'

    Write-Host "`n[1/4] Downloading Amazon Corretto JDK..." -ForegroundColor Cyan
    Write-Host "  URL: $winApi" -ForegroundColor Gray
    $dlStart = Get-Date
    # Use WebClient instead of Invoke-WebRequest to avoid binary corruption on PS 5.1
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12
    (New-Object System.Net.WebClient).DownloadFile($winApi, (Join-Path (Get-Location) 'jdk.zip'))
    $dlElapsed = (Get-Date) - $dlStart
    $dlSize = (Get-Item 'jdk.zip').Length
    Write-Host "  Downloaded: $([math]::Round($dlSize / 1MB, 1)) MB ($('{0:mm\:ss}' -f $dlElapsed))" -ForegroundColor Gray

    Write-Host "  Extracting..." -ForegroundColor Gray
    Expand-Archive -Path 'jdk.zip' -DestinationPath '.' -Force
    Remove-Item -Path 'jdk.zip' -Force

    # Find the extracted JDK directory (name varies by Corretto version)
    $jdkDirItem = Get-ChildItem -Directory -Filter 'jdk*' | Select-Object -First 1
    if ($null -eq $jdkDirItem) { throw "No directory matching 'jdk*' found after extraction. Verify the downloaded archive contains a valid JDK." }
    $jdkDir = $jdkDirItem.FullName
    Write-Host "  JDK directory: $jdkDir" -ForegroundColor Gray
    $downloadedJdk = $true
}

$jdepsCmd = Join-Path (Join-Path $jdkDir 'bin') 'jdeps.exe'
$jlinkCmd = Join-Path (Join-Path $jdkDir 'bin') 'jlink.exe'

# Step 2: Find and analyze jar
$jar = Get-ChildItem -Path '*.jar' -File | Select-Object -First 1
if ($null -eq $jar) { throw 'No jar file found in current directory' }

Write-Host "`n[2/4] Analyzing module dependencies..." -ForegroundColor Cyan
Write-Host "  Jar: $($jar.Name) ($([math]::Round($jar.Length / 1MB, 1)) MB)" -ForegroundColor Gray

# Use jdeps to determine required JDK modules
# When a lib/ directory exists, include it on the module-path so jdeps can
# resolve transitive dependencies from all library jars.
$modules = $null
$jdepsErr = $null
try {
    $savedEAP = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    $jdepsArgs = @('--ignore-missing-deps', '--multi-release', '21', '--print-module-deps')
    if (Test-Path 'lib') {
        $jdepsArgs += '--module-path'
        $jdepsArgs += 'lib'
        $jdepsArgs += '--add-modules'
        $jdepsArgs += 'ALL-MODULE-PATH'
    }
    $jdepsArgs += $jar.Name
    $modules = & $jdepsCmd @jdepsArgs 2>&1 |
        Where-Object { $_ -is [string] } | Select-Object -Last 1
    $ErrorActionPreference = $savedEAP
    if ($LASTEXITCODE -ne 0) { $modules = $null }
} catch {
    $jdepsErr = $_.Exception.Message
    $modules = $null
}

if (-not $modules -or $modules.Trim() -eq '') {
    # Fallback: conservative set covering JavaFX, OSHI system info,
    # Preferences API, XML processing, and sun.misc.Unsafe access.
    # Derived from module-info.java requires and transitive runtime dependencies.
    $modules = 'java.base,java.desktop,java.logging,java.management,java.prefs,java.xml,jdk.unsupported'
    Write-Host "  jdeps analysis failed, using fallback modules" -ForegroundColor Yellow
    if ($jdepsErr) { Write-Host "  Reason: $jdepsErr" -ForegroundColor Yellow }
} else {
    $modules = $modules.Trim()
}
Write-Host "  Modules: $modules" -ForegroundColor Gray

# Step 3: Create custom JRE
Write-Host "`n[3/4] Creating custom JRE with jlink..." -ForegroundColor Cyan
if (Test-Path 'jre') { Remove-Item -Path 'jre' -Recurse -Force }

# Create custom JRE with size optimizations:
#   --strip-debug:    Remove debug info to reduce size
#   --no-man-pages:   Exclude man pages
#   --no-header-files: Exclude C header files
#   --compress zip-9: Maximum compression
& $jlinkCmd --add-modules $modules --output jre --strip-debug --no-man-pages --no-header-files --compress zip-9
if ($LASTEXITCODE -ne 0) { throw "jlink failed with exit code $LASTEXITCODE" }

$jreSize = (Get-ChildItem -Path 'jre' -Recurse -File | Measure-Object -Property Length -Sum).Sum
Write-Host "  JRE size: $([math]::Round($jreSize / 1MB, 1)) MB" -ForegroundColor Gray

# Step 4: Clean up downloaded JDK
Write-Host "`n[4/4] Cleaning up..." -ForegroundColor Cyan
if ($downloadedJdk) {
    Remove-Item -Path $jdkDir -Recurse -Force
    Write-Host "  Removed JDK directory" -ForegroundColor Gray
} else {
    Write-Host "  Skipped (using system JDK)" -ForegroundColor Gray
}

$elapsed = (Get-Date) - $stepStart
Write-Host "`nCustom JRE created successfully. ($('{0:mm\:ss}' -f $elapsed) elapsed)" -ForegroundColor Green
} finally {
    Pop-Location
}
