#!/bin/bash
make clean
make

# Start the ConnectionServer in the background
java ConnectionServer > test4/ConnectionServer.log 2>&1 &

# Start each node in the background and redirect output to individual text files
java -cp .:./json.jar Node M1 True Large > test4/M1.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M2 True Large > test4/M2.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M3 True Large > test4/M3.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M4 False Large > test4/M4.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M5 False Large > test4/M5.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M6 False Large > test4/M6.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M7 False Large > test4/M7.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M8 False Large > test4/M8.txt 2>&1 &
sleep 1
java -cp .:./json.jar Node M9 False Large > test4/M9.txt 2>&1 &
sleep 1

echo "Servers and nodes have been started."

# Wait for all background processes to finish
sleep 20 # Wait for 15 seconds

kill $(pgrep -f "ConnectionServer")
echo "All processes have finished."
