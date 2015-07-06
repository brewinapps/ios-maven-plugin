#!/bin/bash

unset HISTFILE

#security unlock-keychain -p $0 $1 > /dev/null 2>&1

security -v unlock-keychain -p $1 $2

#echo "ECHO# security unlock-keychain -p $1 $2"

if [ $? -ne 0 ];then
echo "Cannot open keychain $1"
exit 1
fi

echo "Successfully unlocked keychain $1"