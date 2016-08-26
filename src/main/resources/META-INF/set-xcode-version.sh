#!/bin/sh

SCRIPT_LOCATION=$(cd "$(dirname "$0")"; pwd)

XCODE_VERSION=$1

sudo xcode-select -s $XCODE_VERSION