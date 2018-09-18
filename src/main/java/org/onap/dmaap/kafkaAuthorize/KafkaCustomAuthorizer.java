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

import java.util.Map;

import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.onap.aaf.cadi.PropAccess;
import org.onap.dmaap.commonauth.kafka.base.authorization.AuthorizationProviderFactory;
import kafka.network.RequestChannel.Session;
import kafka.security.auth.Acl;
import kafka.security.auth.Authorizer;
import kafka.security.auth.Operation;
import kafka.security.auth.Resource;
import scala.collection.immutable.Set;

/**
 * A trivial Kafka Authorizer for use with SSL AT&T AAF
 * Authentication/Authorization.
 * 
 */
public class KafkaCustomAuthorizer implements Authorizer {
	private PropAccess access;
	private static final Logger logger = LoggerFactory.getLogger(KafkaCustomAuthorizer.class);

	// I'm assuming this is called BEFORE any usage...
	@Override
	public void configure(final Map<String, ?> arg0) {
		// TODO Auto-generate method stub
	}

	@Override
	public void addAcls(final Set<Acl> arg0, final Resource arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean authorize(final Session arg0, final Operation arg1, final Resource arg2) {
		if (arg0.principal() == null) {
			return false;
		}

		String fullName = arg0.principal().getName();
		fullName = fullName != null ? fullName.trim() : fullName;
		String topicName = null;
		String namspace = null;
		String ins = null;
		String type = null;
		String action = null;

		String kafkaactivity = arg1.name();

		if (kafkaactivity.equals("Read")) {
			action = "sub";
		} else if (kafkaactivity.equals("Write")) {
			action = "pub";
		} else if (kafkaactivity.equals("Describe")) {
			return true;
		}
		if (arg2.resourceType().name().equals("Topic")) {
			topicName = arg2.name();
		} else {
			return true;
		}

		boolean hasPermission = false;
		try {

			if (null != topicName && topicName.indexOf(".") > 0) {
				namspace = topicName.substring(0, topicName.lastIndexOf("."));
				ins = namspace + ".mr.topic";
				type = ":topic." + topicName;
				logger.info("^Event Received for topic " + topicName + " , User " + fullName + " , action = " + action);
			}

			if (fullName.equals("admin")) {
				return true;
			}

			if (null != topicName) {
				boolean hasResp = AuthorizationProviderFactory.getProviderFactory().getProvider()
						.hasPermission(fullName, ins, type, action);
				if (hasResp) {
					logger.info("Successful Authorization for " + fullName + " on " + topicName + " for " + ins + "|"
							+ type + "|" + action);
				}
				if (!hasResp) {
					logger.info(fullName + " is not allowed in " + ins + "|" + type + "|" + action);
					throw new Exception(fullName + " is not allowed in " + ins + "|" + type + "|" + action);
				}
			}
		} catch (final Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public scala.collection.immutable.Map<Resource, Set<Acl>> getAcls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public scala.collection.immutable.Map<Resource, Set<Acl>> getAcls(final KafkaPrincipal arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeAcls(final Resource arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAcls(final Set<Acl> arg0, final Resource arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public Set<Acl> getAcls(Resource arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
