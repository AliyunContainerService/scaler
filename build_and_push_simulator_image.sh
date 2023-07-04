#!/bin/bash

SIMULATOR_VERSION=${1:-"v1.3.1"}
ALIYUN_IMAGE_URL="registry.cn-beijing.aliyuncs.com/cloudnative-challenge/simulator:${SIMULATOR_VERSION}"
VOLC_IMAGE_URL="autra-cn-beijing.cr.volces.com/autra/volcengine/wei_simulator_test:v1.0"

docker pull $ALIYUN_IMAGE_URL
docker tag $ALIYUN_IMAGE_URL $VOLC_IMAGE_URL
docker push $VOLC_IMAGE_URL
