#!/bin/bash

# Start the ConnectionServer in the background
java ConnectionServer > test1/ConnectionServer.log 2>&1 &

# Start each node in the background and redirect output to individual text files
java -cp .:./json.jar Node M1 True INSTANT > test1/M1.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M2 True M2 > test1/M2.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M3 True M3 > test1/M3.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M4 False INSTANT > test1/M4.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M5 False INSTANT > test1/M5.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M6 False INSTANT > test1/M6.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M7 False INSTANT > test1/M7.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M8 False INSTANT > test1/M8.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M9 False INSTANT > test1/M9.txt 2>&1 &
sleep 1

echo "Servers and nodes have been started."

# Wait for all background processes to finish
sleep 15 # Wait for 15 seconds

kill $(pgrep -f "ConnectionServer")
echo "All processes have finished."
