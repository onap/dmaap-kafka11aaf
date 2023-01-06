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
import org.apache.kafka.common.KafkaException;

public interface IKafkaConfig {

    /**
     * Returns the list of kafka bootstrap servers.
     *
     * @return List of kafka bootstrap servers.
     */
    List<String> getKafkaBootstrapServers();

    /**
     * Kafka security protocol to be used by the client to Auth towards the kafka cluster
     *
     * @return Kafka security.protocol. Default is SASL_PLAINTEXT in the current onap kafka config
     */
    default String getKafkaSecurityProtocolConfig() {
        return "SASL_PLAINTEXT";
    }

    /**
     * Kafka SASL mechanism to be used by the client to Auth towards the kafka cluster
     *
     * @return Kafka sasl.mechanism. Default is SCRAM-SHA-512 in the current onap kafka config
     */
    default String getKafkaSaslMechanism() {
        return "SCRAM-SHA-512";
    }

    /**
     * Kafka JAAS config to be used by the client to Auth towards the kafka cluster.
     * If overridden, must align with sasl.jaas.config convention set out by the sasl.mechanism being used
     * otherwise, mandatory setting of the environment variable SASL_JAAS_CONFIG is required to provide default behaviour
     * @return Kafka sasl.jaas.config
     */
    default String getKafkaSaslJaasConfig() {
        String saslJaasConfFromEnv = System.getenv("SASL_JAAS_CONFIG");
        if(saslJaasConfFromEnv != null) {
            return saslJaasConfFromEnv;
        } else {
            throw new KafkaException("sasl.jaas.config not set for Kafka Consumer");
        }
    }

    /**
     * The timeout in seconds to wait for a response from each poll.
     *
     * @return Client Timeout in seconds. Default is 10 seconds
     */
    default int getPollingTimeout() {
        return 10;
    }

    /**
     * Returns the kafka consumer group defined for this component.
     *
     * @return KafkaConsumer group.
     */
    String getConsumerGroup();

    /**
     * Returns the kafka consumer id defined for this component.
     *
     * @return KafkaConsumer id or null.
     */
    String getConsumerID();

    /**
     * Returns a list of kafka topics to consume from.
     *
     * @return List of kafka topics or empty.
     */
    List<String> getConsumerTopics();

    /**
     * Returns a list of kafka topics to produce to.
     *
     * @return List of kafka topics or empty.
     */
    List<String> getProducerTopics();

}
