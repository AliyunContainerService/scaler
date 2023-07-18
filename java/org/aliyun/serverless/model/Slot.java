package org.aliyun.serverless.model;

import com.google.protobuf.ByteString;
import protobuf.SchedulerProto;

import java.util.Objects;

public class Slot {
    public SchedulerProto.Slot slot;

    public Slot(SchedulerProto.Slot slot) {
        this.slot = slot;
    }

    public String getId() {
        return slot.getId();
    }

    public boolean hasResourceConfig() {
        return slot.hasResourceConfig();
    }

    public SchedulerProto.ResourceConfig getResourceConfig() {
        return slot.getResourceConfig();
    }

    public long getCreateTime() {
        return slot.getCreateTime();
    }

    public long getCreateDurationInMs() {
        return slot.getCreateDurationInMs();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Slot slot1 = (Slot) o;
        return Objects.equals(slot, slot1.slot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slot);
    }
}

