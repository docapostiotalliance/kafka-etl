package org.kafka.etl.kafka.impl;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections4.MapUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;

public class GenericConfig {
    private static final String KAFKA_OFFSET_RESET_SMALLEST = "latest";

    private GenericConfig() {
        // Nothing to do
    }

    public static Properties defaultProducerConfig(Map<String, Object> kafkaAdditionalProducerConfig,
                                                   String kafkaHost,
                                                   Integer requestTimeout,
                                                   Integer kafkaRetries) {
        Properties config = new Properties();

        config.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeout);
        config.put(ProducerConfig.RETRIES_CONFIG, kafkaRetries);
        config.put(ProducerConfig.ACKS_CONFIG, "1");



        if (MapUtils.isNotEmpty(kafkaAdditionalProducerConfig)) {
            config.putAll(kafkaAdditionalProducerConfig);
        }

        return config;
    }

    public static Properties defaultConsumerConfig(String groupId,
                                                   Map<String, Object> kafkaAdditionalConsumerConfig,
                                                   String consumerHost,
                                                   Integer requestTimeout,
                                                   Integer sessionTimeout,
                                                   Integer maxPoll) {
        Properties config = new Properties();

        config.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, consumerHost);
        config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeout);
        config.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeout);
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPoll);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, KAFKA_OFFSET_RESET_SMALLEST);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        if (MapUtils.isNotEmpty(kafkaAdditionalConsumerConfig)) {
            config.putAll(kafkaAdditionalConsumerConfig);
        }

        return config;
    }
}

