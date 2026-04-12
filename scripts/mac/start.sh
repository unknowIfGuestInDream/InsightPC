#!/bin/bash

#
# Copyright (c) 2026 unknowIfGuestInDream.
# All rights reserved.
#

APP_NAME=insightpc.jar

tpid=$(ps -ef|grep $APP_NAME|grep -v grep|grep -v kill|awk '{print $2}')
if [ ${tpid} ]; then
echo 'Stop Process...'
kill -2 $tpid
fi

tpid=$(ps -ef|grep $APP_NAME|grep -v grep|grep -v kill|awk '{print $2}')
if [ ${tpid} ]; then
echo 'Stop Process...'
kill -2 $tpid
else
echo 'Stop Process Successfully!'
echo 'start Process...'
NATIVE_ACCESS="--enable-native-access=javafx.graphics,com.sun.jna,ALL-UNNAMED"
if [ -f "./jre/bin/java" ];then
  nohup jre/bin/java $NATIVE_ACCESS -jar $APP_NAME > nohup.out &
else
  nohup java $NATIVE_ACCESS -jar $APP_NAME > nohup.out &
fi
fi
