/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2022 Nordix Foundation. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that provides a handler for Kafka interactions
 */
public class OnapKafkaClient {

    private final Logger log = LoggerFactory.getLogger(OnapKafkaClient.class.getName());

    private OnapKafkaConsumer onapKafkaConsumer = null;

    private final OnapKafkaProducer onapKafkaProducer;

    public OnapKafkaClient(IKafkaConfig configuration) {
        if (!configuration.getConsumerTopics().isEmpty()) {
            onapKafkaConsumer = new OnapKafkaConsumer(configuration);
            onapKafkaConsumer.subscribeConsumerToTopics();
        }
        onapKafkaProducer = new OnapKafkaProducer(configuration);
    }

    /**
     * @param topicName The topic from which messages will be fetched
     * @return A list of messages from a specific topic
     */
    public List<String> fetchFromTopic(String topicName) {
        List<String> messages =  new ArrayList<>();
        if (onapKafkaConsumer != null) {
            try {
                log.debug("Polling for messages from topic: {}", topicName);
                messages = onapKafkaConsumer.poll(topicName);
                log.debug("Returning messages from topic {}", topicName);
                return messages;
            } catch (KafkaException e) {
                log.error("Failed to fetch from kafka for topic: {}", topicName, e);
            }
        } else {
            log.error("Consumer has not been initialised with the required topic list");
        }
        return messages;
    }

    /**
     * Publish data to a given topic
     *  @param topicName The topic to which the message should be published
     * @param data      The data to publish to the topic specified
     * @return
     */
    public RecordMetadata publishToTopic(String topicName, String data) {
        // Should we check the data size and chunk it if necessary? Do we need to?
        return onapKafkaProducer.sendDataSynch(topicName, data);
    }
}
