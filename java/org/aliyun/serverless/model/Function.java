package org.aliyun.serverless.model;

import com.google.protobuf.*;
import protobuf.SchedulerProto;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Function {
    private SchedulerProto.Meta meta;

    public Function(SchedulerProto.Meta meta) {
        this.meta = meta;
    }

    public String getKey() {
        return meta.getKey();
    }

    public String getRuntime() {
        return meta.getRuntime();
    }

    public int getTimeoutInSecs() {
        return meta.getTimeoutInSecs();
    }

    public long getMemoryInMb() {
        return meta.getMemoryInMb();
    }


    public SchedulerProto.Meta getMeta() {
        return meta;
    }

    public void setMeta(SchedulerProto.Meta meta) {
        this.meta = meta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Function function = (Function) o;
        return Objects.equals(meta, function.meta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meta);
    }
}
