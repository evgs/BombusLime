#!/bin/sh
GIT_SHORT=`git rev-parse --short HEAD`
GIT_SHA1=`git rev-parse HEAD`
GIT_COUNT=`git rev-list --all | wc -l`

ONAME=BombusLime_${GIT_COUNT}_${GIT_SHORT}.apk

./gitinfo.sh
ant clean
ant debug

if [ "$?" -ne "0" ]; then
  echo "*** BUILD ERROR ***"
  exit 1
fi

echo Copying $ONAME

cp bin/BombusLime-debug.apk outweb/BombusLime.apk
cp bin/BombusLime-debug.apk outweb/$ONAME

