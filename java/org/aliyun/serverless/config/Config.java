package org.aliyun.serverless.config;

import java.time.Duration;

public class Config {
    private String platformHost;
    private Integer platformPort;
    private Duration gcInterval;
    private Duration idleDurationBeforeGC;

    public Config(String platformHost, Integer platformPort, Duration gcInterval, Duration idleDurationBeforeGC) {
        this.platformHost = platformHost;
        this.platformPort = platformPort;
        this.gcInterval = gcInterval;
        this.idleDurationBeforeGC = idleDurationBeforeGC;
    }

    public String getPlatformHost() {
        return platformHost;
    }

    public void setPlatformHost(String platformHost) {
        this.platformHost = platformHost;
    }

    public Integer getPlatformPort() {
        return platformPort;
    }

    public void setPlatformPort(Integer platformPort) {
        this.platformPort = platformPort;
    }

    public Duration getGcInterval() {
        return gcInterval;
    }

    public void setGcInterval(Duration gcInterval) {
        this.gcInterval = gcInterval;
    }

    public Duration getIdleDurationBeforeGC() {
        return idleDurationBeforeGC;
    }

    public void setIdleDurationBeforeGC(Duration idleDurationBeforeGC) {
        this.idleDurationBeforeGC = idleDurationBeforeGC;
    }

    public static final Config DEFAULT_CONFIG = new Config(
            "127.0.0.1",
            50051,
            Duration.ofSeconds(10),
            Duration.ofMinutes(5)
    );
}
