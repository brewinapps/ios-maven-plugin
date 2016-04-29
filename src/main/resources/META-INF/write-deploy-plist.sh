#!/bin/sh

SCRIPT_LOCATION=$(cd "$(dirname "$0")"; pwd)

PLIST_LOCATION=$1
IPA_LOCATION=$2
ICON_LOCATION=$3
DISPLAY_NAME=$4
BUNDLE_IDENTIFIER=$5
BUNDLE_VERSION=$6
BUILD_NUMBER=$7

/usr/libexec/PlistBuddy -c "Add :items array" $PLIST_LOCATION
/usr/libexec/PlistBuddy -c "Add :items:0:'assets' array" $PLIST_LOCATION
/usr/libexec/PlistBuddy -c "Add :items:0:assets:0:'kind' string 'software-package'" $PLIST_LOCATION
/usr/libexec/PlistBuddy -c "Add :items:0:assets:0:'url' string $IPA_LOCATION" $PLIST_LOCATION

/usr/libexec/PlistBuddy -c "Add :items:0:assets:1:'kind' string 'display-image'" $PLIST_LOCATION
/usr/libexec/PlistBuddy -c "Add :items:0:assets:1:'needs-shine' bool false" $PLIST_LOCATION
/usr/libexec/PlistBuddy -c "Add :items:0:assets:1:'url' string $ICON_LOCATION" $PLIST_LOCATION

/usr/libexec/PlistBuddy -c "Add :items:0:metadata:'kind' string 'software'" $PLIST_LOCATION
/usr/libexec/PlistBuddy -c "Add :items:0:metadata:'title' string $DISPLAY_NAME" $PLIST_LOCATION
/usr/libexec/PlistBuddy -c "Add :items:0:metadata:'bundle-identifier' string $BUNDLE_IDENTIFIER" $PLIST_LOCATION
/usr/libexec/PlistBuddy -c "Add :items:0:metadata:'bundle-version' string $BUNDLE_VERSION" $PLIST_LOCATION
/usr/libexec/PlistBuddy -c "Set :CFBundleVersion $BUILD_NUMBER" $PLIST_LOCATION