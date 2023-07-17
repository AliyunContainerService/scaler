package org.aliyun.serverless.scaler;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Empty;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import org.aliyun.serverless.config.Config;
import org.aliyun.serverless.model.*;
import org.aliyun.serverless.platformClient.Client;
import org.aliyun.serverless.platformClient.PlatformClient;
import protobuf.SchedulerProto;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.time.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class SimpleScaler implements Scaler {
    private static final Logger logger = Logger.getLogger(SimpleScaler.class.getName());
    private final Config config;
    private final Function function;
    private final Client platformClient;
    private final Lock mu;
    private final CountDownLatch wg;
    private final Map<String, Instance> instances;
    private final Deque<Instance> idleInstances;

    public SimpleScaler(Function function, Config config) {
        try {
            this.config = config;
            this.function = function;
            this.platformClient = new PlatformClient(config.getPlatformHost(), config.getPlatformPort());
            this.mu = new ReentrantLock();
            this.wg = new CountDownLatch(1);
            this.instances = new ConcurrentHashMap<>();
            this.idleInstances = new LinkedList<>();
            logger.info(String.format("New scaler for app: %s is created", function.getKey()));
            new Thread(this::gcLoop).start();
        } catch (Exception e) {
            throw new RuntimeException("failed to create Simple scaler", e);
        }
    }

    public void Assign(Context ctx, SchedulerProto.AssignRequest request, StreamObserver<SchedulerProto.AssignReply> responseObserver) throws Exception {
        Instant start = Instant.now();
        String instanceId = UUID.randomUUID().toString();
        try {
            logger.info("Start assign, request id: " + request.getRequestId());
            mu.lock();
            if (!idleInstances.isEmpty()) {
                Instance instance = idleInstances.pollFirst();
                instance.setBusy(true);
                String instanceID = instance.getID();
                instances.put(instanceID, instance);
                mu.unlock();

                logger.info(String.format("Finish assign, request id: %s, instance %s reused", request.getRequestId(), instanceID));

                SchedulerProto.Assignment assignment = SchedulerProto.Assignment.newBuilder()
                        .setRequestId(request.getRequestId()).setMetaKey(instance.getMeta().getKey())
                        .setInstanceId(instanceID).build();
                responseObserver.onNext(SchedulerProto.AssignReply.newBuilder().setStatus(SchedulerProto.Status.Ok).setAssigment(assignment).build());
                responseObserver.onCompleted();
                return;
            }
            mu.unlock();

            // Create new instance
            SchedulerProto.ResourceConfig resourceConfig = SchedulerProto.ResourceConfig.newBuilder()
                    .setMemoryInMegabytes(request.getMetaData().getMemoryInMb()).build();
            SlotResourceConfig slotResourceConfig = new SlotResourceConfig(resourceConfig);

            ListenableFuture<Slot> slotFuture = platformClient.CreateSlot(ctx, request.getRequestId(), slotResourceConfig);
            Slot slot = slotFuture.get();

            SchedulerProto.Meta meta = SchedulerProto.Meta.newBuilder()
                    .setKey(request.getMetaData().getKey())
                    .setRuntime(request.getMetaData().getRuntime())
                    .setTimeoutInSecs(request.getMetaData().getTimeoutInSecs())
                    .build();
            Function function = new Function(meta);

            ListenableFuture<Instance> instanceFuture = platformClient.Init(ctx, request.getRequestId(), instanceId, slot, function);
            Instance instance = instanceFuture.get();
            String instanceID = instance.getID();

            mu.lock();
            instance.setBusy(true);
            instances.put(instanceID, instance);
            mu.unlock();

            logger.info(String.format("request id: %s, instance %s for app %s is created, init latency: %dms",
                    request.getRequestId(), instanceID, instance.getMeta().getKey(), instance.getInitDurationInMs()));
            SchedulerProto.Assignment assignment = SchedulerProto.Assignment.newBuilder()
                    .setRequestId(request.getRequestId()).setMetaKey(instance.getMeta().getKey())
                    .setInstanceId(instanceID).build();
            responseObserver.onNext(SchedulerProto.AssignReply.newBuilder().setStatus(SchedulerProto.Status.Ok).setAssigment(assignment).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            String errorMessage = String.format("Failed to assign instance, request id: %s due to %s", request.getRequestId(), e.getMessage());
            logger.info(errorMessage);
            responseObserver.onError(new RuntimeException(errorMessage, e));
        } finally {
            logger.info(String.format("Finish assign, request id: %s, instance id: %s, cost %dms",
                    request.getRequestId(), instanceId, Duration.between(start, Instant.now()).toMillis()));
        }
    }

    public void Idle(Context ctx, SchedulerProto.IdleRequest request, StreamObserver<SchedulerProto.IdleReply> responseObserver) throws Exception {
        if (!request.getAssigment().isInitialized()) {
            responseObserver.onError(new RuntimeException("assignment is null"));
            return;
        }

        SchedulerProto.IdleReply.Builder replyBuilder = SchedulerProto.IdleReply.newBuilder()
                .setStatus(SchedulerProto.Status.Ok);
        long start = System.currentTimeMillis();
        String instanceId = request.getAssigment().getInstanceId();
        try {
            logger.info(String.format("Start idle, request id: %s", request.getAssigment().getRequestId()));
            boolean needDestroy = false;
            String slotId = "";
            if (request.getResult().isInitialized() && request.getResult().getNeedDestroy()) {
                needDestroy = true;
            }

            mu.lock();
            Instance instance = instances.get(instanceId);
            if (instance == null) {
                mu.unlock();
                responseObserver.onError(new RuntimeException(
                        String.format("request id %s, instance %s not found",
                                request.getAssigment().getRequestId(), instanceId)));
                return;
            }

            instance.setLastIdleTime(LocalDateTime.now());
            if (!instance.getBusy()) {
                mu.unlock();
                logger.warning(String.format("request id %s, instance %s already freed",
                        request.getAssigment().getRequestId(), instanceId));
            } else {
                instance.setBusy(false);
                idleInstances.offerFirst(instance);
                mu.unlock();
            }

            responseObserver.onNext(replyBuilder.build());
            responseObserver.onCompleted();
            if (needDestroy) {
                deleteSlot(ctx, instance, request.getAssigment().getRequestId(), "bad instance");
            }
        } catch (Exception e) {
            String errorMessage = String.format("idle failed with: %s", e.getMessage());
            logger.info(errorMessage);
            responseObserver.onNext(replyBuilder
                    .setStatus(SchedulerProto.Status.InternalError)
                    .setErrorMessage(errorMessage)
                    .build());
            responseObserver.onCompleted();
        } finally {
            long cost = System.currentTimeMillis() - start;
            logger.info(String.format("Idle, request id: %s, instance: %s, cost %dus%n",
                    request.getAssigment().getRequestId(), instanceId, cost));
        }
    }

    private void deleteSlot(Context context, Instance instance, String requestId, String reason) {
        String instanceID = instance.getID();
        String slotID = instance.getSlot().getId();
        String metaKey = instance.getMeta().getKey();
        logger.info(String.format("start delete Instance %s (Slot: %s) of app: %s", instanceID, slotID, metaKey));

        mu.lock();
        if (!instances.containsKey(instanceID)) {
            mu.unlock();
            return;
        }
        instances.remove(instanceID);
        idleInstances.remove(instance);
        mu.unlock();

        try {
            ListenableFuture<Empty> future = platformClient.DestroySLot(context, requestId, slotID, reason);
            future.get();
        } catch (Exception e) {
            logger.info(String.format("delete Instance %s (Slot: %s) of app: %s failed with: %s",
                    instanceID, slotID, metaKey, e.getMessage()));
        }
    }

    private void gcLoop() {
        logger.info(String.format("gc loop for app: %s is starting", function.getKey()));
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                mu.lock();

                Iterator<Instance> iterator = idleInstances.iterator();
                while (iterator.hasNext()) {
                    Instance instance = iterator.next();
                    if (instance != null) {
                        long idleDuration = Duration.between(instance.getLastIdleTime(), LocalDateTime.now()).toMillis();
                        if (idleDuration > config.getIdleDurationBeforeGC().toMillis()) {
                            String reason = String.format("Idle duration: %dms, exceed configured duration: %dms",
                                    idleDuration, config.getIdleDurationBeforeGC().toMillis());
                            CompletableFuture.runAsync(() -> deleteSlot(Context.current(), instance, UUID.randomUUID().toString(), reason));
                            logger.info(String.format("Instance %s of app %s is GCed due to idle for %dms",
                                    instance.getID(), instance.getMeta().getKey(), idleDuration));
                        }
                    }
                }
                mu.unlock();
            }
        };

        timer.schedule(task, 0, this.config.getGcInterval().toMillis());
    }

    public Stats Stats() {
        mu.lock();
        Stats stats = new Stats();
        stats.setTotalInstance(instances.size());
        stats.setTotalIdleInstance(idleInstances.size());
        mu.unlock();

        return stats;
    }
}