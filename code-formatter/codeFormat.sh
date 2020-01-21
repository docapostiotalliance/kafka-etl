#!/bin/bash

if [[ $# -lt 1 ]]; then
    echo "Error : invalid parameter !" >&2
    echo "Use -h opt to get more informations" >&2
    exit 1
fi

dir="$(cd "$(dirname "$0")"; pwd -P)"
jar="$dir/bin/code-formatter-jar-with-dependencies.jar"
codeStyleParam="-DcodeStyle=$dir/../code-style/code-style.xml"
package="com.docapost.posc.tools.code.formatter"
patterns="\.java$"

# filtering files to keep only java files
files=""
for file in $@ ; do
  matched=$(echo "$file" | grep -Ei "$patterns")
  if [[ -f $matched ]] ; then
    files="$files $matched"
  fi
done

JAVA_OPTS="$codeStyleParam"

result="$(java $JAVA_OPTS -cp $jar $package.FormatterApp $files)"

block_result() {
    out="$1"
    if [[ $out =~ ^.*CAUTION.*$ ]]; then
        echo "Error: there are unformatted file"
        echo "$out"
        exit 1
    fi
    exit 0
}

usage() {
      echo "usage: ./codeFormat.sh [file1.java file2.java] [opts] ..."
      echo "-b : block if there are unformatted file"
      echo "-h : display helps"
}

while getopts ":bh" option; do
    case "$option" in
    	b) block_result "$result" ;;
        h) usage ;;
    esac
done

echo "$result"
