# Kafka etl

This project aims to be able to copy kafka from a topic to another and to be able to transform the data format.

## Configuration

Here is a sample of configuration file:

```json
{
  "kafka.consumer.hosts": "kaf1:9042|kaf2:9042",
  "kafka.producer.hosts": "kaf3:9042",
  "kafka.session.timeout": 90000,
  "kafka.request.timeout": 95000,
  "kafka.fetch.retries": 3,
  "transformer.class": "org.kafka.etl.transform.impl.DefaultTransform",
  "transformer.classpath": "/home/ineumann/my-transformer.jar",
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
* `transformer.classpath`: the jar archive that containing your transformer;
* `topic.input`: input topic name;
* `topic.output`: output topic name;
* `group.id`: group id of the consumer of the input topic;
* `poll.size`: number of records that are read and commit in one loop;
* `consumer.record.size`: max size of a record that will be consumed in the input topic;
* `producer.record.size`: max size of a record that will be produced in the output topic.

## Implementation of the transformations rules

All you need to do is to implement the transformations rules by implementing the following java interface :

```java
package org.kafka.etl.transform;

public interface ITransform {
  String transform(String input);
}
```

To be able to implements this interface, your jar module need to use the `kafka-etl-core` maven dependancy.