#!/bin/sh

SCRIPT_LOCATION=$(cd "$(dirname "$0")"; pwd)

osascript -e 'tell application "iOS Simulator" to quit'
osascript -e 'tell application "Simulator" to quit'
killall "Simulator" 2> /dev/null
xcrun simctl shutdown all
xcrun simctl list | grep Booted | awk -F "[()]" '{ for (i=2; i<NF; i+=2) print $i }' | grep '^[-A-Z0-9]*$' | xargs -I uuid xcrun simctl shutdown uuid
xcrun simctl erase all