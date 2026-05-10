#!/bin/bash

nohup java -jar \
  ~/hireflow/build/libs/hireflow-0.0.1-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME} \
  --spring.datasource.username=${DB_USERNAME} \
  --spring.datasource.password=${DB_PASSWORD} \
  --jwt.secret=${JWT_SECRET} \
  --openai.api.key=${OPENAI_API_KEY} \
  --cloud.aws.credentials.access-key=${AWS_ACCESS_KEY} \
  --cloud.aws.credentials.secret-key=${AWS_SECRET_KEY} \
  --spring.mail.username=${MAIL_USERNAME} \
  --spring.mail.password=${MAIL_PASSWORD} \
  --spring.data.redis.host=${REDIS_HOST} \
  --cloud.aws.s3.bucket=${S3_BUCKET} \
  > ~/hireflow/app.log 2>&1 &

echo $! > ~/hireflow/app.pid
echo "Started with PID $(cat ~/hireflow/app.pid)"