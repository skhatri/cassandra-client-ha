: "${OPTION:=${1:-option1}}"

CLASSPATH=":"
for x in $(ls build/ext); do
  CLASSPATH="${CLASSPATH}:build/ext/$x"
done
CLASSPATH="build/libs/cassandra-client.jar:${CLASSPATH}"

RECONNECTION_CLASS="com.github.skhatri.cassandra.MyExponentialReconnectionPolicy"
RETRY_CLASS="com.github.skhatri.cassandra.MyRetryPolicy"
LOCAL_CONSISTENCY="false"
REMOTE_DC="0"
REMOTE_CONN="0"
RECONNECT_ON_NO_NODE="false"
LOCAL_DATACENTER="dc1,dc2"
LOADBALANCER_CLASS="com.github.skhatri.cassandra.MyLoadBalancingPolicy"

case $OPTION in
#exponential reconnection when local DC is down
option1) ;;

#constant reconnection when local DC is down
option2)
  RECONNECTION_CLASS="com.github.skhatri.cassandra.MyReconnectionPolicy"
  ;;
#exponential reconnection with reconnect on no node
option3)
  RECONNECT_ON_NO_NODE=true
  ;;
#constant reconnection with reconnect on no node
option4)
  RECONNECTION_CLASS="com.github.skhatri.cassandra.MyReconnectionPolicy"
  RECONNECT_ON_NO_NODE=true
  ;;
#remote connection with exponential reconnect
option5)
  LOCAL_CONSISTENCY=true
  REMOTE_DC="1"
  REMOTE_CONN="1"
  ;;
#remote connection with constant reconnect
option6)
  RECONNECTION_CLASS="com.github.skhatri.cassandra.MyReconnectionPolicy"
  LOCAL_CONSISTENCY=true
  REMOTE_DC="1"
  REMOTE_CONN="1"
  ;;
#remote connection with constant reconnect and reconnect on no node
option7)
  RECONNECTION_CLASS="com.github.skhatri.cassandra.MyReconnectionPolicy"
  RECONNECT_ON_NO_NODE=true
  LOCAL_CONSISTENCY=true
  REMOTE_DC="1"
  REMOTE_CONN="1"
  ;;
*)
  echo choose option1-option7
  exit 1
  ;;
esac

echo "-------
OPTIONS
-------
RECONNECTION_CLASS=${RECONNECTION_CLASS}
RETRY_CLASS=${RETRY_CLASS}
LOCAL_CONSISTENCY=${LOCAL_CONSISTENCY}
REMOTE_DC=${REMOTE_DC}
REMOTE_CONN=${REMOTE_CONN}
RECONNECT_ON_NO_NODE=${RECONNECT_ON_NO_NODE}
LOCAL_DATACENTER=${LOCAL_DATACENTER}
-------
"

JAVA_ARGS="${JAVA_ARGS} -Ddatastax-java-driver.advanced.reconnection-policy.class=${RECONNECTION_CLASS}"
JAVA_ARGS="${JAVA_ARGS} -Ddatastax-java-driver.advanced.retry-policy.class=${RETRY_CLASS}"
JAVA_ARGS="${JAVA_ARGS} -Ddatastax-java-driver.advanced.load-balancing-policy.dc-failover.allow-for-local-consistency-levels=${LOCAL_CONSISTENCY}"
JAVA_ARGS="${JAVA_ARGS} -Ddatastax-java-driver.advanced.load-balancing-policy.dc-failover.max-nodes-per-remote-dc=${REMOTE_DC}"
JAVA_ARGS="${JAVA_ARGS} -Ddatastax-java-driver.advanced.connection.pool.remote.size=${REMOTE_CONN}"
JAVA_ARGS="${JAVA_ARGS} -Dcassandra-client.reconnect-on-no-node-available=${RECONNECT_ON_NO_NODE}"
JAVA_ARGS="${JAVA_ARGS} -Ddatastax-java-driver.basic.load-balancing-policy.local-datacenter=${LOCAL_DATACENTER}"
JAVA_ARGS="${JAVA_ARGS} -Ddatastax-java-driver.basic.load-balancing-policy.class=${LOADBALANCER_CLASS}"
JAVA_ARGS="${JAVA_ARGS} -Ddatastax-java-driver.profiles.dc1.basic.load-balancing-policy.local-datacenter=dc1"
JAVA_ARGS="${JAVA_ARGS} -Ddatastax-java-driver.profiles.dc2.basic.load-balancing-policy.local-datacenter=dc2"

JAVA_ARGS="${JAVA_ARGS} -Xms512m -Xmx512m"



java ${JAVA_ARGS} -cp ${CLASSPATH} com.github.skhatri.cassandra.App
