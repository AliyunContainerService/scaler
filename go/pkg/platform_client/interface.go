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
	model2 "github.com/AliyunContainerService/scaler/go/pkg/model"
	"io"
)

type Client interface {
	io.Closer
	CreateSlot(ctx context.Context, requestId string, slotResourceConfig *model2.SlotResourceConfig) (*model2.Slot, error)
	DestroySLot(ctx context.Context, requestId, slotId, reason string) error
	Init(ctx context.Context, requestId, instanceId string, slot *model2.Slot, meta *model2.Meta) (*model2.Instance, error)
}
