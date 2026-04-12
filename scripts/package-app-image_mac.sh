#!/bin/bash

#
# Create a native app image using jpackage (macOS).
#
# Builds a native application image from the staging directory using jpackage.
# This produces a self-contained application with a native launcher.
# Requires Java 21 with jpackage support.
#
# Usage: package-app-image_mac.sh [staging_dir]
#   staging_dir: Path to the staging directory containing the application jar and lib.
#                Defaults to 'staging'.
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

STAGING_DIR="${1:-staging}"
STEP_START=$(date +%s)

echo "========================================"
echo " InsightPC - App Image (jpackage)"
echo " Started: $(date '+%Y-%m-%d %H:%M:%S')"
echo "========================================"

if [ ! -d "$STAGING_DIR" ]; then
    echo "Staging directory not found: $STAGING_DIR" >&2
    exit 1
fi
echo "  Staging dir: $STAGING_DIR"

# Step 1: Locate JDK 21
jdk_dir=""
if command -v java >/dev/null 2>&1; then
    java_ver=$(java -version 2>&1 | head -1)
    major_ver=$(echo "$java_ver" | sed -n 's/.*"\([0-9]*\)[.+].*/\1/p')
    if [ "$major_ver" = "21" ]; then
        if [ -n "$JAVA_HOME" ] && [ -d "$JAVA_HOME/bin" ] && [ -x "$JAVA_HOME/bin/jpackage" ]; then
            jdk_dir="$JAVA_HOME"
        else
            java_path=$(command -v java)
            real_path=$(readlink -f "$java_path" 2>/dev/null || echo "$java_path")
            candidate=$(dirname "$(dirname "$real_path")")
            if [ -x "$candidate/bin/jpackage" ]; then
                jdk_dir="$candidate"
            fi
        fi
    fi
fi

if [ -z "$jdk_dir" ]; then
    echo "Java 21 with jpackage is required but not found." >&2
    echo "Set JAVA_HOME to a JDK 21 installation." >&2
    exit 1
fi

jpackage_cmd="$jdk_dir/bin/jpackage"
echo ""
echo "[1/4] Using Java 21 from: $jdk_dir"

# Step 2: Resolve version
echo ""
echo "[2/4] Resolving version..."
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

# Step 3: Create app image
echo ""
echo "[3/4] Creating app image with jpackage..."

jar_path="$STAGING_DIR/insightpc.jar"
lib_path="$STAGING_DIR/lib"

if [ ! -f "$jar_path" ]; then
    echo "Application jar not found: $jar_path" >&2
    exit 1
fi

# Look for icon file
icon_opt=""
icon_path="src/main/resources/com/tlcsdm/insightpc/icons/insightpc.icns"
if [ -f "$icon_path" ]; then
    icon_opt="--icon $icon_path"
    echo "  Icon: $icon_path"
fi

rm -rf app-image

# Create the app image with jpackage
# --type app-image creates a directory-based application image
# JVM options for memory optimization
"$jpackage_cmd" \
    --type app-image \
    --name InsightPC \
    --app-version "$version" \
    --vendor "Tlcsdm" \
    --description "A cross-platform system information visualizer" \
    --input "$STAGING_DIR" \
    --main-jar insightpc.jar \
    --main-class com.tlcsdm.insightpc.Launcher \
    --java-options "-Xms64m" \
    --java-options "-Xmx256m" \
    --java-options "-XX:MaxMetaspaceSize=128m" \
    --java-options "-XX:+UseG1GC" \
    --dest app-image \
    $icon_opt

if [ $? -ne 0 ]; then
    echo "jpackage failed to create app image" >&2
    exit 1
fi

echo "  App image created at: app-image/InsightPC.app"

# Step 4: Package into zip
echo ""
echo "[4/4] Creating distributable zip..."
mkdir -p dist
zip_name="insightpc-macos-${version}.zip"
rm -f "dist/$zip_name"

cd app-image
zip -r "../dist/$zip_name" .
cd ..

zip_size=$(du -m "dist/$zip_name" | cut -f1)
echo "  Created: dist/$zip_name (${zip_size} MB)"

# Cleanup
rm -rf app-image "$STAGING_DIR"
echo "  Cleaned up temporary directories"

elapsed=$(( $(date +%s) - STEP_START ))
echo ""
echo "App image packaged successfully. (${elapsed}s elapsed)"
