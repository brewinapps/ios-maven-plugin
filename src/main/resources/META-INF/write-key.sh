#!/bin/sh

SCRIPT_LOCATION=$(cd "$(dirname "$0")"; pwd)
PLIST_LOCATION=$1
KEY=$2
VALUE=$3

echo "NEW KEY= $KEY"
echo "NEW VALUE= $VALUE"

echo "Write NEW $KEY with value $VALUE into PLIST now"
/usr/libexec/PlistBuddy -c "Delete :$KEY" $PLIST_LOCATION
/usr/libexec/PlistBuddy -c "Add :$KEY string $VALUE" $PLIST_LOCATION