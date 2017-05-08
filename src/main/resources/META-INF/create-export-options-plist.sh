#!/bin/sh

SCRIPT_LOCATION=$(cd "$(dirname "$0")"; pwd)

EXPORT_OPTIONS=$1
FILE_PATH=$2

echo "$EXPORT_OPTIONS" | plutil -convert xml1 -o "$FILE_PATH" -- -