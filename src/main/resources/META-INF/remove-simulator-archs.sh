#!/bin/sh

APP_PATH="$1"
echo $1" - is the path!!"
find "$APP_PATH" -name '*.framework' -type d | while read -r FRAMEWORK
do
    echo "Trying to remove simulator architectures from framework: ""$FRAMEWORK"
    for subFile in "$FRAMEWORK"/*; do
    if [[ -f "$subFile" ]]; then
    if [[ -x "$subFile" ]]; then
        lipo -remove i386 "$subFile" -o "$subFile" >> 2&>1 /dev/null;
        lipo -remove x86_64 "$subFile" -o "$subFile" >> 2&>1 /dev/null;
    fi
    fi
    done
##fi
done
#fi
