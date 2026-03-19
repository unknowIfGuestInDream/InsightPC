#!/bin/bash

#
# Create a minimal custom JRE using jlink (macOS).
#
# If the current environment already has Java 21, uses it directly.
# Otherwise downloads the Amazon Corretto JDK.
# Uses jdeps to analyze the application jar for required JDK modules, then creates
# a custom runtime image with jlink containing only those modules.
# This significantly reduces the JRE size compared to a full JRE download.
#
# Usage: jre_mac.sh [staging_dir]
#   staging_dir: Path to the staging directory containing the application jar.
#                Defaults to the current directory ('.').
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

STAGING_DIR="${1:-.}"
STEP_START=$(date +%s)

echo "========================================"
echo " InsightPC - Custom JRE (jlink)"
echo " Started: $(date '+%Y-%m-%d %H:%M:%S')"
echo "========================================"

if [ ! -d "$STAGING_DIR" ]; then
    echo "Staging directory not found: $STAGING_DIR" >&2
    exit 1
fi
echo "  Staging dir: $STAGING_DIR"

cd "$STAGING_DIR"

# Step 1: Locate JDK 21
downloaded_jdk=false
jdk_dir=""

# Check if current environment already has Java 21
if command -v java >/dev/null 2>&1; then
    java_ver=$(java -version 2>&1 | head -1)
    major_ver=$(echo "$java_ver" | sed -n 's/.*"\([0-9]*\)[.+].*/\1/p')
    if [ "$major_ver" = "21" ]; then
        if [ -n "$JAVA_HOME" ] && [ -d "$JAVA_HOME/bin" ] && [ -x "$JAVA_HOME/bin/jlink" ]; then
            jdk_dir="$JAVA_HOME"
        else
            java_path=$(command -v java)
            real_path=$(readlink -f "$java_path" 2>/dev/null || echo "$java_path")
            candidate=$(dirname "$(dirname "$real_path")")
            if [ -x "$candidate/bin/jlink" ]; then
                jdk_dir="$candidate"
            fi
        fi
    fi
fi

if [ -n "$jdk_dir" ]; then
    echo ""
    echo "[1/4] Using existing Java 21 from environment"
    echo "  JDK directory: $jdk_dir"
else
    # see https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/downloads-list.html
    mac_api='https://corretto.aws/downloads/latest/amazon-corretto-21-aarch64-macos-jdk.tar.gz'

    echo ""
    echo "[1/4] Downloading Amazon Corretto JDK..."
    echo "  URL: $mac_api"
    dl_start=$(date +%s)
    curl -L -o jdk.tar.gz "$mac_api"
    dl_elapsed=$(( $(date +%s) - dl_start ))
    dl_size=$(du -m jdk.tar.gz | cut -f1)
    echo "  Downloaded: ${dl_size} MB (${dl_elapsed}s)"

    echo "  Extracting..."
    tar -xzf jdk.tar.gz
    rm -f jdk.tar.gz

    # Find the extracted JDK directory (Corretto on macOS extracts to amazon-corretto-*-jdk/Contents/Home)
    jdk_top=$(find . -maxdepth 1 -type d -name 'amazon-corretto-*' | head -1)
    if [ -z "$jdk_top" ]; then
        jdk_top=$(find . -maxdepth 1 -type d -name 'jdk*' | head -1)
    fi
    if [ -z "$jdk_top" ]; then
        echo "No JDK directory found after extraction." >&2
        exit 1
    fi
    # Corretto macOS layout: amazon-corretto-21.jdk/Contents/Home
    if [ -d "$jdk_top/Contents/Home/bin" ]; then
        jdk_dir="$jdk_top/Contents/Home"
    else
        jdk_dir="$jdk_top"
    fi
    echo "  JDK directory: $jdk_dir"
    downloaded_jdk=true
    jdk_top_cleanup="$jdk_top"
fi

jdeps_cmd="$jdk_dir/bin/jdeps"
jlink_cmd="$jdk_dir/bin/jlink"

# Step 2: Find and analyze jar
jar=$(find . -maxdepth 1 -name '*.jar' -type f | head -1)
if [ -z "$jar" ]; then
    echo "No jar file found in current directory" >&2
    exit 1
fi
jar_name=$(basename "$jar")
jar_size=$(du -m "$jar" | cut -f1)

echo ""
echo "[2/4] Analyzing module dependencies..."
echo "  Jar: $jar_name (${jar_size} MB)"

# Use jdeps to determine required JDK modules
# When a lib/ directory exists, include it on the module-path so jdeps can
# resolve transitive dependencies from all library jars.
modules=""
jdeps_args="--ignore-missing-deps --multi-release 21 --print-module-deps"
if [ -d "lib" ]; then
    jdeps_args="$jdeps_args --module-path lib --add-modules ALL-MODULE-PATH"
fi
modules=$("$jdeps_cmd" $jdeps_args "$jar_name" 2>/dev/null | tail -1) || true

if [ -z "$modules" ] || [ "$(echo "$modules" | tr -d '[:space:]')" = "" ]; then
    # Fallback: conservative set covering JavaFX, OSHI system info,
    # Preferences API, XML processing, and sun.misc.Unsafe access.
    # Derived from module-info.java requires and transitive runtime dependencies.
    modules='java.base,java.desktop,java.logging,java.management,java.prefs,java.xml,jdk.unsupported'
    echo "  jdeps analysis failed, using fallback modules"
else
    modules=$(echo "$modules" | tr -d '[:space:]')
fi
echo "  Modules: $modules"

# Step 3: Create custom JRE
echo ""
echo "[3/4] Creating custom JRE with jlink..."
rm -rf jre

# Create custom JRE with size optimizations:
#   --strip-debug:    Remove debug info to reduce size
#   --no-man-pages:   Exclude man pages
#   --no-header-files: Exclude C header files
#   --compress zip-9: Maximum compression
"$jlink_cmd" --add-modules "$modules" --output jre --strip-debug --no-man-pages --no-header-files --compress zip-9
if [ $? -ne 0 ]; then
    echo "jlink failed to create custom runtime" >&2
    exit 1
fi

jre_size=$(du -sm jre | cut -f1)
echo "  JRE size: ${jre_size} MB"

# Step 4: Clean up downloaded JDK
echo ""
echo "[4/4] Cleaning up..."
if [ "$downloaded_jdk" = true ]; then
    rm -rf "${jdk_top_cleanup:-$jdk_dir}"
    echo "  Removed JDK directory"
else
    echo "  Skipped (using system JDK)"
fi

elapsed=$(( $(date +%s) - STEP_START ))
echo ""
echo "Custom JRE created successfully. (${elapsed}s elapsed)"
