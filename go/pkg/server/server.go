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

package server

import (
	"context"
	"fmt"
	"github.com/AliyunContainerService/scaler/go/pkg/config"
	"github.com/AliyunContainerService/scaler/go/pkg/manager"
	"github.com/AliyunContainerService/scaler/go/pkg/model"

	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"

	pb "github.com/AliyunContainerService/scaler/proto"
)

type Server struct {
	pb.UnimplementedScalerServer
	mgr *manager.Manager
}

func New() *Server {
	return &Server{
		mgr: manager.New(&config.DefaultConfig),
	}
}

func (s *Server) Assign(ctx context.Context, request *pb.AssignRequest) (*pb.AssignReply, error) {
	if request.MetaData == nil {
		return nil, status.Errorf(codes.InvalidArgument, fmt.Sprintf("app meta is nil"))
	}
	metaData := &model.Meta{
		Meta: pb.Meta{
			Key:           request.MetaData.Key,
			Runtime:       request.MetaData.Runtime,
			TimeoutInSecs: request.MetaData.TimeoutInSecs,
		},
	}
	scheduler := s.mgr.GetOrCreate(metaData)
	return scheduler.Assign(ctx, request)
}

func (s *Server) Idle(ctx context.Context, request *pb.IdleRequest) (*pb.IdleReply, error) {
	if request.Assigment == nil {
		return nil, status.Errorf(codes.InvalidArgument, fmt.Sprintf("assignment is nil"))
	}
	key := request.Assigment.MetaKey
	scheduler, err := s.mgr.Get(key)
	if err != nil {
		errorMessage := fmt.Sprintf("scaler for app: %s not found", key)
		return &pb.IdleReply{
			Status:       pb.Status_InternalError,
			ErrorMessage: &errorMessage,
		}, nil
	}
	return scheduler.Idle(ctx, request)
}
