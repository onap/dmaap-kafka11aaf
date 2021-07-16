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

import java.util.EnumSet;
import java.util.Map;

import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.onap.dmaap.commonauth.kafka.base.authorization.AuthorizationProviderFactory;
import org.onap.dmaap.commonauth.kafka.base.authorization.Cadi3AAFProvider;

import kafka.network.RequestChannel.Session;
import kafka.security.auth.Acl;
import kafka.security.auth.Authorizer;
import kafka.security.auth.Operation;
import kafka.security.auth.Resource;
import scala.collection.immutable.Set;

/**
 * A trivial Kafka Authorizer for use with SSL and AAF
 * Authentication/Authorization.
 * 
 */
public class KafkaCustomAuthorizer implements Authorizer {

	private final String[] adminPermission = new String[3];
	protected static final EnumSet<AclOperation> TOPIC_DESCRIBE_OPERATIONS = EnumSet.of(AclOperation.DESCRIBE_CONFIGS);
	protected static final EnumSet<AclOperation> TOPIC_READ_WRITE_DESCRIBE_OPERATIONS = EnumSet.of(AclOperation.WRITE,
			AclOperation.READ, AclOperation.DESCRIBE_CONFIGS);
	protected static final EnumSet<AclOperation> TOPIC_ADMIN_OPERATIONS = EnumSet.of(AclOperation.ALTER,
			AclOperation.ALTER_CONFIGS, AclOperation.CREATE);
	static final String TOPIC = "Topic";

	private static final Logger logger = LoggerFactory.getLogger(KafkaCustomAuthorizer.class);

	@Override
	public void configure(final Map<String, ?> arg0) {
		// TODO Auto-generate method stub
	}

	@Override
	public void addAcls(final Set<Acl> arg0, final Resource arg1) {
		// TODO Auto-generated method stub

	}

	private String[] getTopicPermission(String topicName, AclOperation aclOperation) {

		String namspace = topicName.substring(0, topicName.lastIndexOf("."));
		String[] permission = new String[3];
		if (TOPIC_READ_WRITE_DESCRIBE_OPERATIONS.contains(aclOperation)) {
			permission[0] = namspace + ".topic";
			String instancePart = (System.getenv("pubSubInstPart") != null) ? System.getenv("pubSubInstPart")
					: ".topic";
			permission[1] = instancePart + topicName;

			if (aclOperation.equals(AclOperation.WRITE)) {
				permission[2] = "pub";
			} else if (aclOperation.equals(AclOperation.READ)) {
				permission[2] = "sub";

			} else if (TOPIC_DESCRIBE_OPERATIONS.contains(aclOperation)) {
				permission[2] = "view";

			}
		} else if (aclOperation.equals(AclOperation.DELETE)) {
			permission = (System.getProperty("msgRtr.topicfactory.aaf") + namspace + "|destroy").split("\\|");

		} else if (TOPIC_ADMIN_OPERATIONS.contains(aclOperation)) {
			permission = (System.getProperty("msgRtr.topicfactory.aaf") + namspace + "|create").split("\\|");
		}

		return permission;
	}

	private String[] getAdminPermission() {

		if (adminPermission[0] == null) {
			adminPermission[0] = System.getProperty("namespace") + ".kafka.access";
			adminPermission[1] = "*";
			adminPermission[2] = "*";
		}

		return adminPermission;
	}

	private String[] getPermission(AclOperation aclOperation, String resource, String topicName) {
		String[] permission = new String[3];
		switch (aclOperation) {

		case ALTER:
		case ALTER_CONFIGS:
		case CREATE:
		case DELETE:
			if (resource.equals(TOPIC)) {
				permission = getTopicPermission(topicName, aclOperation);
			} else if (resource.equals("Cluster")) {
				permission = getAdminPermission();
			}
			break;
		case DESCRIBE_CONFIGS:
		case READ:
		case WRITE:
			if (resource.equals(TOPIC)) {
				permission = getTopicPermission(topicName, aclOperation);
			}
			break;
		case IDEMPOTENT_WRITE:
			if (resource.equals("Cluster")) {
				permission = getAdminPermission();
			}
			break;
		default:
			break;

		}
		return permission;

	}

	@Override
	public boolean authorize(final Session arg0, final Operation arg1, final Resource arg2) {
		if (arg0.principal() == null) {
			return false;
		}

		String fullName = arg0.principal().getName();
		fullName = fullName != null ? fullName.trim() : fullName;
		String topicName = null;
		String[] permission;

		String resource = arg2.resourceType().name();

		if (resource.equals(TOPIC)) {
			topicName = arg2.name();
		}

		if (fullName != null && fullName.equals(Cadi3AAFProvider.getKafkaUsername())) {
			return true;
		}

		if ((!Cadi3AAFProvider.isCadiEnabled())||(null != topicName && !topicName.startsWith("org.onap"))) {
			return true;
		}

		permission = getPermission(arg1.toJava(), resource, topicName);

		if (permission[0] != null) {
			return !checkPermissions(fullName, topicName, permission);
		}
		return true;
	}

	private boolean checkPermissions(String fullName, String topicName, String[] permission) {
		try {

			if (null != topicName) {
				boolean hasResp = AuthorizationProviderFactory.getProviderFactory().getProvider()
					.hasPermission(fullName, permission[0], permission[1], permission[2]);
				if (hasResp) {
					logger.info("Successful Authorization for {} on {} for {} | {} | {}", fullName, topicName,
						permission[0], permission[1], permission[2]);
				}
				if (!hasResp) {
					logger.info("{} is not allowed in {} | {} | {}", fullName, permission[0], permission[1],
						permission[2]);
					return true;
				}
			}
		} catch (final Exception e) {
			return true;
		}
		return false;
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
