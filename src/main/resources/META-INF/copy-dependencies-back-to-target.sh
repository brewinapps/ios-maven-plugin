#!/bin/sh

APP_PATH="$1"
cd "$APP_PATH"
if [[ -d dependecies-copy ]]; then
    echo "Original Frameworks will be copied back for testing to dependencies"
    rm -rf ios-dependencies
    cp -a dependecies-copy ios-dependencies
    rm -rf dependecies-copy
fi