#!/usr/bin/sh

SCRIPT_LOCATION=$(cd "$(dirname "$0")"; pwd)

DIRECTORY_NAME=$1

if [ -d "$DIRECTORY_NAME" ]; then
  echo "Directory $DIRECTORY_NAME already exists"
else
    echo "create directory at $DIRECTORY_NAME"
    mkdir -p $DIRECTORY_NAME
fi