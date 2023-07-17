package org.aliyun.serverless.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Instance {
    private String ID;
    private Slot slot;
    private Function meta;
    private Long CreateTimeInMs;
    private Long InitDurationInMs;
    private Boolean Busy;
    private LocalDateTime LastIdleTime;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }

    public Function getMeta() {
        return meta;
    }

    public void setMeta(Function meta) {
        this.meta = meta;
    }

    public Long getCreateTimeInMs() {
        return CreateTimeInMs;
    }

    public void setCreateTimeInMs(Long createTimeInMs) {
        CreateTimeInMs = createTimeInMs;
    }

    public Long getInitDurationInMs() {
        return InitDurationInMs;
    }

    public void setInitDurationInMs(Long initDurationInMs) {
        InitDurationInMs = initDurationInMs;
    }

    public Boolean getBusy() {
        return Busy;
    }

    public void setBusy(Boolean busy) {
        Busy = busy;
    }

    public LocalDateTime getLastIdleTime() {
        return LastIdleTime;
    }

    public void setLastIdleTime(LocalDateTime lastIdleTime) {
        LastIdleTime = lastIdleTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Instance instance = (Instance) o;
        return Objects.equals(ID, instance.ID) && Objects.equals(slot, instance.slot) && Objects.equals(meta, instance.meta) && Objects.equals(CreateTimeInMs, instance.CreateTimeInMs) && Objects.equals(InitDurationInMs, instance.InitDurationInMs) && Objects.equals(Busy, instance.Busy) && Objects.equals(LastIdleTime, instance.LastIdleTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, slot, meta, CreateTimeInMs, InitDurationInMs, Busy, LastIdleTime);
    }
}
