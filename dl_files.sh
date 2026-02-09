#!/bin/sh

DIR=$(dirname "$(realpath $0)")
mkdir -p $DIR/app/src/main/assets
cd $DIR/app/src/main/assets


if ! [  -f "APatch-KSU.zip" ]; then
    curl -L -k  -o   APatch-KSU.zip  https://github.com/definitly486/redmia5/releases/download/root/APatch-KSU.zip
fi

if ! [  -f "KernelSU_v1.0.5_12081-release.apk" ]; then
    curl -L -k  -o   APatch_11107_11107-release-signed.apk  https://github.com/definitly486/redmia5/releases/download/apk/KernelSU_v1.0.5_12081-release.apk
fi