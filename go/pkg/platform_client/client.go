/*
Copyright 2023 The Alibaba Cloud Serverless Authors.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package platform_client

import (
	"context"
	"fmt"
	model2 "github.com/AliyunContainerService/scaler/go/pkg/model"
	"io"
	"log"
	"time"

	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"

	pb "github.com/AliyunContainerService/scaler/proto"
)

type PlatformClient struct {
	clientConn io.Closer
	c          pb.PlatformClient
	addr       string
}

func New(addr string) (Client, error) {
	conn, err := grpc.Dial(addr, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		log.Fatalf("did not connect: %v", err)
		return nil, err
	}
	return &PlatformClient{
		clientConn: conn,
		c:          pb.NewPlatformClient(conn),
		addr:       addr,
	}, nil
}

func (client *PlatformClient) CreateSlot(ctx context.Context, requestId string, slotResourceConfig *model2.SlotResourceConfig) (*model2.Slot, error) {
	req := &pb.CreateSlotRequest{
		RequestId:      requestId,
		ResourceConfig: &slotResourceConfig.ResourceConfig,
	}
	reply, err := client.c.CreateSlot(ctx, req)
	if err != nil {
		return nil, err
	}
	if reply.Status != pb.Status_Ok {
		return nil, fmt.Errorf("create slot failed with code: %d, message: %s", reply.Status, *reply.ErrorMessage)
	}

	return &model2.Slot{
		Slot: pb.Slot{
			Id:                 reply.Slot.Id,
			ResourceConfig:     reply.Slot.ResourceConfig,
			CreateTime:         reply.Slot.CreateTime,
			CreateDurationInMs: reply.Slot.CreateDurationInMs,
		},
	}, nil
}

func (client *PlatformClient) DestroySLot(ctx context.Context, requestId, slotId, reason string) error {
	req := &pb.DestroySlotRequest{
		RequestId: requestId,
		Id:        slotId,
		Reason:    &reason,
	}
	reply, err := client.c.DestroySlot(ctx, req)
	if err != nil {
		return err
	}
	if reply.Status != pb.Status_Ok {
		return fmt.Errorf("destroy slot failed with code: %d, msg: %s", reply.Status, *reply.ErrorMessage)
	}
	return nil
}

func (client *PlatformClient) Init(ctx context.Context, requestId, instanceId string, slot *model2.Slot, meta *model2.Meta) (*model2.Instance, error) {
	req := &pb.InitRequest{
		RequestId:  requestId,
		InstanceId: instanceId,
		SlotId:     slot.Id,
		MetaData:   &meta.Meta,
	}
	reply, err := client.c.Init(ctx, req)
	if err != nil {
		return nil, err
	}
	if reply.Status != pb.Status_Ok {
		return nil, fmt.Errorf("init app failed with code: %d, message: %s", reply.Status, *reply.ErrorMessage)
	}
	return &model2.Instance{
		Id:               instanceId,
		Slot:             slot,
		Meta:             meta,
		CreateTimeInMs:   int64(reply.CreateTime),
		InitDurationInMs: int64(reply.InitDurationInMs),
		Busy:             false,
		LastIdleTime:     time.Now(),
	}, nil
}

func (client *PlatformClient) Close() error {
	return client.clientConn.Close()
}
