package org.aliyun.serverless.platformClient;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.Empty;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import org.aliyun.serverless.model.Function;
import org.aliyun.serverless.model.Instance;
import org.aliyun.serverless.model.Slot;
import org.aliyun.serverless.model.SlotResourceConfig;
import org.aliyun.serverless.scaler.SimpleScaler;
import protobuf.PlatformGrpc;
import protobuf.SchedulerProto;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class PlatformClient implements Client {
    private static final Logger logger = Logger.getLogger(PlatformClient.class.getName());

    private final ManagedChannel channel;
    private final PlatformGrpc.PlatformBlockingStub client;
    private final PlatformGrpc.PlatformStub asyncClient;
    private String host;
    private Integer port;

    public PlatformClient(String host, Integer port) throws Exception {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build(), host, port);
    }

    public PlatformClient(ManagedChannel channel, String host, Integer port) {
        this.channel = channel;
        this.client = PlatformGrpc.newBlockingStub(channel);
        this.host = host;
        this.port = port;
        this.asyncClient = PlatformGrpc.newStub(channel);
    }

    @Override
    public ListenableFuture<Slot> CreateSlot(Context ctx, String requestID, SlotResourceConfig slotResourceConfig) throws Exception {
        logger.info(String.format("CreateSlot for req %s", requestID));

        SettableFuture<Slot> future = SettableFuture.create();
        SchedulerProto.CreateSlotRequest request = SchedulerProto.CreateSlotRequest.newBuilder()
                .setRequestId(requestID)
                .setResourceConfig(slotResourceConfig.getResourceConfig())
                .build();

        this.asyncClient.createSlot(request, new StreamObserver<SchedulerProto.CreateSlotReply>() {
            @Override
            public void onNext(SchedulerProto.CreateSlotReply reply) {
                if (reply.getStatus() == SchedulerProto.Status.Ok) {
                    future.set(new Slot(reply.getSlot()));
                } else {
                    String errorMessage = reply.getErrorMessage();
                    future.setException(new RuntimeException("Create slot failed with code: " + reply.getStatus() + ", message: " + errorMessage));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                future.setException(throwable);
            }

            @Override
            public void onCompleted() {
                // do nothing
            }
        });

        return future;
    }

    @Override
    public ListenableFuture<Empty> DestroySLot(Context ctx, String requestID, String slotId, String reason) throws Exception  {
        logger.info(String.format("DestroySLot for req %s", requestID));

        SettableFuture<Empty> future = SettableFuture.create();
        SchedulerProto.DestroySlotRequest request = SchedulerProto.DestroySlotRequest.newBuilder()
                .setRequestId(requestID)
                .setId(slotId)
                .setReason(reason)
                .build();

        this.asyncClient.destroySlot(request, new StreamObserver<SchedulerProto.DestroySlotReply>() {
            @Override
            public void onNext(SchedulerProto.DestroySlotReply destroySlotReply) {
                if (destroySlotReply.getStatus() == SchedulerProto.Status.Ok) {
                    future.set(Empty.getDefaultInstance());
                } else {
                    future.setException(new StatusRuntimeException(Status.INTERNAL));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                future.setException(throwable);
            }

            @Override
            public void onCompleted() {

            }
        });

        return future;
    }

    @Override
    public ListenableFuture<Instance> Init(Context ctx, String requestId, String instanceId, Slot slot, Function function) throws Exception {
        logger.info(String.format("InitSlot for req %s", requestId));

        SettableFuture<Instance> future = SettableFuture.create();
        SchedulerProto.InitRequest request = SchedulerProto.InitRequest.newBuilder()
                .setRequestId(requestId)
                .setInstanceId(instanceId)
                .setSlotId(slot.getId())
                .setMetaData(function.getMeta())
                .build();

        this.asyncClient.init(request, new StreamObserver<SchedulerProto.InitReply>() {
            @Override
            public void onNext(SchedulerProto.InitReply reply) {
                if (reply.getStatus() == SchedulerProto.Status.Ok) {
                    Instance instance = new Instance();
                    instance.setID(instanceId);
                    instance.setSlot(slot);
                    instance.setMeta(function);
                    instance.setCreateTimeInMs(reply.getCreateTime());
                    instance.setInitDurationInMs(reply.getInitDurationInMs());
                    instance.setBusy(false);
                    instance.setLastIdleTime(LocalDateTime.now());

                    future.set(instance);
                } else {
                    String errorMessage = reply.getErrorMessage();
                    future.setException(new RuntimeException("Init app failed with code: " + reply.getStatus() + ", message: " + errorMessage));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                future.setException(throwable);
            }

            @Override
            public void onCompleted() {
                // do nothing
            }
        });
        return future;
    }

    @Override
    public void Close() throws InterruptedException {
        this.channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
}