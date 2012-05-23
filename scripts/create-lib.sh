#!/bin/sh

#SPAN - Smart Phone AdHoc Networking project
#©2012 The MITRE Corporation

# usage: 
# ./scripts/create-lib.sh


# archive all classes

# jar cvf libmanet.jar -C ./bin/classes .


# selectively archive classes

cd ./bin/classes

jar cvf libmanet.jar \
'./android/adhoc/manet/ManetObserver.class' \
'./android/adhoc/manet/ManetHelper.class' \
'./android/adhoc/manet/ManetHelper$IncomingHandler.class' \
'./android/adhoc/manet/ManetHelper$ManetBroadcastReceiver.class' \
'./android/adhoc/manet/ManetHelper$ManetServiceConnection.class' \
'./android/adhoc/manet/system/ManetConfig.class' \
'./android/adhoc/manet/system/ManetConfig$'* \
'./android/adhoc/manet/system/DeviceConfig.class' \
'./android/adhoc/manet/service/ManetService.class' \
'./android/adhoc/manet/service/ManetService$AdhocStateEnum.class'

mv libmanet.jar ../..

cd ../..


# copy jar into other project(s)
# copy will fail if destination dir does not exist

cp libmanet.jar ../../android-manet-manager/AndroidManetManager/lib

