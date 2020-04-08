#!/bin/sh

APP_PATH="$1"
echo "Hello here is the begin of the script"
find "$APP_PATH" -name '*.framework' -type d | while read -r FRAMEWORK
do
    echo "Trying to remove simulator architectures from framework: ""$FRAMEWORK"
    mkdir ../tmp-target
    cp -a "$FRAMEWORK" ../tmp-target/
    for subFile in "$FRAMEWORK"/*; do
    if [[ -f "$subFile" ]]; then
    if [[ -x "$subFile" ]]; then
        echo "Trying to remove i386 from $subFile"
        lipo -remove i386 "$subFile" -o "$subFile";
        echo "Trying to remove x86_64 from $subFile"
        lipo -remove x86_64 "$subFile" -o "$subFile";
    fi
    fi
    done
##fi
done
#fi
