#!/bin/sh

filePath=$1

eval "security cms -D -i $filePath"
