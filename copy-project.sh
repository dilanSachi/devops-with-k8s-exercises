#!/bin/bash

# Usage: ./copy-replace.sh <old_project_name> <new_project_name>

if [ $# -ne 2 ]; then
    echo "Usage: $0 <old_project_name> <new_project_name>"
    exit 1
fi

SRC="$1"
DEST="$2"

if [ ! -d "$SRC" ]; then
    echo "Error: Source directory '$SRC' does not exist."
    exit 1
fi


# Create destination directory
mkdir -p "$DEST"

# Copy entire folder recursively
cp -r "$SRC"/* "$DEST/" 2>/dev/null || {
    echo "Error: Failed to copy contents from '$SRC' to '$DEST'"
    exit 1
}

find "$DEST" -type f \( -name "*" \) -exec sed -i "s/${SRC}/${DEST}/g" {} +

OLD_VER="${SRC:2:1}.${SRC:3:2}"
NEW_VER="${DEST:2:1}.${DEST:3:2}"
sed -i "s/${OLD_VER}/${NEW_VER}/g" "${DEST}/README.md"
sed -i "s/${SRC}/${DEST}/g" .github/workflows/*

echo "Successfully copied '$SRC' to '$DEST'"
