#!/bin/sh

APP_PATH="$1"
cd "$APP_PATH"
if [[ -d ../tmp-target ]]; then
    echo "Dependencies will be copied back to target from tmp-target"
    rm -rf ios-dependencies
    cp -a ../tmp-target ios-dependencies
    rm -rf ../tmp-target
fi