#!/bin/sh

APP_PATH="$1"
cd "$APP_PATH"

DEPENDENCIES_COPY_PATH="dependencies-copy"

if [[ -d $DEPENDENCIES_COPY_PATH ]]; then
    echo "Original Frameworks will be copied back for testing to dependencies"
    cp -a "$DEPENDENCIES_COPY_PATH/." ./
    rm -rf $DEPENDENCIES_COPY_PATH
fi