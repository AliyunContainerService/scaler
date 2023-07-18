package org.aliyun.serverless.model;

import protobuf.SchedulerProto;

import java.util.Objects;

public class SlotResourceConfig {
    private SchedulerProto.ResourceConfig resourceConfig;

    public SlotResourceConfig(SchedulerProto.ResourceConfig resourceConfig) {
        this.resourceConfig = resourceConfig;
    }

    public long getMemoryInMegabytes() {
        return resourceConfig.getMemoryInMegabytes();
    }

    public SchedulerProto.ResourceConfig getResourceConfig() {
        return resourceConfig;
    }

    public void setResourceConfig(SchedulerProto.ResourceConfig resourceConfig) {
        this.resourceConfig = resourceConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlotResourceConfig that = (SlotResourceConfig) o;
        return Objects.equals(resourceConfig, that.resourceConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceConfig);
    }
}
