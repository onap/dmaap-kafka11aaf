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
package org.onap.dmaap.kafkaAuthorize;

import java.security.Provider;
import java.security.Security;

import org.onap.dmaap.kafkaAuthorize.PlainSaslServer1.PlainSaslServerFactory1;

public class PlainSaslServerProvider1 extends Provider {

	private static final long serialVersionUID = 1L;

	protected PlainSaslServerProvider1() {
		super("Simple SASL/PLAIN Server Provider", 1.0, "Simple SASL/PLAIN Server Provider for Kafka");
		super.put("SaslServerFactory." + PlainSaslServer1.PLAIN_MECHANISM, PlainSaslServerFactory1.class.getName());
	}

	public static void initialize() {
		Security.insertProviderAt(new PlainSaslServerProvider1(),1);
	}
}

