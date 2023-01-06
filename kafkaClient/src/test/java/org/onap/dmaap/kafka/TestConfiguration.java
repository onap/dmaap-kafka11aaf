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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import lombok.SneakyThrows;

public class TestConfiguration implements org.onap.dmaap.kafka.IKafkaConfig {

	private Properties loadProperties(String configFileName) throws IOException {
		Properties configuration = new Properties();
		try (InputStream inputStream = TestConfiguration.class
			.getClassLoader()
			.getResourceAsStream(configFileName)) {
			configuration.load(inputStream);
		}
		return configuration;
	}

	private final Properties testConfig;
	private List<String> bootstrapServers;
	private List<String> consumerTopics;

	@SneakyThrows
	public TestConfiguration(String configFilename) {
		testConfig = loadProperties(configFilename);
		bootstrapServers = new ArrayList<>(Arrays.asList(((String) testConfig.get("kafka.kafkaBootstrapServers")).split(",")));
	}

	@Override
	public List<String> getKafkaBootstrapServers() {
		return bootstrapServers;
	}

	public void setBootstrapServers(List<String> newBootstrapList) {
		bootstrapServers = newBootstrapList;
	}

	@Override
	public String getKafkaSaslMechanism() {
		return "PLAIN";
	}

	@Override
	public String getKafkaSaslJaasConfig() {
		return "org.apache.kafka.common.security.plain.PlainLoginModule required username=admin password=admin-secret;";
	}

	@Override
	public int getPollingTimeout() {
		return Integer.parseInt((String) testConfig.get("kafka.pollingTimeout"));
	}

	@Override
	public String getConsumerGroup() {
		return (String) testConfig.get("kafka.consumerGroup");
	}

	@Override
	public String getConsumerID() {
		return (String) testConfig.get("kafka.consumerID");
	}

	@Override
	public List<String> getConsumerTopics() {
		consumerTopics = new ArrayList<>();
		String topicString = (String) testConfig.get("kafka.consumerTopics");
		if (topicString != null) {
			consumerTopics.addAll(Arrays.asList((topicString).split(",")));
		}
		return consumerTopics;
	}

	public void setConsumerTopics(List<String> newTopics) {
		this.consumerTopics = newTopics;
	}

	@Override
	public List<String> getProducerTopics() {
		List<String> producerTopics = new ArrayList<>();
		String topicString = (String) testConfig.get("kafka.producerTopics");
		if (topicString != null) {
			producerTopics.addAll(Arrays.asList((topicString).split(",")));
		}
		return producerTopics;
	}
}
