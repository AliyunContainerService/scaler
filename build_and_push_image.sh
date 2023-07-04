#!/bin/bash

# build proto first
cd proto && protoc -I ./  serverless-sim.proto --go_out=. --go_opt=paths=source_relative --go-grpc_out=. --go-grpc_opt=paths=source_relative && cd -

# build & push aliyun image
docker buildx build --platform linux/amd64 -t registry.cn-shanghai.aliyuncs.com/cloud-native-challenge-wei/scaler:v1.0 . --push

# build & push volcengine image
docker buildx build --platform linux/amd64 -t autra-cn-beijing.cr.volces.com/autra/volcengine/wei_scaler_test:v1.0 . --push
