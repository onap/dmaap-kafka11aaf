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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.aaf.v2_0.AAFAuthn;
import org.onap.aaf.cadi.aaf.v2_0.AAFCon;
import org.onap.aaf.cadi.aaf.v2_0.AAFConHttp;
import org.onap.aaf.cadi.aaf.v2_0.AbsAAFLur;
import org.onap.aaf.cadi.principal.UnAuthPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cadi3AAFProvider implements AuthorizationProvider {

	private static PropAccess access;
	private static AAFCon<?> aafcon;
	private static final String CADI_PROPERTIES = "/etc/kafka/data/cadi.properties";
	private static final String AAF_LOCATOR_ENV = "aaf_locate_url";
	private static String apiKey = null;
	private static String kafkaUsername = null;
	private static AAFAuthn<?> aafAuthn;
	private static AbsAAFLur<AAFPermission> aafLur;
	private static boolean enableCadi = false;
	private static final String ENABLE_CADI = "enableCadi";
	private static final Logger logger = LoggerFactory.getLogger(Cadi3AAFProvider.class);

	static {
		if (System.getProperty(ENABLE_CADI) != null) {
			if (System.getProperty(ENABLE_CADI).equals("true")) {
				enableCadi = true;
			}
		}
         else{
		if (System.getenv(ENABLE_CADI) != null && System.getenv(ENABLE_CADI).equals("true")) {
			enableCadi = true;
		}
         }
		Configuration config = Configuration.getConfiguration();
		try {
			if (config == null) {
				logger.error("CRITICAL ERROR|Check java.security.auth.login.config VM argument|");
			} else {
				// read the section for KafkaServer
				AppConfigurationEntry[] entries = config.getAppConfigurationEntry("KafkaServer");
				if (entries == null) {
					logger.error(
							"CRITICAL ERROR|Check config contents passed in java.security.auth.login.config VM argument|");
					kafkaUsername = "kafkaUsername";
					apiKey = "apiKey";

				} else {
					for (AppConfigurationEntry entry : entries) {
						Map<String, ?> optionsMap = entry.getOptions();
						kafkaUsername = (String) optionsMap.get("username");
						apiKey = (String) optionsMap.get("password");
					}
				}
			}
		} catch (Exception e) {
			logger.error("CRITICAL ERROR: JAAS configuration incorrectly set: {}", e.getMessage());
		}
	}

	public static String getKafkaUsername() {
		return kafkaUsername;
	}

	public static boolean isCadiEnabled() {

		return enableCadi;
	}

	public Cadi3AAFProvider() {
		setup();
	}

	private synchronized void setup() {
		if (access == null) {

			Properties props = new Properties();
			FileInputStream fis;
			try {
				if (System.getProperty("CADI_PROPERTIES") != null) {
					fis = new FileInputStream(System.getProperty("CADI_PROPERTIES"));
				} else {
					fis = new FileInputStream(CADI_PROPERTIES);
				}
				try {
					props.load(fis);
					if (System.getenv(AAF_LOCATOR_ENV) != null)
						props.setProperty(AAF_LOCATOR_ENV, System.getenv(AAF_LOCATOR_ENV));
					access = new PropAccess(props);
				} finally {
					fis.close();
				}
			} catch (IOException e) {
				logger.error("Unable to load " + CADI_PROPERTIES);
				logger.error("Error", e);
			}
		}

		if (aafAuthn == null) {
			try {
				aafcon = new AAFConHttp(access);
				aafAuthn = aafcon.newAuthn();
				aafLur = aafcon.newLur(aafAuthn);
			} catch (final Exception e) {
				aafAuthn = null;
				if (access != null)
					access.log(e, "Failed to initialize AAF");
			}
		}

	}

	/**
	 * Checks if a user has a particular permission
	 * <p/>
	 * Returns true if the permission in found
	 */
	public boolean hasPermission(String userId, String permission, String instance, String action) {
		boolean hasPermission = false;
		try {
			logger.info("^ Event at hasPermission to validate userid {} with {} {} {}", userId, permission, instance, action);
			// AAF Style permissions are in the form
			// Resource Name, Resource Type, Action
			if (userId.equals("admin")) {
				hasPermission = true;
				return hasPermission;
			}
			AAFPermission perm = new AAFPermission(null, permission, instance, action);
			if (aafLur != null) {
				hasPermission = aafLur.fish(new UnAuthPrincipal(userId), perm);
				logger.trace("Permission: {}  for user : {}  found: {}" , perm.getKey(), userId, hasPermission);
			} else {
				logger.error("AAF client not initialized. Not able to find permissions.");
			}
		} catch (Exception e) {
			logger.error("AAF client not initialized", e);
		}
		return hasPermission;
	}

	public String getId() {
		return "CADI_AAF_PROVIDER";
	}

	public String authenticate(String userId, String password) throws IOException {

		logger.info("^Event received  with username {}", userId);

		if (!enableCadi) {
			return null;
		} else {
			if (userId.equals(kafkaUsername)) {
				if (password.equals(apiKey)) {
					logger.info("by passes the authentication for the admin {}", kafkaUsername);
					return null;
				} else {
					String errorMessage = "Authentication failed for user " + kafkaUsername;
					logger.error(errorMessage);
					return errorMessage;
				}

			}

			String aafResponse = aafAuthn.validate(userId, password);
			logger.info("aafResponse = {} for {}", aafResponse, userId);

			if (aafResponse != null) {
				logger.error("Authentication failed for user {}", userId);
			}
			return aafResponse;
		}

	}

}
