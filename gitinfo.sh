#!/bin/sh
OUT=res/values/gitinfo.xml
GIT_SHORT=`git rev-parse --short HEAD`
GIT_SHA1=`git rev-parse HEAD`
GIT_COUNT=`git rev-list --all | wc -l`

echo "<?xml version=\"1.0\" encoding=\"utf-8\"?>" > $OUT
echo "<resources>" >>$OUT
echo "  <string name=\"git_count\">$GIT_COUNT</string>" >>$OUT
echo "  <string name=\"git_short\">$GIT_SHORT</string>" >>$OUT
echo "  <string name=\"git_sha1\">$GIT_SHA1</string>" >>$OUT
echo "</resources>" >>$OUT
