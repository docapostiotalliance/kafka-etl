# Kafka ETL

This project aims to be make easier the copy of kafka records from a topic to another and to be able to transform the data before copying them.

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
  "topic.input": "IN"``,
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

To be able to implements this interface, your jar module need to use the `kafka-etl-core` maven dependency.

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

## Running with docker-compose

# Troubleshooting

## Debuging network in the etl_run container

```bash
$ docker exec -it elt_run bash
root# apt-get update -y
root# apt-get install dns-utils telnet net-tools vim nmap -y
```