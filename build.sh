#!/bin/sh
# build.sh
rm -rf bin
mkdir bin 2>/dev/null

find src -name "*.java" > sources.txt
javac -source 1.6 -d bin -cp bin @sources.txt
cp -r ./resource ./bin/resource

