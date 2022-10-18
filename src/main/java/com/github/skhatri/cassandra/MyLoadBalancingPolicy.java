package com.github.skhatri.cassandra;

import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.api.core.metadata.Node;
import com.datastax.oss.driver.api.core.metadata.NodeState;
import com.datastax.oss.driver.api.core.tracker.RequestTracker;
import com.datastax.oss.driver.internal.core.loadbalancing.BasicLoadBalancingPolicy;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class MyLoadBalancingPolicy extends BasicLoadBalancingPolicy implements RequestTracker {


    private final List<String> dcs;

    public MyLoadBalancingPolicy(DriverContext context, String profileName) {
        super(context, profileName);
        this.dcs = Arrays.stream(context.getConfig().getDefaultProfile().getString(DefaultDriverOption.LOAD_BALANCING_LOCAL_DATACENTER).split(","))
            .collect(Collectors.toList());
    }

    @NonNull
    @Override
    protected Optional<String> discoverLocalDc(@NonNull Map<UUID, Node> nodes) {
        List<String> availableDcs = nodes.values().stream()
            .map(node -> {
                System.out.println(node.getBroadcastAddress() + " " + node.getDatacenter() + " " + node.getState());
                return node;
            })
            .filter(node -> node.getState() == NodeState.UP || node.getState() == NodeState.UNKNOWN)
            .map(node -> node.getDatacenter()).distinct().collect(Collectors.toList());
        Optional<String> localDc = Optional.empty();
        System.out.println("available dcs [" + availableDcs.stream().collect(Collectors.joining(",")) + "]");
        for (String dc : dcs) {
            if (availableDcs.contains(dc)) {
                localDc = Optional.of(dc);
                break;
            }
        }
        if (localDc.isEmpty()) {
            throw new RuntimeException("could not discover local dc");
        }
        LoadBalancerHelper.getInstance().setLocalDc(localDc.get());
        System.out.println("discovered local dc = " + localDc);
        return localDc;
    }
}
