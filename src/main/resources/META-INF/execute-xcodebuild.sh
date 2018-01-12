#!/bin/sh

SCRIPT_LOCATION=$(cd "$(dirname "$0")"; pwd)

XCODEBUILD_COMMAND=$1
TMPDIR=$2

echo "----------------------------------------------------------------------------"
echo "executing $XCODEBUILD_COMMAND, TMPDIR: $TMPDIR"
echo "----------------------------------------------------------------------------"
export TMPDIR=$TMPDIR
eval "$XCODEBUILD_COMMAND"
