package org.aliyun.serverless.platformClient;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Empty;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import org.aliyun.serverless.model.Function;
import org.aliyun.serverless.model.Instance;
import org.aliyun.serverless.model.Slot;
import org.aliyun.serverless.model.SlotResourceConfig;
import protobuf.SchedulerProto;

public interface Client {
    ListenableFuture<Slot> CreateSlot(Context ctx, String requestID, SlotResourceConfig slotResourceConfig) throws Exception;
    ListenableFuture<Empty> DestroySLot(Context ctx, String requestID, String slotId, String reason) throws Exception;
    ListenableFuture<Instance> Init(Context ctx, String requestId, String instanceId, Slot slot, Function function) throws Exception;
    void Close() throws Exception;
}
