#!/bin/bash
echo "360" | java -jar "build/libs/Log4jDetector${VERSION:+-VERSION}.jar" &   # Start in the background with 5 minute interval
DetectorPID=$!                                                                # Record PID
sleep 5                                                                       # Let run for 5 seconds
# echo $DetectorPID                                                             # Output PID
kill $DetectorPID || exit 1                                                   # Stop or fail process does not exist
cat Log4jDetector/log/app.log                                                 # Output log
line1="$(head -n 1 Log4jDetector/log/app.log)"                                # Get line 1 from log
if [[ "${line1: -5:5}" == "Start" ]]; then                                    # Test if started successfully
    echo "Pass"
    exit 0
else
    echo "Fail"
    exit 1
fi
