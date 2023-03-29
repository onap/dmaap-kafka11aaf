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
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.onap.dmaap.kafka.IKafkaConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kafka")
@Getter
@Setter
public class SampleConfiguration implements IKafkaConfig {

    @NonNull
    private List<String> kafkaBootstrapServers;

    private List<String> consumerTopics;
    private String consumerGroup;
    private String consumerID;
    private int pollingTimeout;

    private List<String> producerTopics;
    private String kafkaSaslJaasConfig;
}
