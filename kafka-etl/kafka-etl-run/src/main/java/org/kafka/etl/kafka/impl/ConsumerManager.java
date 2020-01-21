package org.kafka.etl.kafka.impl;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.kafka.etl.kafka.IConsumerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

public class ConsumerManager implements IConsumerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerManager.class);

    private final Integer maxPoll;
    private final String consumerHost;
    private final Integer requestTimeout;
    private final Integer sessionTimeout;

    public ConsumerManager(String consumerHost,
                                Integer requestTimeout,
                                Integer sessionTimeout,
                                Integer maxPoll) {
        this.maxPoll = requireNonNull(maxPoll, "maxPoll must not be empty");
        this.consumerHost = requireNonNull(consumerHost, "consumerHost must not be empty");
        this.requestTimeout = requireNonNull(requestTimeout, "requestTimeout must not be empty");
        this.sessionTimeout = requireNonNull(sessionTimeout, "sessionTimeout must not be empty");
    }

    @Override
    public KafkaConsumer<String, String> getConsumer(String groupId, String topic, Map<String, Object> kafkaConsumerAdditionalConfig) {
        LOGGER.info("[ConsumerManager][getConsumer][singleTopic] Creating consumer with the groupId config : groupId = {}, topic = {}", groupId, topic);
        return getConsumer(groupId, Collections.singletonList(topic), kafkaConsumerAdditionalConfig);
    }

    @Override
    public KafkaConsumer<String, String> getConsumer(String groupId, List<String> topics, Map<String, Object> kafkaAdditionalConsumerConfig) {
        Properties config = getConsumerConfig(groupId, kafkaAdditionalConsumerConfig);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(config);
        consumer.subscribe(topics);
        LOGGER.info("[ConsumerManager][getConsumer][topics] Creating consumer with the following config : topics = {}, group = {}, conf = {}", topics, groupId, config);
        return consumer;
    }

    private Properties getConsumerConfig(String groupId,
                                         Map<String, Object> kafkaAdditionalConsumerConfig) {
        return GenericConfig.defaultConsumerConfig(groupId, kafkaAdditionalConsumerConfig, consumerHost,
                requestTimeout,
                sessionTimeout,
                maxPoll);
    }
}
