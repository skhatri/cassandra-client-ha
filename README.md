## Cassandra Client Behavior
This project stands up a 2-node cassandra cluster with a node each on its own dc - dc1 and dc2.

Start it up with 

``` 
docker-compose up -d
```

### Documentation
https://docs.datastax.com/en/dseplanning/docs/metricsandalerts.html
https://docs.datastax.com/en/developer/java-driver/4.14/manual/core/load_balancing/#cross-datacenter-failover


#### Build the JAR

```
gradle clean build
```

Run the following tasks:

#### Tests

#### Option 1

when both are up and one goes down
```
does not recover
```
when local DC is down

```
does not start
```

#### Option 2

when both are up and one goes down
```
does not recover until DC comes up
```

when local DC is down
```
does not connect
```

#### Option 3

when both are up and one goes down
```
switches to another dc
```
when local DC is down
```
does not initiate connection to remote initially
```

#### Option 4

when both are up and one goes down
```
switches to another dc
recovers when another dc is up
```
when local DC is down
```
switches to another dc
```

#### Option 5

when both are up and one goes down
```
```
when local DC is down
```
```

#### Option 6

when both are up and one goes down
```
recovers when DC is up
```
when local DC is down
```
uses another DC
```

#### Option 7

when both are up and one goes down
```
recovers when DC is up
```
when local DC is down
```
```
