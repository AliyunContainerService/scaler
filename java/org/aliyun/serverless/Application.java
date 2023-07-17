package org.aliyun.serverless;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class Application {
    public static void main(String[] args) throws IOException, InterruptedException {

        Server server = ServerBuilder.forPort(9001)
                .addService(new org.aliyun.serverless.server.Server())
                .build()
                .start();
        System.out.println("Server started, listening on 9001");
        server.awaitTermination();
    }
}
