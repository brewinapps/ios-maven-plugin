#!/bin/bash

SCRIPT_LOCATION=$(cd "$(dirname "$0")"; pwd)

SCHEME=$1
CONFIGURATION=$2
SDK=$3
ARCHS=$4
DESTINATION=$5
OTHER_ARGS=$6
XCPRETTY_COMMAND=$7
BUILD_COMMAND_WRAPPER_COMMAND=$8

echo "----------------------------------------------------------------------------"
echo "$BUILD_COMMAND_WRAPPER_COMMAND xcodebuild -scheme $SCHEME -configuration $CONFIGURATION -sdk $SDK ARCHS='$ARCHS' VALID_ARCHS='$ARCHS' -destination '$DESTINATION' $OTHER_ARGS clean test 2>&1 $XCPRETTY_COMMAND"
echo "----------------------------------------------------------------------------"
eval "$BUILD_COMMAND_WRAPPER_COMMAND xcodebuild -scheme $SCHEME -configuration $CONFIGURATION -sdk $SDK ARCHS='$ARCHS' VALID_ARCHS='$ARCHS' -destination '$DESTINATION' $OTHER_ARGS clean test 2>&1 $XCPRETTY_COMMAND"

cat test-results.txt | ocunit2junit > /dev/null