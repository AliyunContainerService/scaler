package org.aliyun.serverless.model;

import java.util.Objects;

public class Stats {
    private Integer TotalInstance;
    private Integer TotalIdleInstance;

    public Stats() {
    }

    public Integer getTotalInstance() {
        return TotalInstance;
    }

    public void setTotalInstance(Integer totalInstance) {
        TotalInstance = totalInstance;
    }

    public Integer getTotalIdleInstance() {
        return TotalIdleInstance;
    }

    public void setTotalIdleInstance(Integer totalIdleInstance) {
        TotalIdleInstance = totalIdleInstance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stats stats = (Stats) o;
        return Objects.equals(TotalInstance, stats.TotalInstance) && Objects.equals(TotalIdleInstance, stats.TotalIdleInstance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(TotalInstance, TotalIdleInstance);
    }
}
