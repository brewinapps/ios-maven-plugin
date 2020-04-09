#!/bin/sh

APP_PATH="$1"
cd "$APP_PATH"
if [[ -d dependencies-copy ]]; then
    echo "Original Frameworks will be copied back for testing to dependencies"
    rm -rf ios-dependencies
    cp -a dependencies-copy ios-dependencies
    rm -rf dependencies-copy
fi