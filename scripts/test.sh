#!/bin/bash
echo "Started jar..."
( cmdpid=$BASHPID; (sleep 15; kill $cmdpid || exit 1 ) & echo "5" | exec java -jar "build/libs/Log4jDetector${VERSION:+-VERSION}.jar" )
# echo "5" | java -jar "build/libs/Log4jDetector${VERSION:+-VERSION}.jar" &     # Start in the background with 5 second interval
# DetectorPID=$!                                                                # Record PID
# sleep 15                                                                      # Let run for 5 seconds
# echo $DetectorPID                                                           # Output PID
# kill $DetectorPID || exit 1                                                   # Stop or fail process does not exist
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
