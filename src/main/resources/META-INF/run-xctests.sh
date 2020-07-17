#!/bin/bash

SCRIPT_LOCATION=$(cd "$(dirname "$0")"; pwd)

SCHEME=$1
CONFIGURATION=$2
DESTINATION=$3
OTHER_ARGS=$4
XCPRETTY_COMMAND=$5
BUILD_COMMAND_WRAPPER_COMMAND=$6

echo "----------------------------------------------------------------------------"
echo "$BUILD_COMMAND_WRAPPER_COMMAND xcodebuild -scheme $SCHEME -configuration $CONFIGURATION -destination '$DESTINATION' $OTHER_ARGS clean test 2>&1 $XCPRETTY_COMMAND"
echo "----------------------------------------------------------------------------"
eval "$BUILD_COMMAND_WRAPPER_COMMAND xcodebuild -scheme $SCHEME -configuration $CONFIGURATION -destination '$DESTINATION' $OTHER_ARGS clean test 2>&1 $XCPRETTY_COMMAND"

cat test-results.txt | ocunit2junit > /dev/null