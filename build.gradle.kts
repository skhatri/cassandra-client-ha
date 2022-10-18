plugins {
    id("java")
    id("idea")
}

repositories {
    mavenCentral()
}

val uber by configurations.creating

dependencies {
    uber("com.datastax.oss:java-driver-core:4.14.0")
    implementation(uber)
}

//exponential reconnection when local DC is down
val exponentialReconnectionWhenLocalDcDown = listOf(
    "-Ddatastax-java-driver.advanced.reconnection-policy.class=com.github.skhatri.cassandra.MyExponentialReconnectionPolicy",
    "-Ddatastax-java-driver.advanced.retry-policy.class=com.github.skhatri.cassandra.MyRetryPolicy",
    "-Ddatastax-java-driver.advanced.load-balancing-policy.dc-failover.allow-for-local-consistency-levels=false",
    "-Ddatastax-java-driver.advanced.load-balancing-policy.dc-failover.max-nodes-per-remote-dc=0",
    "-Ddatastax-java-driver.basic.load-balancing-policy.class=com.github.skhatri.cassandra.MyLoadBalancingPolicy",
    "-Dcassandra-client.reconnect-on-no-node-available=false",
    "-Ddatastax-java-driver.profiles.dc1.basic.load-balancing-policy.local-datacenter=dc1",
    "-Ddatastax-java-driver.profiles.dc2.basic.load-balancing-policy.local-datacenter=dc2",
    "-Ddatastax-java-driver.basic.load-balancing-policy.local-datacenter=dc1,dc2"
)

//constant reconnection when local DC is down
val constantReconnectionWhenLocalDcDown = listOf(
    "-Ddatastax-java-driver.advanced.reconnection-policy.class=com.github.skhatri.cassandra.MyReconnectionPolicy",
    "-Ddatastax-java-driver.advanced.retry-policy.class=com.github.skhatri.cassandra.MyRetryPolicy",
    "-Ddatastax-java-driver.advanced.load-balancing-policy.dc-failover.allow-for-local-consistency-levels=false",
    "-Ddatastax-java-driver.advanced.load-balancing-policy.dc-failover.max-nodes-per-remote-dc=0",
    "-Ddatastax-java-driver.basic.load-balancing-policy.class=com.github.skhatri.cassandra.MyLoadBalancingPolicy",
    "-Dcassandra-client.reconnect-on-no-node-available=true",
    "-Ddatastax-java-driver.profiles.dc1.basic.load-balancing-policy.local-datacenter=dc1",
    "-Ddatastax-java-driver.profiles.dc2.basic.load-balancing-policy.local-datacenter=dc2",
    "-Ddatastax-java-driver.basic.load-balancing-policy.local-datacenter=dc1,dc2"
)

//exponential reconnection with reconnect on no node
val exponentialReconnectionWithReconnectOnNoNode = listOf(
    "-Ddatastax-java-driver.advanced.reconnection-policy.class=com.github.skhatri.cassandra.MyExponentialReconnectionPolicy",
    "-Ddatastax-java-driver.advanced.retry-policy.class=com.github.skhatri.cassandra.MyRetryPolicy",
    "-Ddatastax-java-driver.advanced.load-balancing-policy.dc-failover.allow-for-local-consistency-levels=false",
    "-Ddatastax-java-driver.advanced.load-balancing-policy.dc-failover.max-nodes-per-remote-dc=0",
    "-Ddatastax-java-driver.basic.load-balancing-policy.class=com.github.skhatri.cassandra.MyLoadBalancingPolicy",
    "-Dcassandra-client.reconnect-on-no-node-available=true",
    "-Ddatastax-java-driver.profiles.dc1.basic.load-balancing-policy.local-datacenter=dc1",
    "-Ddatastax-java-driver.profiles.dc2.basic.load-balancing-policy.local-datacenter=dc2",
    "-Ddatastax-java-driver.basic.load-balancing-policy.local-datacenter=dc1,dc2"
)

//constant reconnection with reconnect on no node
val constantReconectionWithReconnectOnNoNode = listOf(
    "-Ddatastax-java-driver.advanced.reconnection-policy.class=com.github.skhatri.cassandra.MyReconnectionPolicy",
    "-Ddatastax-java-driver.advanced.retry-policy.class=com.github.skhatri.cassandra.MyRetryPolicy",
    "-Ddatastax-java-driver.advanced.load-balancing-policy.dc-failover.allow-for-local-consistency-levels=false",
    "-Ddatastax-java-driver.advanced.load-balancing-policy.dc-failover.max-nodes-per-remote-dc=0",
    "-Ddatastax-java-driver.basic.load-balancing-policy.class=com.github.skhatri.cassandra.MyLoadBalancingPolicy",
    "-Dcassandra-client.reconnect-on-no-node-available=true",
    "-Ddatastax-java-driver.profiles.dc1.basic.load-balancing-policy.local-datacenter=dc1",
    "-Ddatastax-java-driver.profiles.dc2.basic.load-balancing-policy.local-datacenter=dc2",
    "-Ddatastax-java-driver.basic.load-balancing-policy.local-datacenter=dc1,dc2"
)

//remote connection with exponential reconnect
val remoteConnectionWithExponentialReconnect = listOf(
    "-Ddatastax-java-driver.advanced.reconnection-policy.class=com.github.skhatri.cassandra.MyExponentialReconnectionPolicy",
    "-Ddatastax-java-driver.advanced.retry-policy.class=com.github.skhatri.cassandra.MyRetryPolicy",
    "-Ddatastax-java-driver.advanced.load-balancing-policy.dc-failover.allow-for-local-consistency-levels=true",
    "-Ddatastax-java-driver.advanced.load-balancing-policy.dc-failover.max-nodes-per-remote-dc=1",
    "-Ddatastax-java-driver.advanced.connection.pool.remote.size=1",
    "-Ddatastax-java-driver.basic.load-balancing-policy.class=com.github.skhatri.cassandra.MyLoadBalancingPolicy",
    "-Dcassandra-client.reconnect-on-no-node-available=false",
    "-Ddatastax-java-driver.profiles.dc1.basic.load-balancing-policy.local-datacenter=dc1",
    "-Ddatastax-java-driver.profiles.dc2.basic.load-balancing-policy.local-datacenter=dc2",
    "-Ddatastax-java-driver.basic.load-balancing-policy.local-datacenter=dc1,dc2"
)


//remote connection with constant reconnect
val remoteConnectionWithConstantReconnect = listOf(
    "-Ddatastax-java-driver.advanced.reconnection-policy.class=com.github.skhatri.cassandra.MyReconnectionPolicy",
    "-Ddatastax-java-driver.advanced.retry-policy.class=com.github.skhatri.cassandra.MyRetryPolicy",
    "-Ddatastax-java-driver.advanced.load-balancing-policy.dc-failover.allow-for-local-consistency-levels=true",
    "-Ddatastax-java-driver.advanced.load-balancing-policy.dc-failover.max-nodes-per-remote-dc=1",
    "-Ddatastax-java-driver.advanced.connection.pool.remote.size=1",
    "-Ddatastax-java-driver.basic.load-balancing-policy.class=com.github.skhatri.cassandra.MyLoadBalancingPolicy",
    "-Ddatastax-java-driver.profiles.dc1.basic.load-balancing-policy.local-datacenter=dc1",
    "-Ddatastax-java-driver.profiles.dc2.basic.load-balancing-policy.local-datacenter=dc2",
    "-Dcassandra-client.reconnect-on-no-node-available=false",
    "-Ddatastax-java-driver.basic.load-balancing-policy.local-datacenter=dc1,dc2"
)

//remote connection with constant reconnect and reconnect on no node
val remoteConnectionWithConstantReconnectAndReconnectOnNoNode = listOf(
    "-Ddatastax-java-driver.advanced.reconnection-policy.class=com.github.skhatri.cassandra.MyReconnectionPolicy",
    "-Ddatastax-java-driver.advanced.retry-policy.class=com.github.skhatri.cassandra.MyRetryPolicy",
    "-Ddatastax-java-driver.advanced.load-balancing-policy.dc-failover.allow-for-local-consistency-levels=true",
    "-Ddatastax-java-driver.advanced.load-balancing-policy.dc-failover.max-nodes-per-remote-dc=1",
    "-Ddatastax-java-driver.advanced.connection.pool.remote.size=1",
    "-Ddatastax-java-driver.basic.load-balancing-policy.class=com.github.skhatri.cassandra.MyLoadBalancingPolicy",
    "-Dcassandra-client.reconnect-on-no-node-available=true",
    "-Ddatastax-java-driver.profiles.dc1.basic.load-balancing-policy.local-datacenter=dc1",
    "-Ddatastax-java-driver.profiles.dc2.basic.load-balancing-policy.local-datacenter=dc2",
    "-Ddatastax-java-driver.basic.load-balancing-policy.local-datacenter=dc1,dc2",

)

val arg = "${project.ext["opt"]}"

task("runApp", JavaExec::class) {
    main = "com.github.skhatri.cassandra.App"
    classpath = sourceSets["main"].runtimeClasspath
    jvmArgs = when (arg) {
        "option1" -> exponentialReconnectionWhenLocalDcDown
        "option2" -> constantReconnectionWhenLocalDcDown
        "option3" -> exponentialReconnectionWithReconnectOnNoNode
        "option4" -> constantReconectionWithReconnectOnNoNode
        "option5" -> remoteConnectionWithExponentialReconnect
        "option6" -> remoteConnectionWithConstantReconnect
        else -> remoteConnectionWithConstantReconnectAndReconnectOnNoNode
    }
}

val runtime by configurations.compileClasspath

tasks.register<Copy>("copyDeps") {
    from(runtime)
    include("**/*.jar")
    into("$buildDir/ext")
}

tasks.build {
    dependsOn("copyDeps")
}

