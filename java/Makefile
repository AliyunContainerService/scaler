.PHONY: binary, proto, run

HOME_DIR := $(shell echo ~)
TARGET_PLATFORMS = linux/amd64

binary:
	mvn package

run: binary
	java -cp target/fc-scheduler-jar-with-dependencies.jar:$(HOME_DIR)/.m2/repository/io/grpc/grpc-core/1.56.0/grpc-core-1.56.0.jar org.aliyun.serverless.Application

proto:
	mvn protobuf:compile && mvn protobuf:compile-custom

docker-build:
	docker buildx build --push ${DOCKER_BUILD_ARGS} --platform ${TARGET_PLATFORMS} \
    --build-arg TARGETOS=linux --build-arg TARGETARCH=amd64 --build-arg GIT_VERSION=${GIT_VERSION} \
    -f Dockerfile . -t ${IMAGE_REPO}/scaler:${GIT_VERSION}
