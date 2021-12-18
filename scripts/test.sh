#!/bin/bash
echo "Started jar..."
echo "5" | java -jar "build/libs/Log4jDetector${VERSION:+-VERSION}.jar" &     # Start in the background with 5 second interval
DetectorPID=$!                                                                # Record PID
(sleep 15 && kill $DetectorPID) || echo "exit 1"                              # Let run for 15 seconds and stop, or fail if process does not exist
echo "Stopped jar..."
echo "Outputting log..."
echo "<start>"
cat Log4jDetector/log/app.log                                                 # Output log
echo "<end>"
line1="$(head -n 1 Log4jDetector/log/app.log)"                                # Get line 1 from log
if [[ "${line1: -5:5}" == "Start" ]]; then                                    # Test if started successfully
    echo "Pass"
    exit 0
else
    echo "Fail"
    exit 1
fi
