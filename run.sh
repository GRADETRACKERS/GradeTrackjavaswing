#!/bin/sh
cd "$(dirname "$0")"
mkdir -p out
javac -d out $(find src -name '*.java') && java -cp out gradetrack.Main
