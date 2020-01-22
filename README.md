# Kafka ETL

This project aims to make easier the copy of kafka records from a topic to another and to be able to transform the data before copying them.

## Configuration description

Here is a sample of configuration file:

```json
{
  "kafka.consumer.hosts": "kaf1:9042",
  "kafka.producer.hosts": "kaf2:9042",
  "kafka.session.timeout": 90000,
  "kafka.request.timeout": 95000,
  "kafka.fetch.retries": 3,
  "transformer.class": "org.kafka.etl.transform.impl.DefaultTransform",
  "avro.json.schema.path": "/home/ineumann/my-schema.json",
  "group.id": "etl",
  "topic.input": "IN",
  "topic.output": "OUT",
  "poll.timeout": 1000,
  "poll.size": 10,
  "consumer.record.size": 4194304,
  "producer.record.size": 4194304
}
```

* `kafka.consumer.hosts`: broker (hosts and ports) that contain the input topic;
* `kafka.producer.hosts`: broker (hosts and ports) that contain the output topic;
* `transformer.class`: the name of the transformer rules class (go see the next section to get more details);
* `topic.input`: input topic name;
* `topic.output`: output topic name;
* `group.id`: group id of the consumer of the input topic;
* `poll.size`: number of records that are read and commit in one loop;
* `consumer.record.size`: max size of a record that will be consumed in the input topic;
* `producer.record.size`: max size of a record that will be produced in the output topic.
* `avro.json.schema.path` (optional): path to a json file that contain the avro schema to unserialize data.

## Implementation of the transformations rules

All you need to do is to implement the transformations rules by implementing the following java interface :

```java
package org.kafka.etl.transform;

public interface ITransform {
  String transform(String input);
}
```

To be able to implement this interface, your jar module need to use the `kafka-etl-core` as a maven (or graddle or whatever) dependency.

# Run the project

## Build the project

First, compile the `etl-run-project` like that:

```shell
$ cd ~/kafka-etl/kafka-etl
$ mvn clean install
```

Then, compile your jar that contain an implementation of the `ITransform` interface. 

In order to test quickly, you could use the default implementation that doesn't do anything but log a trace, which is present in the `kafka-etl-core` artifact.

In this case, the previous `mvn` command has also generate a jar file which is named `kafka-etl-core-1.0.0-SNAPSHOT.jar`

## Test with docker-compose (development environment)

### Run kafka and zookeeper

1. Check in the `docker-compose.yml` the topics that will be created inside the kafka container, for example:

```
KAFKA_CREATE_TOPICS: "IN:10:1,OUT:10:1"
```

The values must be the same as the `topic.input` and `topic.output` entries in the JSON configuration file:

```json
{
  "topic.input": "IN",
  "topic.output": "OUT"
}
```

The format in the is the following:

```
topic_name:number_of_partitions:number_of_replicas
```

2. Start the containers

```shell
$ cd ~/kafka-etl/kafka-etl
$ docker-compose up etl_kafka
```

### Run the ETL

1. Check the ip inside the docker network of the `etl_kafka` container, then replace it in the JSON configuration file:

```json
{
  "kafka.consumer.hosts": "172.21.0.3:9092",
  "kafka.producer.hosts": "172.21.0.3:9092"
}
```

2. Replace in the `docker-compose.yml`, the jar that contain your implementation of the `ITransform` interface:

```yaml
volumes:
  - ./kafka-etl/kafka-etl-run/src/main/resources/configuration.json:/config.json:z
  - ./kafka-etl/kafka-etl-run/target/kafka-etl-run-runnable.jar:/kafka-etl-runnable.jar:z
  - ./kafka-etl/kafka-etl-core/target/kafka-etl-core-1.0.0-SNAPSHOT.jar:/transformer.jar:z
command: /bin/bash -c "java -jar /kafka-etl-runnable.jar -conf /config.json -classpath /transformer.jar:* && while true; do echo \"debug with 'docker exec -it etl_run bash'\"; sleep 20; done"
```

You need to replace the `./kafka-etl/kafka-etl-core/target/kafka-etl-core-1.0.0-SNAPSHOT.jar` path by your own jar file if you want to use your implementation.

3. Start the container

```shell
$ cd ~/kafka-etl/kafka-etl
$ docker-compose up etl_run
```

## Run in production

Follow the previous section but:
- skip the kafka and zookeeper part (we will assume that you already have your kafka brokers installed in production);
- replace the ips by your kafka production hostnames in the JSON configuration file;
- make another `docker-compose.yml` file that only contain:

```yaml
version: "2"

services:
  etl_run:
    image: openjdk:8
    container_name: etl_run
    restart: always
    volumes:
      - ./kafka-etl/kafka-etl-run/src/main/resources/configuration.json:/config.json:z
      - ./kafka-etl/kafka-etl-run/target/kafka-etl-run-runnable.jar:/kafka-etl-runnable.jar:z
      - ./kafka-etl/kafka-etl-core/target/kafka-etl-core-1.0.0-SNAPSHOT.jar:/transformer.jar:z
    command: /bin/bash -c "java -jar /kafka-etl-runnable.jar -conf /config.json -classpath /transformer.jar:* && while true; do echo \"debug with 'docker exec -it etl_run bash'\"; sleep 20; done"
```

Replace the `kafka-etl-core-1.0.0-SNAPSHOT.jar` jar file by your own jar implementing `ITransform` interface.

You can also use another JSON configuration file that will be manage by something like puppet/chef/ansible (you need to change the volume path if it's the case).

## Troubleshooting

### Debuging network in the etl_run container

```bash
$ docker exec -it etl_run bash
root@94374f0953cd:/# apt-get update -y; apt-get install dnsutils telnet net-tools vim nmap -y
```
