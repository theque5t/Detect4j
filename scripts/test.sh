#!/bin/bash
java -jar build/libs/Log4jDetector.jar &         # Start it in the background
DetectorPID=$!                                   # Record PID
echo $DetectorPID                                # Output PID
sleep 5                                          # Let run for 5 seconds
cat Log4jDetector/log/app.log                    # Output log
line1="$(head -n 1 Log4jDetector/log/app.log)"   # Get line 1 from log
echo "${line1: -6:5}"
if [[ "${line1: -6:5}" == "Start" ]]; then       # Test if started successfully
    echo "Pass"
    exit 0
else
    echo "Fail"
    exit 1
fi
