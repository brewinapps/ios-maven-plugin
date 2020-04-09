#!/bin/sh

APP_PATH="$1"
echo "Dependencies will be copied back to target from tmp-target"
cd "$APP_PATH"
rm -rf ios-dependencies
cp -a ../tmp-target ios-dependencies
rm -rf ../tmp-target
