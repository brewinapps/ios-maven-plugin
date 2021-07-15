#!/bin/sh

SCRIPT_LOCATION=$(cd "$(dirname "$0")"; pwd)

CARTHAGE_BUILD_COMMAND=$1

echo "----------------------------------------------------------------------------"
echo "executing $CARTHAGE_BUILD_COMMAND"
echo "----------------------------------------------------------------------------"
eval "$CARTHAGE_BUILD_COMMAND"
