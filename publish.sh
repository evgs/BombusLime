#!/bin/sh
GIT_SHORT=`git rev-parse --short HEAD`
GIT_SHA1=`git rev-parse HEAD`
GIT_COUNT=`git rev-list --all | wc -l`

ONAME=BombusLime_${GIT_COUNT}_${GIT_SHORT}.apk
echo $ONAME

./gitinfo.sh
ant debug

cp bin/BombusLime-debug.apk outweb/BombusLime.apk
cp bin/BombusLime-debug.apk outweb/$ONAME


