#!/bin/sh

export PATH=$PATH:/Applications/Xcode.app/Contents/Applications/Application\ Loader.app/Contents/Frameworks/ITunesSoftwareService.framework/Versions/Current/Support/:/Applications/Xcode.app/Contents/Applications/Application\ Loader.app/Contents/itms/:/Applications/Xcode.app/Contents/Applications/Application\ Loader.app/Contents/itms/bin

SCRIPT_LOCATION=$(cd "$(dirname "$0")"; pwd)

IPA_LOCATION=$1
USERNAME=$2
PASSWORD=$3

altool --upload-app -f $IPA_LOCATION -u $USERNAME -p $PASSWORD