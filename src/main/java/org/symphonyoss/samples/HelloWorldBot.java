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

package org.symphonyoss.samples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.Utils;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.SymphonyClientConfig;
import org.symphonyoss.client.SymphonyClientConfigID;
import org.symphonyoss.client.exceptions.SymException;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;

import java.util.HashSet;
import java.util.Set;

public class HelloWorldBot {
    private final static Logger log = LoggerFactory.getLogger(HelloWorldBot.class);

    private SymphonyClientConfig config = new SymphonyClientConfig(true);
    private SymphonyClient symClient;
    private Chat chat;

    public static void main(String[] args) throws Exception {
        new HelloWorldBot();
        System.exit(0);
    }

    public HelloWorldBot() throws SymException {
        // Get SJC instance
        this.symClient = Utils.getSymphonyClient(config);

        // Init chat
        this.chat = new Chat();
        chat.setLocalUser(symClient.getLocalUser());

        // Add users to chat
        Set<SymUser> remoteUsers = new HashSet<>();
        remoteUsers.add(symClient.getUsersClient().getUserFromEmail(config.get(SymphonyClientConfigID.RECEIVER_EMAIL)));
        chat.setRemoteUsers(remoteUsers);
        chat.setStream(symClient.getStreamsClient().getStream(remoteUsers));

        // Send a message
        String message = "Hello " + config.get(SymphonyClientConfigID.RECEIVER_EMAIL) + "!";
        Utils.sendMessage(symClient, chat, message);
    }
}
