#!/bin/bash

# Start the ConnectionServer in the background
java -cp .:./json.jar ConnectionServer > test3/ConnectionServer.log 2>&1 &

# Start each node in the background and redirect output to individual text files
java -cp .:./json.jar Node M1 True M2 > test3/M1.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M2 True M2 > test3/M2.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M3 True M2 > test3/M3.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M4 False M2 > test3/M4.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M5 False M2 > test3/M5.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M6 False M2 > test3/M6.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M7 False M2 > test3/M7.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M8 False M2 > test3/M8.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M9 False M2 > test3/M9.txt 2>&1 &
sleep 1

echo "Servers and nodes have been started."

# Wait for all background processes to finish
sleep 15 # Wait for 15 seconds

kill $(pgrep -f "ConnectionServer")
echo "All processes have finished."