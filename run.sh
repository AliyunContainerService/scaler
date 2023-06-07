#!/bin/bash

set -x

./scaler &

SCALER_PID=$!

while true; do
  nc -z localhost 9001
  if [ $? -eq 0 ]; then
    break
  else
    sleep 1
  fi
done

while true; do
  nc -z localhost 9000
  if [ $? -eq 0 ]; then
    break
  else
    sleep 1
  fi
done

while true; do
    nc -z localhost 9000
    if [ $? -eq 0 ]; then
        sleep 1
    else
        echo "No process is listening on port 9000, stopping the scaler process..."
        kill $SCALER_PID
        exit 0
    fi
done
