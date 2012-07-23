#!/bin/sh

SCRIPT_LOCATION=$(cd "$(dirname "$0")"; pwd)
PLIST_LOCATION=$1
NEW_BUILD_NUMBER=$2

echo "OLD BUILD NUMBER=$(/usr/libexec/PlistBuddy -c "Print CFBuildNumber" $PLIST_LOCATION)"
echo "NEW BUILD NUMBER= $NEW_BUILD_NUMBER"

echo "Write NEW Build Number into PLIST now"
/usr/libexec/PlistBuddy -c "Set :CFBuildNumber $NEW_BUILD_NUMBER" $PLIST_LOCATION