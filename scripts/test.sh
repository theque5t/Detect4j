#!/bin/bash
Failed=false
echo "Starting jar..."
java -jar "build/libs/Log4jDetector${VERSION:+-VERSION}.jar" &
DetectorPID=$!
echo "Waiting 15 seconds..."
sleep 15
echo "Stopping jar..."
kill $DetectorPID || exit 1
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
        Failed=true
    fi
done
echo "Outputting log..."
echo "<start>"
cat $DetectorLog
echo "<end>"
if $Failed; then
    echo "Failed"
    exit 1
else
    echo "Passed"
fi
