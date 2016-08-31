#!/bin/sh

SCRIPT_LOCATION=$(cd "$(dirname "$0")"; pwd)

IPA_LOCATION=$1
USERNAME=$2
PASSWORD=$3
XCODE_VERSION=$4

export PATH=$PATH:/Applications/Xcode.app/Contents/Applications/Application\ Loader.app/Contents/Frameworks/ITunesSoftwareService.framework/Versions/Current/Support/:/Applications/Xcode.app/Contents/Applications/Application\ Loader.app/Contents/itms/:/Applications/Xcode.app/Contents/Applications/Application\ Loader.app/Contents/itms/bin
export PATH=$PATH:/Applications/Xcode-7.app/Contents/Applications/Application\ Loader.app/Contents/Frameworks/ITunesSoftwareService.framework/Versions/Current/Support/:/Applications/Xcode-7.app/Contents/Applications/Application\ Loader.app/Contents/itms/:/Applications/Xcode-7.app/Contents/Applications/Application\ Loader.app/Contents/itms/bin
export PATH=$PATH:$XCODE_VERSION/Contents/Applications/Application\ Loader.app/Contents/Frameworks/ITunesSoftwareService.framework/Versions/Current/Support/:$XCODE_VERSION/Contents/Applications/Application\ Loader.app/Contents/itms/:$XCODE_VERSION/Contents/Applications/Application\ Loader.app/Contents/itms/bin

altool --upload-app -f $IPA_LOCATION -u $USERNAME -p $PASSWORD