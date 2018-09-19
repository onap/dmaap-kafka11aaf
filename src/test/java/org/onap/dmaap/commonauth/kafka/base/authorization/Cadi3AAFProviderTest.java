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
package org.onap.dmaap.commonauth.kafka.base.authorization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.aaf.v2_0.AAFAuthn;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class Cadi3AAFProviderTest {

	public Cadi3AAFProvider cadi3AAFProvider;

	@Mock
	private static AAFAuthn<?> aafAuthn;

	@Mock
	private static PropAccess access;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testHasPermission() {
		System.setProperty("CADI_PROPERTIES", "src/test/resources/cadi.properties");
		cadi3AAFProvider = new Cadi3AAFProvider();
		assertFalse(cadi3AAFProvider.hasPermission("userID", "permission", "instance", "action"));
	}

	@Test(expected = NullPointerException.class)
	public void tesAuthenticate() throws Exception {
		System.setProperty("CADI_PROPERTIES", "src/test/resources/cadi.properties");
		cadi3AAFProvider = new Cadi3AAFProvider();
		when(aafAuthn.validate("userId", "password")).thenReturn("valid");
		assertEquals(cadi3AAFProvider.authenticate("userId", "password"), "valid");
	}

	@Test
	public void tesAuthenticateadmin() throws Exception {
		System.setProperty("CADI_PROPERTIES", "src/test/resources/cadi.properties");
		cadi3AAFProvider = new Cadi3AAFProvider();
		when(aafAuthn.validate("admin", "password")).thenReturn("valid");
		assertNull(cadi3AAFProvider.authenticate("admin", "password"));
	}

}
