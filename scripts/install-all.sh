#!/bin/sh

# usage: 
# ./scripts/install-all.sh

#http://www.cs.utah.edu/dept/old/texinfo/gawk/gawk_4.html#SEC9
#http://www.tek-tips.com/faqs.cfm?fid=1281

# PREBUILT ANDROID SDK [has valid targets]
# export PATH=$PATH:~/Desktop/android-sdk-linux_x86/tools
# export PATH=$PATH:~/Desktop/android-sdk-linux_x86/platform-tools

# create build.xml
#
# android list targets
#
#   id: 9 or "android-10"
#      Name: Android 2.3.3
#      Type: Platform
#      API level: 10
#      Revision: 2
#      Skins: WQVGA432, QVGA, WQVGA400, HVGA, WVGA800 (default), WVGA854
#
# android update project --path . --target 9

# build apk
# ant debug


# variables
apk='./bin/AndroidManetService.apk'

# load each device
adb devices | awk '$2 == "device" { print "adb -s "$1" install -r '"$apk"'" }' | sh -x


# variables
apk='../../android-manet-manager/AndroidManetManager/bin/AndroidManetManager.apk'

# load each device
adb devices | awk '$2 == "device" { print "adb -s "$1" install -r '"$apk"'" }' | sh -x


