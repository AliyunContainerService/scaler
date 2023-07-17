#!/bin/sh

java -cp scheduler.jar:/app/grpc-core-1.56.0.jar org.aliyun.serverless.Application &
echo "Scaler is starting..."

SCALER_PID=$!

while true; do
  nc -z localhost 9001 > /dev/null 2>&1
  if [ $? -eq 0 ]; then
    echo "Scaler is now available."
    break
  else
    sleep 1
  fi
done

while true; do
  nc -z localhost 9000 > /dev/null 2>&1
  if [ $? -eq 0 ]; then
    echo "Simulator is now available."
    break
  else
    sleep 1
  fi
done

while true; do
    nc -z localhost 9000 > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        sleep 1
    else
        echo "Simulator(:9000) has stopped, stopping the Scaler process..."
        kill $SCALER_PID
        exit 0
    fi
done