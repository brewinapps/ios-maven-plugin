#!/bin/sh

SCRIPT_LOCATION=$(cd "$(dirname "$0")"; pwd)
PLIST_LOCATION=$1
NEW_DISPLAY_NAME=$2

echo "OLD DISPLAY NAME=$(/usr/libexec/PlistBuddy -c "Print CFBundleDisplayName" $PLIST_LOCATION)"
echo "NEW DISPLAY NAME= $NEW_DISPLAY_NAME"

echo "Write NEW DISPLAY NAME into PLIST now"
/usr/libexec/PlistBuddy -c "Set :CFBundleDisplayName $NEW_DISPLAY_NAME" $PLIST_LOCATION