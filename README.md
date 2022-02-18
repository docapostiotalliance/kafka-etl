# Kafka ETL

This project aims to facilitate the copying of kafka records from one topic to another (or something else) while transforming them beforehand.

## Git repo

* Main working repo: https://gitlab.comwork.io/oss/docapostiotalliance/kafka-etl
* Github mirror backup: https://github.com/docapostiotalliance/kafka-etl
* Gitlab mirror backup: https://gitlab.com/ineumann/kafka-etl
* Bitbucket mirror backup: https://bitbucket.org/idrissneumann/kafka-etl

The releases are published on github here: https://github.com/docapostiotalliance/kafka-etl/releases

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
  "transformer.jar.path": "/transformer.jar",
  "partition.key.calculator.class": "org.kafka.etl.kafka.impl.DefaultPartitionKeyCalculator",
  "partition.key.calculator.jar.path": "/transformer.jar",
  "avro.json.schema.path": "/my-schema.json",
  "group.id": "etl",
  "topic.input": "IN",
  "topic.output": "OUT",
  "loader.class": "org.kafka.etl.load.impl.ElasticSearchLoader",
  "loader.jar.path": "/loader.jar",
  "poll.timeout": 1000,
  "poll.size": 10,
  "consumer.record.size": 4194304,
  "producer.record.size": 4194304,
  "avro.data.bytes.start.offset": 5,
  "fail.on.empty.json": true
}
```

* `kafka.consumer.hosts`: input broker (host and port pair) that contain the input topic;
* `kafka.producer.hosts`: output broker (host and port pair) that hosts the output topic;
* `transformer.class`: the name of the transformer class (for more details in the next section);
* `transformer.jar.path`: the path of an external jar file that contain your transformer rules class;
* `topic.input`: input topic name;
* `topic.output`: (optional): output topic name (if present, it will enable the default `KafkaLoader` that will load the data in the output topic);
* `loader.class`: (optional): the name of the loader class (for more details in the loader section);
* `loader.jar.path`: (optional): the path of an external jar file that contain your loader class;
* `partition.key.calculator.class`: (optional): the name of the partition key calculator class (for more details in the loader section);
* `partition.key.calculator.jar.path`: (optional): the path of an external jar file that contain your partition key calculator class;
* `group.id`: group id of the consumer hosting the input topic;
* `poll.size`: number of records to are read and commit in one loop;
* `consumer.record.size`: max size of a record that will be consumed in the input topic;
* `producer.record.size`: max size of a record that will be produced in the output topic;
* `avro.json.schema.path`: (optional): path to a json file that contain the avro schema to unserialize data;
* `avro.data.bytes.start.offset`: (optional): start offset to decode with avro schema (to skip the first bytes that contain the schema id).
* `fail.on.empty.json`: (optional): fail if the deserialized json from avro has all field empty (non-valid schema). Default is `true`.

## Implementation of the transformations rules

Transformation rules are for mutating records after they get consumed from their original kafka broker (then optionally deserialized) and before they get produced into their destination.

In order to be able to create a data transformer, you need to include the `kafka-etl-core` JAR file into your dependencies and implement the following interface :

```java
package org.kafka.etl.transform;

import java.util.Map;
import java.util.Optional;

public interface ITransform {
  Optional<String> transform(String input, Map<String, String> metadata);
}
```

* `input`: the data input string to transform in whatever format you want;
* `metadata`: additional informations of the original kafka message (offset number, partition number, partition key and topic name). That can be usefull in order to keep it in your own supervision system (log files, ElasticStack, Prometheus+Grafana...).

If the implementation does return an empty `Optional`, the data will be skipped and not send to the output topic.

## Implementation of another Loader

This ETL provide a `KafkaLoader` implementation which aims to load you transformed data into an output kafka topic.

But sometimes you want to load the data into something else like databases, distributed caches, other message queueing systems, webservices or http endpoints... that's the point of using an ETL after all. 

In order to load the transformed data, you need to include the `kafka-etl-core` JAR file into your dependencies and implement the following interface :

```java
package org.kafka.etl.load;

public interface ILoad {
  // Injecting the json configuration file content into a java.util.Properties object
  ILoad init(Properties properties);

  void loadEvent(String originalKey, String event);

  // Close persistent TCP connections at the end
  void close();
}
```

Then, replace the `topic.output` property in your config files by `loader.class` and `loader.jar.path`.

## Implementation of another PartitionKeyCalculator

This ETL provide a `DefaultPartitionKeyCalculator` implementation which set input topic partition key as partition key.

But sometimes you want to set the partition key manually.

In order to create a customized PartitionKeyCalculator, you need to include the `kafka-etl-core` JAR file into your dependencies and implement the following interface :

```java
package org.kafka.etl.load;

public interface IPartitionKeyCalculator {
    String generatePartitionKey(String originalKey, String message);
}
```
You need to set the properties  `partition.key.calculator.class` and `partition.key.calculator.jar.path` in your config files by .
# Running the project

## Build the project

First, compile the `etl-run-project` as follows :

```shell
$ cd ~/kafka-etl/kafka-etl
$ mvn clean install
```

This `mvn` command will generate a jar file named `kafka-etl-core-1.0.0-SNAPSHOT.jar`.

Then, compile your jar containing an implementation of the `ITransform` interface. 

In order to facilitate testing, you could use the default implementation that only but logs its inout, which can be found under the `kafka-etl-core` artifact.

## Test with docker-compose (development environment)

### Run kafka and zookeeper

1. Inside the `docker-compose.yml`, configure the topics to be created inside the kafka container, as follows:

```
KAFKA_CREATE_TOPICS: "IN:10:1,OUT:10:1"
```

The topic names must correspond to the `topic.input` and `topic.output` entries in the JSON configuration file:

```json
{
  "topic.input": "IN",
  "topic.output": "OUT"
}
```

Their format is as follows:

```
topic_name:number_of_partitions:number_of_replicas
```

2. Start the containers

```shell
$ cd ~/kafka-etl/kafka-etl
$ docker-compose up etl_kafka
```

### Run the ETL

1. Configure the JSON file to contain the ip of the of the `etl_kafka` container inside your docker network. Alternatively, these entries reference your input and output brokers.

```json
{
  "kafka.consumer.hosts": "172.21.0.3:9092",
  "kafka.producer.hosts": "172.21.0.3:9092"
}
```

2. Update in the `docker-compose.yml` to include the jar containing your `ITransform` interface implementation by replacing the `./kafka-etl/kafka-etl-core/target/kafka-etl-core-1.0.0-SNAPSHOT.jar` with that of your implementation :

```yaml
volumes:
  - ./kafka-etl/kafka-etl-run/src/main/resources/configuration.json:/config.json:z
  - ./kafka-etl/kafka-etl-run/target/kafka-etl-run-runnable.jar:/kafka-etl-runnable.jar:z
  - ./kafka-etl/kafka-etl-core/target/kafka-etl-core-1.0.0-SNAPSHOT.jar:/transformer.jar:z
command: /bin/bash -c "java -jar /kafka-etl-runnable.jar -conf /config.json && while true; do echo \"debug with 'docker exec -it etl_run bash'\"; sleep 20; done"
```

3. Start the container

```shell
$ cd ~/kafka-etl/kafka-etl
$ docker-compose up etl_run
```

## Run in production

Follow the previous section but:
- skip the kafka and zookeeper part (we will assume that you already have your kafka brokers installed in production);
- replace the ips by your kafka production hostnames in the JSON configuration file;
- make another `docker-compose.yml` file that only contain the following :

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
    command: /bin/bash -c "java -jar /kafka-etl-runnable.jar -conf /config.json && while true; do echo \"debug with 'docker exec -it etl_run bash'\"; sleep 20; done"
```

Replace the `kafka-etl-core-1.0.0-SNAPSHOT.jar` jar file by your own jar implementing `ITransform` interface.

Optionally, you can also use another JSON configuration file managed by something like puppet/chef/ansible (you will also need to change the volume path if it's the case).

If you're using your own loader implementation, don't forger to add a volume to mount your own loader jar file in the place that correspond to your `loader.jar.path` property.

## Troubleshooting

### Debuging network in the etl_run container

```bash
$ docker exec -it etl_run bash
root@94374f0953cd:/# apt-get update -y; apt-get install dnsutils telnet net-tools vim nmap -y
```

## Contributions / pull requests

In order to submit a pull request, make sure that you meet the following requirements.

### Code format


You have to follow the same code format as everyone else in order to have a better view of the real changes in the pull requests. 
As such, the project contains a code formatter under the root directory. 

To install it, run the following command : 

```bash
$ cd ~/kafka-etl/code-formatter
$ ./installPreCommit.sh
```

### Continuous integration

Please be sure to keep the pipeline in an ok state (maven compile ok and unit tests ok) when you submit a pull request.
You can find the pipelines [here](https://github.com/docapostiotalliance/kafka-etl/actions).

