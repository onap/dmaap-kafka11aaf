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

import static org.junit.Assert.assertNotNull;

import javax.security.sasl.SaslException;

import org.apache.kafka.common.security.JaasContext;
import org.apache.kafka.common.security.plain.PlainSaslServer;
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

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AuthorizationProviderFactory.class })
public class PlainSaslServer1Test {

	PlainSaslServer1 sslServer = new PlainSaslServer1(null);
	@Mock
	JaasContext jaasContext;
	@Mock
	AuthorizationProviderFactory factory;
	@Mock
	AuthorizationProvider provider;

	@Before
	public void setUp() throws Exception {

		MockitoAnnotations.initMocks(this);
		PowerMockito.mockStatic(AuthorizationProviderFactory.class);
		PowerMockito.when(AuthorizationProviderFactory.getProviderFactory()).thenReturn(factory);
		PowerMockito.when(factory.getProvider()).thenReturn(provider);
	}

	@Test
	public void testAuthentication() throws Exception {
		String response = "authorizationID\u0000username\u0000password";
		PowerMockito.when(provider.authenticate("username", "password")).thenReturn(null);
		assertNotNull(sslServer.evaluateResponse(response.getBytes()));

	}
}
