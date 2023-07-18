package org.aliyun.serverless.manager;

import org.aliyun.serverless.scaler.Scaler;
import org.aliyun.serverless.scaler.SimpleScaler;
import org.aliyun.serverless.config.Config;
import org.aliyun.serverless.model.Function;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

public class Manager {
    private static final Logger logger = Logger.getLogger(Manager.class.getName());
    private final ReadWriteLock rw = new ReentrantReadWriteLock();
    private final Map<String, Scaler> schedulers = new HashMap<>();
    private final Config config;

    public Manager(Config config) {
        this.config = config;
    }

    public Scaler GetOrCreate(Function function) {
        String key = function.getKey();
        rw.readLock().lock();
        try {
            Scaler scheduler = schedulers.get(key);
            if (scheduler != null) {
                return scheduler;
            }
        } finally {
            rw.readLock().unlock();
        }

        rw.writeLock().lock();
        try {
            Scaler scheduler = schedulers.get(key);
            if (scheduler != null) {
                return scheduler;
            }

            logger.info("Create new scaler for app " + function.getKey());
            scheduler = new SimpleScaler(function, config);
            schedulers.put(function.getKey(), scheduler);
            return scheduler;
        } finally {
            rw.writeLock().unlock();
        }
    }

    public Scaler Get(String functionKey) throws Exception {
        rw.readLock().lock();
        try {
            Scaler scheduler = schedulers.get(functionKey);
            if (scheduler != null) {
                return scheduler;
            }
            throw new Exception("scaler of app: " + functionKey + " not found");
        } finally {
            rw.readLock().unlock();
        }
    }
}
