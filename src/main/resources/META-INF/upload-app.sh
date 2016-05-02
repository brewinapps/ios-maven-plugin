#!/bin/sh

SCRIPT_LOCATION=$(cd "$(dirname "$0")"; pwd)

IPA_LOCATION=$1
USERNAME=$2
PASSWORD=$3

altool --upload-app -f $IPA_LOCATION -u $USERNAME -p $PASSWORD