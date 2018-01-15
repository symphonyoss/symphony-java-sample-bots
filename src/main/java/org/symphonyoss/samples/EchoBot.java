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
import org.symphonyoss.client.exceptions.AuthorizationException;
import org.symphonyoss.client.exceptions.InitException;
import org.symphonyoss.client.exceptions.MessagesException;
import org.symphonyoss.client.exceptions.NetworkException;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.symphony.clients.model.SymMessage;

public class EchoBot
        implements ChatListener, ChatServiceListener {

    private final static Logger log = LoggerFactory.getLogger(EchoBot.class);

    private SymphonyClientConfig config = new SymphonyClientConfig(true);
    private SymphonyClient symClient;

    public static void main(String[] args) throws Exception {
        new EchoBot();
        Utils.hang();
    }

    public EchoBot() throws AuthorizationException, InitException, NetworkException {
        // Get SJC instance and register as listener
        this.symClient = Utils.getSymphonyClient(config);
        this.symClient.getChatService().addListener(this);
    }

    @Override
    public void onChatMessage(SymMessage message) {
        log.debug("on chat message");
        String messageText = message.getMessage();

        try {
            Chat chat = this.symClient.getChatService().getChatByStream(message.getStreamId());
            Utils.sendMessage(this.symClient, chat, messageText);
        } catch (MessagesException e) {
            e.printStackTrace();
            log.error("Error sending message", e);
        }
    }

    @Override
    public void onNewChat(Chat chat) {
        log.debug("on new chat invoked; registering listener, so messages get parsed");
        chat.addListener(this);
    }

    @Override
    public void onRemovedChat(Chat chat) {
        log.debug("on removed chat invoked; removing EchoBot as chat listener");
        chat.removeListener(this);
    }
}
