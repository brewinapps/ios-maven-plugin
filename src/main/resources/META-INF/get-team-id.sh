#!/bin/sh

SCRIPT_LOCATION=$(cd "$(dirname "$0")"; pwd)
PROVISIONING_PROFILE_LOCATION=$1

/usr/libexec/PlistBuddy -c "Print :Entitlements:com.apple.developer.team-identifier" /dev/stdin <<< $(security cms -D -i 2>/dev/null $PROVISIONING_PROFILE_LOCATION)