#!/bin/bash

# TODO automate this script
docker pull registry.cn-beijing.aliyuncs.com/cloudnative-challenge/simulator:v1.3.1
docker tag ${IMAGE_DIGEST} autra-cn-beijing.cr.volces.com/autra/volcengine/wei_simulator_test:v1.0
docker push autra-cn-beijing.cr.volces.com/autra/volcengine/wei_simulator_test:v1.0
