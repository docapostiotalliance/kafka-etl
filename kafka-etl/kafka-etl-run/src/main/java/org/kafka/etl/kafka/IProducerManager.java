package org.kafka.etl.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public interface IProducerManager {
    void sendEvent(String key, String event, String topic, IProducerCallback callback);

    void sendEvent(KafkaProducer<String, String> producer, String key, String event, String topic, IProducerCallback callback);

    KafkaProducer<String, String> getProducer();
}
