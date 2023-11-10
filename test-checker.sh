#!/bin/bash

# Check if a directory was provided
if [ -z "$1" ]; then
  echo "Please provide a directory name."
  exit 1
fi

# Check if the provided argument is a directory
if [ ! -d "$1" ]; then
  echo "The provided argument is not a directory."
  exit 1
fi

# Change directory to the provided directory
cd "$1"

# Function to check for consensus in the file
check_consensus() {
  local file=$1
  
  if [ ! -f "$file" ]; then
    echo "File $file does not exist."
    return 1
  fi

  # Using awk to extract the value after DECIDE:M
  local consensus=$(awk '/DECIDE:M/{print substr($0, index($0, $3))}' "$file")

  if [ -n "$consensus" ]; then
    echo "Consensus reached in $file with $consensus."
    return 0
  else
    echo "Consensus NOT reached in $file."
    return 1
  fi
}

# Loop through M1.txt to M9.txt and check for consensus
all_consensus=true
for i in {1..9}; do
  if ! check_consensus "M$i.txt"; then
    all_consensus=false
  fi
done

# Final output
if [ "$all_consensus" = true ]; then
  echo "Consensus reached in all files."
else
  echo "Consensus not reached in all files."
fi

exit 0
