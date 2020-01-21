#!/bin/sh

set -o noclobber
set -o errexit
set -o nounset

dir="$(cd "$(dirname "$0")"; pwd -P)"

testFile1="$dir/src/main/java/com/docapost/posc/tools/code/formatter/Formatter.java"
testFile2="$dir/src/main/java/com/docapost/posc/tools/code/formatter/FormatterApp.java"
testFile3="$dir/src/main/java/com/docapost/posc/tools/code/formatter/CodeStyleParser.java"
testFile4="$dir/pom.xml"
testFiles="$testFile1 $testFile2 $testFile3 $testFile4"

$dir/codeFormat.sh $testFiles
