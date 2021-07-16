/******************************************************************************
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;
import javax.security.sasl.SaslServerFactory;
import org.apache.kafka.common.errors.SaslAuthenticationException;
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
    private String authorizationId;
    private static final String AUTH_EXC_NOT_COMPLETE = "Authentication exchange has not completed";


    /**
     * @throws SaslAuthenticationException if username/password combination is invalid or if the requested
     *         authorization id is not the same as username.
     * <p>
     * <b>Note:</b> This method may throw {@link SaslAuthenticationException} to provide custom error messages
     * to clients. But care should be taken to avoid including any information in the exception message that
     * should not be leaked to unauthenticated clients. It may be safer to throw {@link SaslException} in
     * some cases so that a standard error message is returned to clients.
     * </p>
     */
    @Override
    public byte[] evaluateResponse(byte[] responseBytes) throws SaslAuthenticationException {
        /*
         * Message format (from https://tools.ietf.org/html/rfc4616):
         *
         * message   = [authzid] UTF8NUL authcid UTF8NUL passwd
         * authcid   = 1*SAFE ; MUST accept up to 255 octets
         * authzid   = 1*SAFE ; MUST accept up to 255 octets
         * passwd    = 1*SAFE ; MUST accept up to 255 octets
         * UTF8NUL   = %x00 ; UTF-8 encoded NUL character
         *
         * SAFE      = UTF1 / UTF2 / UTF3 / UTF4
         *                ;; any UTF-8 encoded Unicode character except NUL
         */
        String response = new String(responseBytes, StandardCharsets.UTF_8);
        List<String> tokens = extractTokens(response);
        String authorizationIdFromClient = tokens.get(0);
        String username = tokens.get(1);
        String password = tokens.get(2);

        if (username.isEmpty()) {
            throw new SaslAuthenticationException("Authentication failed: username not specified");
        }
        if (password.isEmpty()) {
            throw new SaslAuthenticationException("Authentication failed: password not specified");
        }

        String aafResponse = "Not Verified";
		try {
			aafResponse = AuthorizationProviderFactory.getProviderFactory().getProvider().authenticate(username,
					password);
		} catch (Exception ignored) {
            throw new SaslAuthenticationException("Authentication failed: " + aafResponse + " User " + username);
		}
		if (null != aafResponse) {
			throw new SaslAuthenticationException("Authentication failed: " + aafResponse + " User " + username);
		}

        if (!authorizationIdFromClient.isEmpty() && !authorizationIdFromClient.equals(username))
            throw new SaslAuthenticationException("Authentication failed: Client requested an authorization id that is different from username");

        this.authorizationId = username;

        complete = true;
        return new byte[0];
    }

    private List<String> extractTokens(String string) {
        List<String> tokens = new ArrayList<>();
        int startIndex = 0;
        for (int i = 0; i < 4; ++i) {
            int endIndex = string.indexOf("\u0000", startIndex);
            if (endIndex == -1) {
                tokens.add(string.substring(startIndex));
                break;
            }
            tokens.add(string.substring(startIndex, endIndex));
            startIndex = endIndex + 1;
        }

        if (tokens.size() != 3)
            throw new SaslAuthenticationException("Invalid SASL/PLAIN response: expected 3 tokens, got " +
                tokens.size());

        return tokens;
    }

    @Override
    public String getAuthorizationID() {
        if (!complete)
            throw new IllegalStateException(AUTH_EXC_NOT_COMPLETE);
        return authorizationId;
    }

    @Override
    public String getMechanismName() {
        return PLAIN_MECHANISM;
    }

    @Override
    public Object getNegotiatedProperty(String propName) {
        if (!complete)
            throw new IllegalStateException(AUTH_EXC_NOT_COMPLETE);
        return null;
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

    @Override
    public byte[] unwrap(byte[] incoming, int offset, int len) {
        if (!complete)
            throw new IllegalStateException(AUTH_EXC_NOT_COMPLETE);
        return Arrays.copyOfRange(incoming, offset, offset + len);
    }

    @Override
    public byte[] wrap(byte[] outgoing, int offset, int len) {
        if (!complete)
            throw new IllegalStateException(AUTH_EXC_NOT_COMPLETE);
        return Arrays.copyOfRange(outgoing, offset, offset + len);
    }

    @Override
    public void dispose() {
        // TODO Auto-generate method stub
    }

    public static class PlainSaslServerFactory1 implements SaslServerFactory {

        @Override
        public SaslServer createSaslServer(String mechanism, String protocol, String serverName, Map<String, ?> props, CallbackHandler cbh)
            throws SaslException {

            if (!PLAIN_MECHANISM.equals(mechanism))
                throw new SaslException(String.format("Mechanism '%s' is not supported. Only PLAIN is supported.", mechanism));

            return new PlainSaslServer1();
        }

        @Override
        public String[] getMechanismNames(Map<String, ?> props) {
            if (props == null) return new String[]{PLAIN_MECHANISM};
            String noPlainText = (String) props.get(Sasl.POLICY_NOPLAINTEXT);
            if ("true".equals(noPlainText))
                return new String[]{};
            else
                return new String[]{PLAIN_MECHANISM};
        }
    }
}

