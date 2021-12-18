#!/bin/bash
java -jar executable.jar &                       # Start it in the background
DetectorPID=$!                                   # Record PID
echo $DetectorPID                                # Output PID
sleep 5                                          # Let run for 5 seconds
kill $DetectorPID                                # Stop it
cat Log4jDetector/log/app.log                    # Output log
line1="$(head -n 1 Log4jDetector/log/app.log)"   # Get line 1 from log
[[ "${line1: -6:5}" == "Start" ]] && echo "Pass"; exit 0; || echo "Fail"; exit 1;
