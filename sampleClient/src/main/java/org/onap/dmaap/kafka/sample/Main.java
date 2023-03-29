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

package org.onap.dmaap.kafka.sample;

import java.util.List;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.onap.dmaap.kafka.OnapKafkaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main implements CommandLineRunner{

    private final Logger log = LoggerFactory.getLogger(OnapKafkaClient.class.getName());

    @Autowired
    private SampleConfiguration configuration;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws InterruptedException {
        OnapKafkaClient handler = new OnapKafkaClient(configuration);
        String testTopic = configuration.getConsumerTopics().get(0);
        for (int i = 0; i < 5; i++) {
            RecordMetadata recordMetadata = handler.publishToTopic(testTopic, "dummy-message-"+i);
            if (recordMetadata != null) {
                log.info("Topic: {}, Partition: {}, Offset: {}", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
            }
        }
        int fetch = 0;
        while (true) {
            fetch++;
            log.info("Fetch {} from topic: {}", fetch, testTopic);
            List<String> res = handler.fetchFromTopic(testTopic);
            log.info("Messages from fetch {}: " + res, fetch);
            Thread.sleep(3000);
        }
    }
}