/*-
 * ============LICENSE_START=======================================================
 * dmaap-kafka-client
 * ================================================================================
 * Copyright (C) 2023 Nordix Foundation. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.dmaap.kafka;

import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.config.SaslConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that provides a KafkaProducer to communicate with a kafka cluster
 */
public class OnapKafkaProducer {

    private final Logger log = LoggerFactory.getLogger(OnapKafkaProducer.class);
    private final KafkaProducer<String, String> producer;
    private final List<String> producerTopics;

    /**
     *
     * @param configuration The config provided to the client
     */
    public OnapKafkaProducer(IKafkaConfig configuration) {
        producerTopics = configuration.getProducerTopics();
        log.debug("Instantiating kafka producer for topics {}", producerTopics);
        Properties props = new Properties();
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, configuration.getKafkaBootstrapServers());
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, configuration.getKafkaSecurityProtocolConfig());
        props.put(SaslConfigs.SASL_MECHANISM, configuration.getKafkaSaslMechanism());
        props.put(SaslConfigs.SASL_JAAS_CONFIG, configuration.getKafkaSaslJaasConfig());
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 10000);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, configuration.getConsumerID() + "-producer-" + UUID.randomUUID());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,  "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(props);
    }

    /**
     *
     * @param topicName The name of the topic to publish the data to
     * @param value The value of the data
     * @return The RecordMetedata of the request
     */
    public RecordMetadata sendDataSynch(String topicName, String value) {
        RecordMetadata data = null;
        try {
            data = producer.send(new ProducerRecord<>(topicName, value)).get();
            log.debug("Data sent to topic {} at partition no {} and offset {}", topicName, data.partition(), data.offset());
        } catch (KafkaException | ExecutionException | InterruptedException e) {
            log.error("Failed the send data: exc {}", e.getMessage());
        } finally {
            producer.flush();
        }
        return data;
    }
}