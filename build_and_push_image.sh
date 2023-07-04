#!/bin/bash

# build & push aliyun image
docker buildx build --platform linux/amd64 -t registry.cn-shanghai.aliyuncs.com/cloud-native-challenge-wei/scaler:v1.0 . --push

# build & push volcengine image
docker buildx build --platform linux/amd64 -t autra-cn-beijing.cr.volces.com/autra/volcengine/wei_scaler_test:v1.0 . --push
