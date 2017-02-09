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
package org.symphonyoss.simplebot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.SymphonyClientFactory;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.model.SymAuth;
import org.symphonyoss.exceptions.*;
import org.symphonyoss.symphony.clients.AuthorizationClient;
import org.symphonyoss.symphony.clients.model.SymMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Utils {

    private final static Logger log = LoggerFactory.getLogger(Utils.class);

    public SymphonyClient getSymphonyClient(Map<String,String> initParams) {
        SymphonyClient symClient = SymphonyClientFactory.getClient(SymphonyClientFactory.TYPE.BASIC);

        log.debug("Instanciating Symphony client: Session auth '{}' and Keyauth '{}'", System.getProperty("sessionauth.url"),
                System.getProperty("keyauth.url"));

        AuthorizationClient authClient = new AuthorizationClient(
                initParams.get("sessionauth.url"),
                initParams.get("keyauth.url"));

        authClient.setKeystores(
                initParams.get("truststore.file"),
                initParams.get("truststore.password"),
                initParams.get("bot.user.cert.file"),
                initParams.get("bot.user.cert.password"));

        SymAuth symAuth = null;
        try {
            symAuth = authClient.authenticate();

            symClient.init(
                    symAuth,
                    initParams.get("bot.user.email"),
                    initParams.get("agent.url"),
                    initParams.get("pod.url")
            );

            return symClient;
        } catch (AuthorizationException e) {
            throw new RuntimeException(e);
        } catch (InitException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String,String> readInitParams(Set<String> initParamNames) {

        // TODO - remove this!
        log.debug("Printing out init params...");

        Map<String,String> initParams = new HashMap<>();
        for (String initParam : initParamNames) {
            String initParamValue = System.getProperty(initParam);

            if (isEmpty(initParamValue)) {
                throw new IllegalArgumentException("Cannot find required property; make sure you're using -D" + initParam + " to run HelloWorldBot");
            } else {
                initParams.put(initParam, initParamValue);
            }
            
            // Logging debug statements for each param
            // TODO - remove this!
            if (initParam.contains("password")) {
              initParamValue = "not null";
            }
            log.debug("{}={}", initParam,initParamValue);
        }
        return initParams;
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public void sendMessage(SymphonyClient client, Chat chat, String message, SymMessage.Format messageFormat)
            throws MessagesException
    {
        SymMessage messageSubmission = new SymMessage();
        messageSubmission.setFormat(messageFormat);
        messageSubmission.setMessage(message);

        client.getMessageService().sendMessage(chat, messageSubmission);
    }
}
