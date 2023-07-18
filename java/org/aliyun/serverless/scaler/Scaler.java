package org.aliyun.serverless.scaler;

import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import org.aliyun.serverless.model.Stats;
import protobuf.SchedulerProto;

public interface Scaler {
    void Assign(Context ctx, SchedulerProto.AssignRequest request, StreamObserver<SchedulerProto.AssignReply> responseObserver) throws Exception;
    void Idle(Context ctx, SchedulerProto.IdleRequest request, StreamObserver<SchedulerProto.IdleReply> responseObserver) throws Exception;
    Stats Stats();
}
