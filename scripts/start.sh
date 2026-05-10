#!/bin/bash
source ~/hireflow/.env

nohup java -jar \
  ~/hireflow/build/libs/hireflow-0.0.1-SNAPSHOT.jar \
  > ~/hireflow/app.log 2>&1 &

echo $! > ~/hireflow/app.pid
echo "Started with PID $(cat ~/hireflow/app.pid)"