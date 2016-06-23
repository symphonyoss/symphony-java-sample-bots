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

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.SymphonyClientFactory;
import org.symphonyoss.client.model.AttribTypes;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.model.NodeTypes;
import org.symphonyoss.client.model.SymAuth;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.client.services.PresenceListener;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.clients.AuthorizationClient;
import org.symphonyoss.symphony.pod.model.Stream;
import org.symphonyoss.symphony.pod.model.User;
import org.symphonyoss.symphony.pod.model.UserPresence;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleBot implements ChatListener, ChatServiceListener {

    private final Logger logger = LoggerFactory.getLogger(SimpleBot.class);
    private SymphonyClient symClient;

    public SimpleBot() {
        init();
    }

    public static void main(String[] args) {
        System.out.println("SimpleBot starting...");
        new SimpleBot();
    }

    public void init() {
        try {

            symClient = SymphonyClientFactory.getClient(SymphonyClientFactory.TYPE.BASIC);

            logger.debug("{} {}", System.getProperty("sessionauth.url"),
                    System.getProperty("keyauth.url"));


            AuthorizationClient authClient = new AuthorizationClient(
                    System.getProperty("sessionauth.url"),
                    System.getProperty("keyauth.url"));


            authClient.setKeystores(
                    System.getProperty("truststore.file"),
                    System.getProperty("truststore.password"),
                    System.getProperty("certs.dir") + System.getProperty("bot.user") + ".p12",
                    System.getProperty("keystore.password"));

            SymAuth symAuth = authClient.authenticate();


            symClient.init(
                    symAuth,
                    System.getProperty("bot.user") + "@markit.com",
                    System.getProperty("symphony.agent.agent.url"),
                    System.getProperty("symphony.agent.pod.url")
            );

            symClient.getChatService().registerListener(this);

            MessageSubmission aMessage = new MessageSubmission();
            aMessage.setFormat(MessageSubmission.FormatEnum.TEXT);
            aMessage.setMessage("Hello master, I'm alive again....");

            Chat chat = new Chat();
            chat.setLocalUser(symClient.getLocalUser());
            // Set<User> remoteUsers = new HashSet<>();
            // remoteUsers.add(symClient.getUsersClient().getUserFromEmail("call.home.user@domain.com"));
            // chat.setRemoteUsers(remoteUsers);
            chat.registerListener(this);
            // chat.setStream(symClient.getStreamsClient().getStream(remoteUsers));
            symClient.getChatService().addChat(chat);
            symClient.getMessageService().sendMessage(chat, aMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onChatMessage(Message message) {
        if (message == null)
            return;

        logger.debug("TS: {}\nFrom ID: {}\nMessage: {}\nMessage Type: {}",
                message.getTimestamp(),
                message.getFromUserId(),
                message.getMessage(),
                message.getMessageType());

        MessageSubmission aMessage = new MessageSubmission();
        aMessage.setFormat(MessageSubmission.FormatEnum.MESSAGEML);
        aMessage.setMessage("<messageML>Hello!</messageML>");

        Stream stream = new Stream();
        stream.setId(message.getStream());
        try {
            symClient.getMessagesClient().sendMessage(stream, aMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onNewChat(Chat chat) {
        chat.registerListener(this);
        logger.debug("New chat session detected on stream {} with {}", chat.getStream().getId(), chat.getRemoteUsers());
    }

    public void onRemovedChat(Chat chat) {
    }
}
