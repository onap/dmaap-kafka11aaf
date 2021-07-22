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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;
import org.apache.kafka.common.errors.SaslAuthenticationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.dmaap.commonauth.kafka.base.authorization.AuthorizationProvider;
import org.onap.dmaap.commonauth.kafka.base.authorization.AuthorizationProviderFactory;
import org.onap.dmaap.kafkaAuthorize.PlainSaslServer1.PlainSaslServerFactory1;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.security.auth.*", "jdk.internal.reflect.*", "javax.crypto.*"})
@PrepareForTest({ AuthorizationProviderFactory.class })
public class PlainSaslServer1Test {

	PlainSaslServer1 sslServer = new PlainSaslServer1();

	@Mock
	AuthorizationProviderFactory factory;
	@Mock
	AuthorizationProvider provider;
	@Mock
	CallbackHandler callbackHandler;
	@Mock
	static Map<String, String> props;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		PowerMockito.mockStatic(AuthorizationProviderFactory.class);
		PowerMockito.when(AuthorizationProviderFactory.getProviderFactory()).thenReturn(factory);
		PowerMockito.when(factory.getProvider()).thenReturn(provider);
	}

	public void testAuthentication() throws Exception {
		String response = "authorizationID\u0000username\u0000password";
		PowerMockito.when(provider.authenticate("username", "password")).thenReturn(null);
		assertNotNull(sslServer.evaluateResponse(response.getBytes()));

	}

	@Test
	public void testAuthenticationEmptyAuth() throws Exception {
		String response = "\u0000username\u0000password";
		PowerMockito.when(provider.authenticate("username", "password")).thenReturn(null);
		assertNotNull(sslServer.evaluateResponse(response.getBytes()));
	}

	@Test
	public void testAuthenticationEmptyUser() throws Exception {
		String response = "authorizationID\u0000\u0000password";
		PowerMockito.when(provider.authenticate("username", "password")).thenReturn(null);
		try {
			sslServer.evaluateResponse(response.getBytes());
		}
		catch (SaslAuthenticationException e) {
			assertNotNull(e);
		}
	}

	@Test
	public void testAuthenticationEmptyPassword() throws Exception {
		String response = "authorizationID\u0000username\u0000";
		PowerMockito.when(provider.authenticate("username", "password")).thenReturn(null);
		try {
			sslServer.evaluateResponse(response.getBytes());
		}
		catch (SaslAuthenticationException e) {
			assertNotNull(e);
		}
	}
	
	@Test
	public void testGetAuthorizationIdWithException() {
		try {
		sslServer.getAuthorizationID();
		}
		catch (IllegalStateException ise) {
			assertTrue(ise.getMessage().equalsIgnoreCase("Authentication exchange has not completed"));
		}
	}

	@Test
	public void testGetNegotiatedPropertyWithException() {
		try {
		sslServer.getNegotiatedProperty("test");
		}
		catch (IllegalStateException ise) {
			assertTrue(ise.getMessage().equalsIgnoreCase("Authentication exchange has not completed"));
		}
	}
	
	@Test
	public void testIsComplete() {
		try {
		sslServer.getNegotiatedProperty("test");
		}
		catch (IllegalStateException ise) {
			assertTrue(ise.getMessage().equalsIgnoreCase("Authentication exchange has not completed"));
		}
		assert(true);
	}	

	@Test
	public void testUnwrap() {
		try {
		sslServer.unwrap(new byte[1], 0, 0);
		}
		catch (IllegalStateException ise) {
			assertTrue(ise.getMessage().equalsIgnoreCase("Authentication exchange has not completed"));
		} catch (SaslAuthenticationException e) {
			e.printStackTrace();
		}
		assert(true);
	}	
	
	@Test
	public void testWrap() {
		try {
		sslServer.wrap(new byte[1], 0, 0);
		}
		catch (IllegalStateException ise) {
			assertTrue(ise.getMessage().equalsIgnoreCase("Authentication exchange has not completed"));
		} catch (SaslAuthenticationException e) {
			e.printStackTrace();
		}
		assert(true);
	}

	@Test
	public void testGetMech() {
		assertEquals("PLAIN", sslServer.getMechanismName());
	}

	@Test
	public void testIsCompleteBool() {
		assertFalse(sslServer.isComplete());
	}

	@Test
	public void testPlainSaslServer1() throws SaslException {
		PlainSaslServerFactory1 plainSaslServerFactory1 = new PlainSaslServerFactory1();
		PlainSaslServer1 saslServer1 = (PlainSaslServer1) plainSaslServerFactory1.createSaslServer(PlainSaslServer1.PLAIN_MECHANISM, "https", "mySaslServer", props, callbackHandler);
		assertNotNull(saslServer1);
		Mockito.when(props.get(Sasl.POLICY_NOPLAINTEXT)).thenReturn("javax.security.sasl.policy.noplaintext");
		assertEquals(new String[]{"PLAIN"}, plainSaslServerFactory1.getMechanismNames(props));
	}
}
