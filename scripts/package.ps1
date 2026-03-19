<# 
.SYNOPSIS
    Package the staging directory into a distributable zip.

.DESCRIPTION
    Compresses the staging directory into a zip file in the dist directory.
    This script should be called from the project root directory.
    Requires APP_VERSION environment variable to be set.
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

$ErrorActionPreference = 'Stop'
$stepStart = Get-Date

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " InsightPC - Package Artifact (zip)" -ForegroundColor Cyan
Write-Host " Started: $($stepStart.ToString('yyyy-MM-dd HH:mm:ss'))" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Step 1: Resolve version
Write-Host "`n[1/3] Resolving version..." -ForegroundColor Cyan
$version = $env:APP_VERSION
if (-not $version) {
    Write-Host "  APP_VERSION not set, extracting from Maven..." -ForegroundColor Yellow
    $version = & mvn -q -DforceStdout 'help:evaluate' -Dexpression='project.version' 2>$null
    $version = $version.Trim()
    if (-not $version) { throw 'APP_VERSION not set and failed to extract version from Maven' }
}
Write-Host "  Version: $version" -ForegroundColor Gray

# Step 2: Create zip archive
Write-Host "`n[2/3] Creating zip archive..." -ForegroundColor Cyan
if (-not (Test-Path dist)) { New-Item -ItemType Directory -Path dist | Out-Null }

$zipName = "insightpc-windows-$version.zip"
Write-Host "  Source: staging\" -ForegroundColor Gray
Write-Host "  Target: dist/$zipName" -ForegroundColor Gray
Add-Type -AssemblyName System.IO.Compression
$stagingFull = (Resolve-Path 'staging').Path
$zipFull = Join-Path (Resolve-Path 'dist') $zipName
if (Test-Path $zipFull) { Remove-Item $zipFull -Force }

$zipStream = [System.IO.File]::Create($zipFull)
$zip = New-Object System.IO.Compression.ZipArchive($zipStream, [System.IO.Compression.ZipArchiveMode]::Create)
try {
    foreach ($file in (Get-ChildItem $stagingFull -Recurse -File)) {
        $entryName = $file.FullName.Substring($stagingFull.Length).TrimStart('\', '/').Replace('\', '/')
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
Write-Host "  Size: $([math]::Round($zipSize / 1MB, 1)) MB" -ForegroundColor Gray

# Step 3: Cleanup
Write-Host "`n[3/3] Cleaning up..." -ForegroundColor Cyan
Remove-Item -Path 'staging' -Recurse -Force
Write-Host "  Removed staging directory" -ForegroundColor Gray

$elapsed = (Get-Date) - $stepStart
Write-Host "`nCreated dist/$zipName ($('{0:mm\:ss}' -f $elapsed) elapsed)" -ForegroundColor Green
