package com.github.skhatri.cassandra;

import com.datastax.oss.driver.api.core.connection.ReconnectionPolicy;
import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.api.core.metadata.Node;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class MyReconnectionPolicy implements ReconnectionPolicy {

    private ReconnectionPolicy.ReconnectionSchedule schedule;

    public MyReconnectionPolicy(DriverContext context) {
        this.schedule = () -> {
            Duration delay = Duration.of(10, ChronoUnit.SECONDS);
            System.out.println("next delay " + delay.toString());
            return delay;
        };
    }

    @Override
    public ReconnectionSchedule newNodeSchedule(Node node) {
        System.out.printf("new node dc=%s address=%s\n", node.getDatacenter(),
            node.getBroadcastAddress(),
            node.getDistance().name());
        System.out.flush();

        return schedule;
    }

    @Override
    public ReconnectionSchedule newControlConnectionSchedule(boolean isInitialConnection) {
        System.out.println("is initial connection " + isInitialConnection);
        return schedule;
    }

    @Override
    public void close() {
        System.out.println("called closed connection");
    }
}
