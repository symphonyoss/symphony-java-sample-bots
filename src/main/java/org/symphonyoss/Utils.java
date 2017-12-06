/*
 *
 * Copyright 2016 The Symphony Software Foundation
 *
 * Licensed to The Symphony Software Foundation (SSF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.symphonyoss;

import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.SymphonyClientConfig;
import org.symphonyoss.client.SymphonyClientConfigID;
import org.symphonyoss.client.SymphonyClientFactory;
import org.symphonyoss.client.exceptions.AuthenticationException;
import org.symphonyoss.client.exceptions.InitException;
import org.symphonyoss.client.exceptions.MessagesException;
import org.symphonyoss.client.impl.CustomHttpClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.symphony.clients.model.SymMessage;

import javax.ws.rs.client.Client;

import static java.lang.Thread.sleep;

public class Utils {

    public static SymphonyClient getSymphonyClient(SymphonyClientConfig symphonyClientConfig) throws InitException, AuthenticationException {
        SymphonyClient symClient = SymphonyClientFactory.getClient(SymphonyClientFactory.TYPE.BASIC);
        String proxy = symphonyClientConfig.get("proxy.url");

        if (proxy == null) {
            symClient.init(symphonyClientConfig);
            return symClient;
        } else {
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.connectorProvider(new ApacheConnectorProvider());
            clientConfig.property(ClientProperties.PROXY_URI, proxy);

            try {
                Client httpClient = CustomHttpClient.getClient(
                        symphonyClientConfig.get(SymphonyClientConfigID.USER_CERT_FILE),
                        symphonyClientConfig.get(SymphonyClientConfigID.USER_CERT_PASSWORD),
                        symphonyClientConfig.get(SymphonyClientConfigID.TRUSTSTORE_FILE),
                        symphonyClientConfig.get(SymphonyClientConfigID.TRUSTSTORE_PASSWORD),
                        clientConfig);
                symClient.init(httpClient, symphonyClientConfig);
                return symClient;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return symClient;
    }

    public static void sendMessage(SymphonyClient client, Chat chat, String message)
            throws MessagesException {
        SymMessage messageSubmission = new SymMessage();
        messageSubmission.setMessageText(message);

        client.getChatService().addChat(chat);

        client.getMessageService().sendMessage(chat, messageSubmission);
    }

    public static void hang() {
        while (true) {
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
