#!/bin/bash

# Start the ConnectionServer in the background
java ConnectionServer > test2/ConnectionServer.log 2>&1 &

# Start each node in the background and redirect output to individual text files
java -cp .:./json.jar Node M1 True INSTANT > test2/M1.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M2 True M2 > test2/M2.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M3 True M3 > test2/M3.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M4 False Small > test2/M4.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M5 False Small > test2/M5.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M6 False Small > test2/M6.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M7 False Small > test2/M7.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M8 False Small > test2/M8.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M9 False Small > test2/M9.txt 2>&1 &
sleep 1

echo "Servers and nodes have been started."

# Wait for all background processes to finish
sleep 15 # Wait for 15 seconds

kill $(pgrep -f "ConnectionServer")
echo "All processes have finished."
