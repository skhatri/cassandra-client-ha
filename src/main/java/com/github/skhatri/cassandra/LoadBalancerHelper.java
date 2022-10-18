package com.github.skhatri.cassandra;

import java.util.concurrent.atomic.AtomicReference;

public class LoadBalancerHelper {

    private final AtomicReference<String> localDc = new AtomicReference<>("");

    public LoadBalancerHelper() {

    }

    public void setLocalDc(String dc) {
        this.localDc.set(dc);
    }

    public String getLocalDc() {
        return localDc.get();
    }

    public static LoadBalancerHelper getInstance() {
        return INSTANCE;
    }

    private static final LoadBalancerHelper INSTANCE = new LoadBalancerHelper();
}
