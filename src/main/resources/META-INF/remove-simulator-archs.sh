#!/bin/sh

APP_PATH="$1"
cd $APP_PATH

DEPENDENCIES_COPY_PATH="dependencies-copy"
mkdir -p $DEPENDENCIES_COPY_PATH
find . -name '*.framework' -type d | while read -r FRAMEWORK
do
    echo "Trying to remove simulator architectures from framework: ""$FRAMEWORK"

    FRAMEWORK_DIR=$(dirname "$FRAMEWORK")
    FRAMEWORK_DIR="$DEPENDENCIES_COPY_PATH/${FRAMEWORK_DIR//.\/}"
    echo "Saving a copy of the framework with all architectures to $FRAMEWORK_DIR"
    mkdir -p $FRAMEWORK_DIR
    cp -a "$FRAMEWORK" $FRAMEWORK_DIR

    for subFile in "$FRAMEWORK"/*; do
    if [[ -f "$subFile" ]]; then
        if ! [ "${subFile##*.}" = "plist" ]; then
            if lipo $subFile -verify_arch i386; then
                echo "Trying to remove i386 from $subFile"
                lipo -remove i386 "$subFile" -o "$subFile";
            fi

            if lipo $subFile -verify_arch x86_64; then
                echo "Trying to remove x86_64 from $subFile"
                lipo -remove x86_64 "$subFile" -o "$subFile";
            fi
        fi
    fi
    done
##fi
done
#fi
