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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;

import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;
import javax.security.sasl.SaslServerFactory;

import org.apache.kafka.common.security.JaasContext;
import org.apache.kafka.common.security.authenticator.SaslServerCallbackHandler;

import org.onap.dmaap.commonauth.kafka.base.authorization.AuthorizationProviderFactory;

/**
 * Simple SaslServer implementation for SASL/PLAIN. In order to make this
 * implementation fully pluggable, authentication of username/password is fully
 * contained within the server implementation.
 * <p>
 * Valid users with passwords are specified in the Jaas configuration file. Each
 * user is specified with user_<username> as key and <password> as value. This
 * is consistent with Zookeeper Digest-MD5 implementation.
 * <p>
 * To avoid storing clear passwords on disk or to integrate with external
 * authentication servers in production systems, this module can be replaced
 * with a different implementation.
 *
 */
public class PlainSaslServer1 implements SaslServer {

	public static final String PLAIN_MECHANISM = "PLAIN";

	private boolean complete;
	private String authorizationID;


	@Override
	public byte[] evaluateResponse(byte[] response) throws SaslException {
		/*
		 * Message format (from https://tools.ietf.org/html/rfc4616):
		 *
		 * message = [authzid] UTF8NUL authcid UTF8NUL passwd authcid = 1*SAFE ;
		 * MUST accept up to 255 octets authzid = 1*SAFE ; MUST accept up to 255
		 * octets passwd = 1*SAFE ; MUST accept up to 255 octets UTF8NUL = %x00
		 * ; UTF-8 encoded NUL character
		 *
		 * SAFE = UTF1 / UTF2 / UTF3 / UTF4 ;; any UTF-8 encoded Unicode
		 * character except NUL
		 */

		String[] tokens;
		try {
			tokens = new String(response, "UTF-8").split("\u0000");
		} catch (UnsupportedEncodingException e) {
			throw new SaslException("UTF-8 encoding not supported", e);
		}
		if (tokens.length != 3)
			throw new SaslException("Invalid SASL/PLAIN response: expected 3 tokens, got " + tokens.length);
		authorizationID = tokens[0];
		String username = tokens[1];
		String password = tokens[2];

		if (username.isEmpty()) {
			throw new SaslException("Authentication failed: username not specified");
		}
		if (password.isEmpty()) {
			throw new SaslException("Authentication failed: password not specified");
		}
		if (authorizationID.isEmpty())
			authorizationID = username;

		String aafResponse = "Not Verified";
		try {
			aafResponse = AuthorizationProviderFactory.getProviderFactory().getProvider().authenticate(username,
					password);
		} catch (Exception e) {
		}

		if (null != aafResponse) {
			throw new SaslException("Authentication failed: " + aafResponse + " User " + username);
		}

		complete = true;
		return new byte[0];
	}

	@Override
	public String getAuthorizationID() {
		if (!complete)
			throw new IllegalStateException("Authentication exchange has not completed");
		return authorizationID;
	}

	@Override
	public String getMechanismName() {
		return PLAIN_MECHANISM;
	}

	@Override
	public Object getNegotiatedProperty(String propName) {
		if (!complete)
			throw new IllegalStateException("Authentication exchange has not completed");
		return null;
	}

	@Override
	public boolean isComplete() {
		return complete;
	}

	@Override
	public byte[] unwrap(byte[] incoming, int offset, int len) throws SaslException {
		if (!complete)
			throw new IllegalStateException("Authentication exchange has not completed");
		return Arrays.copyOfRange(incoming, offset, offset + len);
	}

	@Override
	public byte[] wrap(byte[] outgoing, int offset, int len) throws SaslException {
		if (!complete)
			throw new IllegalStateException("Authentication exchange has not completed");
		return Arrays.copyOfRange(outgoing, offset, offset + len);
	}

	@Override
	public void dispose() throws SaslException {
	}

	public static class PlainSaslServerFactory1 implements SaslServerFactory {

		@Override
		public SaslServer createSaslServer(String mechanism, String protocol, String serverName, Map<String, ?> props,
				CallbackHandler cbh) throws SaslException {

			if (!PLAIN_MECHANISM.equals(mechanism))
				throw new SaslException(
						String.format("Mechanism \'%s\' is not supported. Only PLAIN is supported.", mechanism));

			return new PlainSaslServer1();
		}

		@Override
		public String[] getMechanismNames(Map<String, ?> props) {
			if (props == null)
				return new String[] { PLAIN_MECHANISM };
			String noPlainText = (String) props.get(Sasl.POLICY_NOPLAINTEXT);
			if ("true".equals(noPlainText))
				return new String[] {};
			else
				return new String[] { PLAIN_MECHANISM };
		}
	}
}
