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
package org.onap.dmaap.commonauth.kafka.base.authorization;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorizationProviderFactory {
	private static final Logger logger = LoggerFactory.getLogger(AuthorizationProviderFactory.class);
	private static final Map<String, AuthorizationProvider> AUTHORIZATION_PROVIDER_MAP = new HashMap<>();
	private static final AuthorizationProviderFactory AUTHORIZATION_PROVIDER_FACTORY = new AuthorizationProviderFactory();

	private AuthorizationProviderFactory() {
		try {
			ServiceLoader<AuthorizationProvider> serviceLoader = ServiceLoader.load(AuthorizationProvider.class);
			for (AuthorizationProvider authzProvider : serviceLoader) {
				AUTHORIZATION_PROVIDER_MAP.put(authzProvider.getId(), authzProvider);

			}
		} catch (Exception ee) {
			logger.error(ee.getMessage(), ee);
			System.exit(0);
		}
	}

	public static AuthorizationProviderFactory getProviderFactory() {
		return AUTHORIZATION_PROVIDER_FACTORY;
	}

	public AuthorizationProvider getProvider() {
		return AUTHORIZATION_PROVIDER_MAP.get(System.getProperty("kafka.authorization.provider", "CADI_AAF_PROVIDER"));
	}
}
