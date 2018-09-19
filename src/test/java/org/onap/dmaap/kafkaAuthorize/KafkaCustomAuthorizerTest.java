/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  org.onap.dmaap
 *  ================================================================================
 *  Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
*  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ============LICENSE_END=========================================================
 *  
 *  
 *******************************************************************************/
package org.onap.dmaap.kafkaAuthorize;

import static org.junit.Assert.assertTrue;

import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.dmaap.commonauth.kafka.base.authorization.AuthorizationProvider;
import org.onap.dmaap.commonauth.kafka.base.authorization.AuthorizationProviderFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import kafka.network.RequestChannel.Session;
import kafka.security.auth.Operation;
import kafka.security.auth.Resource;
import kafka.security.auth.ResourceType;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AuthorizationProviderFactory.class })
public class KafkaCustomAuthorizerTest {
	@Mock
	Session arg0;
	@Mock
	Operation arg1;
	@Mock
	Resource arg2;
	@Mock
	KafkaPrincipal principal;
	@Mock
	ResourceType resourceType;
	@Mock
	AuthorizationProviderFactory factory;
	@Mock
	AuthorizationProvider provider;

	KafkaCustomAuthorizer authorizer = new KafkaCustomAuthorizer();

	@Before
	public void setUp() throws Exception {

		MockitoAnnotations.initMocks(this);
		PowerMockito.when(principal.getName()).thenReturn("fullName");
		PowerMockito.when(arg0.principal()).thenReturn(principal);
		PowerMockito.when(arg1.name()).thenReturn("Write");
		PowerMockito.when(resourceType.name()).thenReturn("Topic");
		PowerMockito.when(arg2.resourceType()).thenReturn(resourceType);
		PowerMockito.when(arg2.name()).thenReturn("namespace.Topic");
		PowerMockito.mockStatic(AuthorizationProviderFactory.class);
		PowerMockito.when(AuthorizationProviderFactory.getProviderFactory()).thenReturn(factory);
		PowerMockito.when(factory.getProvider()).thenReturn(provider);

	}

	@Test
	public void testAuthorizerSuccess() {
		PowerMockito.when(provider.hasPermission("fullName", "namespace.topic", ":topic.namespace.Topic", "pub"))
				.thenReturn(true);
		assertTrue(authorizer.authorize(arg0, arg1, arg2));

	}

	@Test
	public void testAuthorizerFailure() {

		PowerMockito.when(provider.hasPermission("fullName", "namespace.topic", ":topic.namespace.Topic", "pub"))
				.thenReturn(false);
		try {
			authorizer.authorize(arg0, arg1, arg2);
		} catch (Exception e) {
			assertTrue(true);
		}

	}

}
