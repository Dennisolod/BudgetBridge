#!/usr/bin/env bash
#
# copy_fxml.sh
# Copies all .fxml files from target/classes/groupid to src/java/groupid
# and overwrites any existing files with the same names.

set -euo pipefail

SRC_DIR="target/classes/groupid"
DEST_DIR="src/java/groupid"

# Make sure the destination exists.
mkdir -p "$DEST_DIR"

# Copy each FXML file, forcing overwrite (-f).
for fxml in "$SRC_DIR"/*.fxml; do
    # Skip if the glob didn’t match anything.
    [[ -e "$fxml" ]] || { echo "No FXML files found in $SRC_DIR" >&2; exit 1; }

    cp -f "$fxml" "$DEST_DIR/"
    echo "Copied $(basename "$fxml") → $DEST_DIR/"
done

echo "✓ All FXML files copied."

