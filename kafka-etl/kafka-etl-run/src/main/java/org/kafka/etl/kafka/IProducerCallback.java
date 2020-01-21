package org.kafka.etl.kafka;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;

public interface IProducerCallback {
    Callback producerCallback(String event, ProducerRecord<String, String> record);
}
