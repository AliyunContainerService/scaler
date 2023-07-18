package org.aliyun.serverless.server;

import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import org.aliyun.serverless.config.Config;
import org.aliyun.serverless.manager.Manager;
import org.aliyun.serverless.model.Function;
import org.aliyun.serverless.scaler.Scaler;
import protobuf.ScalerGrpc;
import protobuf.SchedulerProto;

public class Server extends ScalerGrpc.ScalerImplBase {
    private Manager mgr;

    public Server() {
        this.mgr = new Manager(Config.DEFAULT_CONFIG);
    }

    @Override
    public void assign(SchedulerProto.AssignRequest request, StreamObserver<SchedulerProto.AssignReply> responseObserver) {
        SchedulerProto.Meta meta = request.getMetaData();
        if (!meta.isInitialized()) {
            responseObserver.onError(new RuntimeException("app meta is nil"));
            return;
        }

        Function function = new Function(meta);
        Scaler scaler = this.mgr.GetOrCreate(function);
        try {
            scaler.Assign(Context.current(), request, responseObserver);
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void idle(SchedulerProto.IdleRequest request, StreamObserver<SchedulerProto.IdleReply> responseObserver) {
        SchedulerProto.Assignment assignment = request.getAssigment();
        if (!assignment.isInitialized()) {
            responseObserver.onError(new RuntimeException("assignment is nil"));
            return;
        }

        String metaKey = request.getAssigment().getMetaKey();
        try {
            Scaler scaler = this.mgr.Get(metaKey);
            scaler.Idle(Context.current(), request, responseObserver);
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
