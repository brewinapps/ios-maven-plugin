#!/bin/sh

SCRIPT_LOCATION=$(cd "$(dirname "$0")"; pwd)

osascript -e 'tell application "iOS Simulator" to quit'
osascript -e 'tell application "Simulator" to quit'
xcrun simctl erase all