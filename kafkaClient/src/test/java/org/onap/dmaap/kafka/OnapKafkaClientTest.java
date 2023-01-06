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

import com.salesforce.kafka.test.KafkaTestCluster;
import com.salesforce.kafka.test.KafkaTestUtils;
import com.salesforce.kafka.test.listeners.BrokerListener;
import com.salesforce.kafka.test.listeners.SaslPlainListener;
import io.github.netmikey.logunit.api.LogCapturer;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OnapKafkaClientTest {

    @RegisterExtension
    LogCapturer producerLogs = LogCapturer.create().captureForType(OnapKafkaProducer.class);

    @RegisterExtension
    LogCapturer clientLogs = LogCapturer.create().captureForType(OnapKafkaClient.class);

    private static final Logger logger = LoggerFactory.getLogger(OnapKafkaClientTest.class);

    private static TestConfiguration configuration = new TestConfiguration("application.properties");
    private static final List<String> consumerTopics = configuration.getConsumerTopics();
    private static KafkaTestCluster kafkaTestCluster = null;

    @BeforeAll
    static void before() throws Exception {
        startKafkaService();
        KafkaTestUtils utils = new KafkaTestUtils(kafkaTestCluster);
        for (String topic: consumerTopics) {
            utils.createTopic(topic, 1, (short) 1);
        }
        configuration.setBootstrapServers(Collections.singletonList(kafkaTestCluster.getKafkaConnectString()));
    }

    @AfterAll
    static void after() throws Exception {
        kafkaTestCluster.close();
        kafkaTestCluster.stop();
    }

    @Test
    void whenProducingCorrectRecordsArePresent() {
        OnapKafkaClient handler = new OnapKafkaClient(configuration);
        Assertions.assertEquals(handler.fetchFromTopic(consumerTopics.get(0)).size(), 0);
        handler.publishToTopic(consumerTopics.get(0), "blahblahblahblah");
        handler.publishToTopic(consumerTopics.get(1), "iaerugfoiaeurgfoaiuerf");
        List<String> eventsFrom1 = handler.fetchFromTopic(consumerTopics.get(0));
        Assertions.assertEquals(1, eventsFrom1.size());
        handler.fetchFromTopic(consumerTopics.get(0));
        List<String> events2 = handler.fetchFromTopic(consumerTopics.get(1));
        Assertions.assertEquals( 0, events2.size());
    }

    @Test
    void whenConsumingFromInvalidTopicEmptyListIsReturned() {
        OnapKafkaClient handler = new OnapKafkaClient(configuration);
        List<String> events = handler.fetchFromTopic("invalidTopic");
        Assertions.assertEquals(0, events.size());
    }

    @Test
    void whenPublishingToInvalidTopicExceptionIsLogged() {
        OnapKafkaClient handler = new OnapKafkaClient(configuration);
        RecordMetadata metadata = handler.publishToTopic("invalid.topic", "blahblahblahblah");
        producerLogs.assertContains("Failed the send data");
        Assertions.assertNull(metadata);
    }

    @Test
    void whenSubscribingToInvalidTopicExceptionIsLogged() {
        configuration = new TestConfiguration("invalid-application.properties");
        OnapKafkaClient handler = new OnapKafkaClient(configuration);
        handler.fetchFromTopic("bvserbatb");
        clientLogs.assertContains("Consumer has not been initialised");
        configuration.setConsumerTopics(consumerTopics);
    }


    private static void startKafkaService() throws Exception {
        final BrokerListener listener = new SaslPlainListener()
            .withUsername("kafkaclient")
            .withPassword("client-secret");
        final Properties brokerProperties = new Properties();
        brokerProperties.setProperty("auto.create.topics.enable", "false");
        kafkaTestCluster = new KafkaTestCluster(
            1,
            brokerProperties,
            Collections.singletonList(listener)
        );
        kafkaTestCluster.start();
        logger.debug("Cluster started at: {}", kafkaTestCluster.getKafkaConnectString());
    }

    static {
        System.setProperty("java.security.auth.login.config", "src/test/resources/jaas.conf");
    }
}