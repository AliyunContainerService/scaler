#!/bin/bash

kubectl --context dev -n ml-infra apply -f hack/serverless-simulaion.yaml
