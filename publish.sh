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

mkdir -p outweb
cp bin/BombusLime-debug.apk outweb/$ONAME
ln -sf $ONAME outweb/BombusLime.apk
