#!/bin/sh

APP_PATH="$1"
echo $1" - is the path!!"
find "$APP_PATH" -name '*.framework' -type d | while read -r FRAMEWORK
do
##if [ -e "$FRAMEWORK/Info.plist" ]; then
#    # do with info from plist
#    # FRAMEWORK_EXECUTABLE_NAME=$(defaults read "$FRAMEWORK/Info.plist" CFBundleExecutable)
#    # FRAMEWORK_EXECUTABLE_PATH="$FRAMEWORK/$FRAMEWORK_EXECUTABLE_NAME"
#    # lipo -remove
##else
#    #do brutal
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