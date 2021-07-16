/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  org.onap.dmaap
 *  ================================================================================
 *  Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 *  Modification copyright (C) 2021 Nordix Foundation.
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
package org.onap.dmaap.kafkaauthorize;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;

@PowerMockIgnore({"jdk.internal.reflect.*"})
@PrepareForTest({ PlainLoginModule1.class })
public class PlainLoginModule1Test {

	static PlainLoginModule1 pLogin = new PlainLoginModule1();
	static Subject subject;
	@Mock
	static CallbackHandler callbackHandler;

	@Mock
	static Map<String, String> mymap1;

	@Mock
	static Map<String, ?> mymap2;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		PowerMockito.when(mymap1.get("username")).thenReturn("user1");
		PowerMockito.when(mymap1.get("password")).thenReturn("pass1");
		pLogin.initialize(subject, callbackHandler, mymap1, mymap2);
	}

	@Test
	public void testLogin() {
		assertTrue(pLogin.login());
	}
	
	@Test
	public void testLogout() {
		assertTrue(pLogin.logout());
	}
	
	@Test
	public void testCommit() {
		assertTrue(pLogin.commit());
	}
	
	@Test
	public void testAbort() {
		assertFalse(pLogin.abort());
	}
}
