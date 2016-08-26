#!/bin/sh

SCRIPT_LOCATION=$(cd "$(dirname "$0")"; pwd)

XCODE_VERSION=$1

xcode-select -s $XCODE_VERSION