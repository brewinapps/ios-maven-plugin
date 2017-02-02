#!/bin/bash

SCRIPT_LOCATION=$(cd "$(dirname "$0")"; pwd)

SCHEME=$1
CONFIGURATION=$2
SDK=$3
ARCHS=$4
DESTINATION=$5

eval "xcodebuild -scheme $SCHEME -configuration $CONFIGURATION -sdk $SDK ARCHS='$ARCHS' VALID_ARCHS='$ARCHS' -destination '$DESTINATION' clean test 2>&1 | tee test-results.txt"

cat test-results.txt |ocunit2junit