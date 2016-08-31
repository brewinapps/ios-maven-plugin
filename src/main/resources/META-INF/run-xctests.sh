#!/bin/sh

SCRIPT_LOCATION=$(cd "$(dirname "$0")"; pwd)

SCHEME=$1
CONFIGURATION=$2
SDK=$3

xcodebuild -scheme $SCHEME -configuration $CONFIGURATION -sdk $SDK clean test 2>&1 | tee test-results.txt
cat test-results.txt |ocunit2junit