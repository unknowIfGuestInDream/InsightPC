#!/bin/bash

#
# Package the staging directory into a distributable zip (macOS).
#
# Compresses the staging directory into a zip file in the dist directory.
# This script should be called from the project root directory.
# Requires APP_VERSION environment variable to be set.
#

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

set -e

STEP_START=$(date +%s)

echo "========================================"
echo " InsightPC - Package Artifact (zip)"
echo " Started: $(date '+%Y-%m-%d %H:%M:%S')"
echo "========================================"

# Step 1: Resolve version
echo ""
echo "[1/3] Resolving version..."
version="${APP_VERSION:-}"
if [ -z "$version" ]; then
    echo "  APP_VERSION not set, extracting from Maven..."
    version=$(mvn -q -DforceStdout 'help:evaluate' -Dexpression=project.version 2>/dev/null | tr -d '\r')
    if [ -z "$version" ]; then
        echo "APP_VERSION not set and failed to extract version from Maven" >&2
        exit 1
    fi
fi
echo "  Version: $version"

# Step 2: Create zip archive
echo ""
echo "[2/3] Creating zip archive..."
mkdir -p dist

zip_name="insightpc-macos-${version}.zip"
echo "  Source: staging/"
echo "  Target: dist/$zip_name"
rm -f "dist/$zip_name"

cd staging
zip -r "../dist/$zip_name" .
cd ..

zip_size=$(du -m "dist/$zip_name" | cut -f1)
echo "  Size: ${zip_size} MB"

# Step 3: Cleanup
echo ""
echo "[3/3] Cleaning up..."
rm -rf staging
echo "  Removed staging directory"

elapsed=$(( $(date +%s) - STEP_START ))
echo ""
echo "Created dist/$zip_name (${elapsed}s elapsed)"
