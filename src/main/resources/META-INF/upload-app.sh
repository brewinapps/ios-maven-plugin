#!/bin/sh

IPA_LOCATION=$1
USERNAME=$2
PASSWORD=$3

xcrun altool --upload-app -f $IPA_LOCATION -u $USERNAME -p $PASSWORD