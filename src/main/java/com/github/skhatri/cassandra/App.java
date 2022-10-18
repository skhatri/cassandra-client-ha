package com.github.skhatri.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.DriverException;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

class Hosts {
    static class Host {
        public final String host;
        public final String dc;
        public final int port;

        Host(String host, String dc, int port) {
            this.host = host;
            this.dc = dc;
            this.port = port;
        }
    }

    public static final Host dc1 = new Host("dse-0", "dc1", 19042);
    public static final Host dc2 = new Host("dse-1", "dc2", 29042);


    public static final Host forDc(String dc) {
        if (dc.equals("dc1")) {
            return dc1;
        }
        return dc2;
    }
}

class TestConn {
    private final AtomicReference<CqlSession> session;
    private final List<InetSocketAddress> contactPoints;
    private final String dc;
    private final PreparedStatement preparedStatement;
    private final String localDc;
    private final Optional<String> fallbackDc;

    public TestConn(String dc) {
        this.contactPoints = new ArrayList<>();
        this.dc = dc;
        contactPoints.add(new InetSocketAddress(Hosts.dc1.host, Hosts.dc1.port));
        contactPoints.add(new InetSocketAddress(Hosts.dc2.host, Hosts.dc2.port));
        this.session = new AtomicReference<>(newSession());
        this.preparedStatement = this.session.get().prepare("select name, description, object_type, parent from objects limit 1");
        this.localDc = LoadBalancerHelper.getInstance().getLocalDc();
        this.fallbackDc = createFallback(dc, localDc);
        System.out.println("fallback dc " + fallbackDc);
    }

    private Optional<String> createFallback(String dcNames, String local) {
        return Arrays.stream(dcNames.split(",")).filter(name -> !name.equals(local)).findFirst();
    }

    private CqlSession newSession() {
        return CqlSession.builder()
            .addContactPoints(contactPoints)
            .withAuthCredentials("cassandra", "cassandra")
            .withKeyspace("metadata")
            .withLocalDatacenter(dc.split(",")[0])
            .build();
    }

    private void execute(boolean retry) {
        try {
            System.out.println();
            BoundStatement boundStatement = this.preparedStatement.bind();
            if (!retry && fallbackDc.isPresent()) {
                System.out.println("using fallback " + fallbackDc.get());
                boundStatement = boundStatement.setExecutionProfileName(fallbackDc.get());
            }
            session.get().execute(boundStatement).all()
                .stream().forEach(row -> {
                    String name = row.getString("name");
                    String description = row.getString("description");
                    String objectType = row.getString("object_type");
                    String parent = row.getString("parent");
                    System.out.printf("name=%s, object_type=%s, description=%s, parent=%s\n", name, objectType, description, parent);
                });
        } catch (DriverException nae) {
            if (retry) {
                execute(false);
            } else {
                System.err.println("error with query " + nae);
            }
        } catch (Exception ex) {
            System.err.println("exception in query " + ex);
        }
    }

    public void start() {
        while (true) {
            execute(true);
            try {
                Thread.sleep(5000L);
            } catch (Exception ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void shutdown() {
        System.out.println("shutting down");
        session.get().close();
    }
}

/*

    datastax-java-driver.advanced.reconnection-policy.class=com.github.skhatri.cassandra.MyExponentialReconnectionPolicy
    datastax-java-driver.advanced.retry-policy.class=com.github.skhatri.cassandra.MyRetryPolicy

    advanced.load-balancing-policy.dc-failover.allow-for-local-consistency-levels=true
    advanced.load-balancing-policy.dc-failover.max-nodes-per-remote-dc=1
    reconnect-on-no-node-available=true

 */
public class App {
    public static void main(String[] args) throws Exception {

        if (true) {
            String data = System.getProperty("test", "1234");
            System.out.println(data);
            return;
        }

        boolean reconnectOnNoNode = Boolean.valueOf(System.getProperty("cassandra-client.reconnect-on-no-node-available", "false"));
        String dc = System.getProperty("datastax-java-driver.basic.load-balancing-policy.local-datacenter", "dc1");
        System.out.printf("Datacenter %s\n", dc);
        System.getProperties().entrySet().stream()
            .filter(kv ->
                kv.getKey().toString().contains("datastax") || kv.getKey().toString().contains("cassandra-client")
            )
            .forEach(kv -> {
                System.out.println(kv.getKey() + "=" + kv.getValue());
            });
        TestConn test = new TestConn(dc);
        test.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> test.shutdown()));
    }
}
