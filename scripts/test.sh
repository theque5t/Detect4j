#!/bin/bash
echo "Starting jar..."
java -jar "build/libs/Log4jDetector${VERSION:+-VERSION}.jar" &
DetectorPID=$!
echo "Stopping jar..."
(sleep 15 && kill $DetectorPID) || exit 1
echo "Testing..."
declare -a logPatterns=(
    "Searching for JVMs" 
    "Found JVM"
    "does not match target pattern"
)
DetectorLog="Log4jDetector/log/app.log"
for pattern in "${logPatterns[@]}"
do
    echo "Testing for pattern: $pattern"
    if grep -q "$pattern" "$DetectorLog"; then
        echo "Passed: $pattern found"
    else
        echo "Failed: $pattern found"
        exit 1
    fi
done
echo "Outputting log..."
echo "<start>"
cat $DetectorLog
echo "<end>"
