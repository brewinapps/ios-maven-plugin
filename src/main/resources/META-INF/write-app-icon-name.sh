#!/bin/sh

SCRIPT_LOCATION=$(cd "$(dirname "$0")"; pwd)
PLIST_LOCATION=$1
NEW_APP_ICON_NAME=$2

APP_ICON_PREFIX="${NEW_APP_ICON_NAME%%.*}"
APP_ICON_SUFFIX="${NEW_APP_ICON_NAME##*.}"


/usr/libexec/PlistBuddy -c "Delete :CFBundleIcons" $PLIST_LOCATION
/usr/libexec/PlistBuddy -c "Add :CFBundleIcons:CFBundlePrimaryIcon:CFBundleIconFiles array" $PLIST_LOCATION
/usr/libexec/PlistBuddy -c "Add :CFBundleIcons:CFBundlePrimaryIcon:'CFBundleIconFiles':0 string $APP_ICON_PREFIX.$APP_ICON_SUFFIX" $PLIST_LOCATION
/usr/libexec/PlistBuddy -c "Add :CFBundleIcons:CFBundlePrimaryIcon:'CFBundleIconFiles':1 string $APP_ICON_PREFIX@2x.$APP_ICON_SUFFIX" $PLIST_LOCATION
/usr/libexec/PlistBuddy -c "Add :CFBundleIcons:CFBundlePrimaryIcon:'CFBundleIconFiles':2 string $APP_ICON_PREFIX-72.$APP_ICON_SUFFIX" $PLIST_LOCATION
/usr/libexec/PlistBuddy -c "Add :CFBundleIcons:CFBundlePrimaryIcon:'CFBundleIconFiles':3 string $APP_ICON_PREFIX-72@2x.$APP_ICON_SUFFIX" $PLIST_LOCATION


/usr/libexec/PlistBuddy -c "Delete :CFBundleIcons~ipad" $PLIST_LOCATION
/usr/libexec/PlistBuddy -c "Add :CFBundleIcons~ipad:CFBundlePrimaryIcon:CFBundleIconFiles array" $PLIST_LOCATION
/usr/libexec/PlistBuddy -c "Add :CFBundleIcons~ipad:CFBundlePrimaryIcon:'CFBundleIconFiles':0 string $APP_ICON_PREFIX-29.$APP_ICON_SUFFIX" $PLIST_LOCATION
/usr/libexec/PlistBuddy -c "Add :CFBundleIcons~ipad:CFBundlePrimaryIcon:'CFBundleIconFiles':1 string $APP_ICON_PREFIX-40.$APP_ICON_SUFFIX" $PLIST_LOCATION
/usr/libexec/PlistBuddy -c "Add :CFBundleIcons~ipad:CFBundlePrimaryIcon:'CFBundleIconFiles':2 string $APP_ICON_PREFIX-50.$APP_ICON_SUFFIX" $PLIST_LOCATION
/usr/libexec/PlistBuddy -c "Add :CFBundleIcons~ipad:CFBundlePrimaryIcon:'CFBundleIconFiles':3 string $APP_ICON_PREFIX-72.$APP_ICON_SUFFIX" $PLIST_LOCATION
/usr/libexec/PlistBuddy -c "Add :CFBundleIcons~ipad:CFBundlePrimaryIcon:'CFBundleIconFiles':4 string $APP_ICON_PREFIX-76.$APP_ICON_SUFFIX" $PLIST_LOCATION
