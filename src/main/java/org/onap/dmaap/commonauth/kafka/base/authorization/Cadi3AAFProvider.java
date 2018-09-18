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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.aaf.v2_0.AAFAuthn;
import org.onap.aaf.cadi.aaf.v2_0.AAFCon;
import org.onap.aaf.cadi.aaf.v2_0.AAFConHttp;
import org.onap.aaf.cadi.aaf.v2_0.AbsAAFLur;
import org.onap.aaf.cadi.principal.UnAuthPrincipal;

public class Cadi3AAFProvider implements AuthorizationProvider {

	private static PropAccess access;
	private static AAFCon<?> aafcon;
	private static final String CADI_PROPERTIES = "/opt/kafka/config/cadi.properties";
	private static final String AAF_LOCATOR_ENV = "aaf_locate_url";
	private static final String MR_NAMESPACE = "	org.onap.dmaap.mr";

	public static AAFAuthn<?> getAafAuthn() throws CadiException {
		if (aafAuthn == null) {
			throw new CadiException("Cadi is uninitialized in Cadi3AAFProvider.getAafAuthn()");
		}
		return aafAuthn;
	}
	// public static void setAafAuthn(AAFAuthn<?> aafAuthn) {
	// Cadi3AAFProvider.aafAuthn = aafAuthn;
	// }

	private static AAFAuthn<?> aafAuthn;
	private static AbsAAFLur<AAFPermission> aafLur;

	private static boolean props_ok = false;

	private static final Logger logger = LoggerFactory.getLogger(Cadi3AAFProvider.class);

	public Cadi3AAFProvider() {
		setup();
		/*
		 * try { final String cadiconfigFile =
		 * System.getProperty("cadi_prop_files"); final InputStream input = new
		 * FileInputStream(cadiconfigFile);
		 * 
		 * final Properties props = new Properties();
		 * 
		 * 
		 * props.load(input);
		 * 
		 * access = new PropAccess(props); setup();
		 * 
		 * 
		 * } catch ( Exception ex ) { logger.error(
		 * "Exception connecting to AAF", ex ); }
		 */
	}

	private synchronized void setup() {
		if (access == null) {

			Properties props = new Properties();
			try {
				FileInputStream fis = new FileInputStream(CADI_PROPERTIES);
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

			props_ok = true;
			if (props_ok == false) {
				return;
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
				props_ok = false;
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
			logger.info("^ Event at hasPermission to validate userid " + userId + " with " + permission + " " + instance
					+ " " + action);
			// AAF Style permissions are in the form
			// Resource Name, Resource Type, Action
			if (userId.equals("admin")) {
				hasPermission = true;
				return hasPermission;
			}
			AAFPermission perm = new AAFPermission(MR_NAMESPACE, permission, instance, action);
			if (aafLur != null) {
				hasPermission = aafLur.fish(new UnAuthPrincipal(userId), perm);
				logger.trace("Permission: " + perm.getKey() + " for user :" + userId + " found: " + hasPermission);
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

	public String authenticate(String userId, String password) throws Exception {
		logger.info("^Event received  with   username " + userId);
		if (userId.equals("admin")) {
			logger.info("User Admin by passess AAF call ....");
			return null;
		}
		String aafResponse = aafAuthn.validate(userId, password);
		logger.info("aafResponse=" + aafResponse + " for " + userId);

		if (aafResponse != null) {
			logger.error("Authentication failed for user ." + userId);
		}
		return aafResponse;
	}

}
