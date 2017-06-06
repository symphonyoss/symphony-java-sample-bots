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

package org.symphonyoss.examples.connections;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.SymphonyClientFactory;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.model.SymAuth;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.client.services.ConnectionsListener;
import org.symphonyoss.client.services.ConnectionsService;
import org.symphonyoss.exceptions.*;
import org.symphonyoss.symphony.clients.AuthorizationClient;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;
import org.symphonyoss.symphony.clients.model.SymUserConnection;
import org.symphonyoss.symphony.pod.model.Stream;

import java.util.HashSet;
import java.util.Set;


/**
 * Simple example of the ChatService.
 * <p>
 * It will send a message to a call.home.user and listen/create new Chat sessions.
 * <p>
 * <p>
 * <p>
 * REQUIRED VM Arguments or System Properties:
 * <p>
 * -Dsessionauth.url=https://pod_fqdn:port/sessionauth
 * -Dkeyauth.url=https://pod_fqdn:port/keyauth
 * -Dsymphony.agent.pod.url=https://agent_fqdn:port/pod
 * -Dsymphony.agent.agent.url=https://agent_fqdn:port/agent
 * -Dcerts.dir=/dev/certs/
 * -Dkeystore.password=(Pass)
 * -Dtruststore.file=/dev/certs/server.truststore
 * -Dtruststore.password=(Pass)
 * -Dbot.user=bot.user1
 * -Dbot.domain=@domain.com
 * -Duser.call.home=frank.tarsillo@markit.com
 *
 * @author Frank Tarsillo
 */
//NOSONAR
public class ChatWithAutoAcceptConnectionsExample implements ChatListener, ChatServiceListener, ConnectionsListener {


    private final Logger logger = LoggerFactory.getLogger(ChatWithAutoAcceptConnectionsExample.class);
    private SymphonyClient symClient;


    public ChatWithAutoAcceptConnectionsExample() {

        init();


    }

    public static void main(String[] args) {

        new ChatWithAutoAcceptConnectionsExample();

    }

    public void init() {

        logger.info("Connections example starting...");
        try {


            logger.debug("{} {}", System.getProperty("sessionauth.url"),
                    System.getProperty("keyauth.url"));


            //Create an initialized client
            symClient = SymphonyClientFactory.getClient(
                    SymphonyClientFactory.TYPE.BASIC,
                    System.getProperty("bot.user") + System.getProperty("bot.domain"), //bot email
                    System.getProperty("certs.dir") + System.getProperty("bot.user") + ".p12", //bot cert
                    System.getProperty("keystore.password"), //bot cert/keystore pass
                    System.getProperty("truststore.file"), //truststore file
                    System.getProperty("truststore.password"));  //truststore password


            //Will notify the bot of new Chat conversations.
            symClient.getChatService().addListener(this);

            //Init connection service.
            ConnectionsService connectionsService = new ConnectionsService(symClient);

            //Optional to auto accept connections.
            connectionsService.setAutoAccept(true);


            //A message to send when the BOT comes online.
            SymMessage aMessage = new SymMessage();
            aMessage.setFormat(SymMessage.Format.MESSAGEML);
            aMessage.setMessage("<messageML>Hello <b>master</b>, I'm alive again....</messageML>");


            //Creates a Chat session with that will receive the online message.
            Chat chat = new Chat();
            chat.setLocalUser(symClient.getLocalUser());
            Set<SymUser> remoteUsers = new HashSet<>();
            remoteUsers.add(symClient.getUsersClient().getUserFromEmail(System.getProperty("user.call.home")));
            chat.setRemoteUsers(remoteUsers);
            chat.addListener(this);
            chat.setStream(symClient.getStreamsClient().getStream(remoteUsers));

            //Add the chat to the chat service, in case the "master" continues the conversation.
            symClient.getChatService().addChat(chat);


            //Send a message to the master user.
            symClient.getMessageService().sendMessage(chat, aMessage);


        } catch (UserNotFoundException ue) {

            logger.error("Failed to find user....", ue);

        } catch (UsersClientException  | MessagesException | StreamsException e) {
            logger.error("error", e);
        }

    }


    //Chat sessions callback method.
    @Override
    public void onChatMessage(SymMessage message) {
        if (message == null)
            return;

        logger.debug("TS: {}\nFrom ID: {}\nSymMessage: {}\nSymMessage Type: {}",
                message.getTimestamp(),
                message.getFromUserId(),
                message.getMessage(),
                message.getMessageType());


        if (message.getMessage().contains("life")) {
            message.setMessage("The meaning of life is 42!..silly");

        } else {
            message.setMessage("ECHO...boring..ECHO...ask me the meaning of life..");
        }


        Stream stream = new Stream();
        stream.setId(message.getStreamId());

        try {
            symClient.getMessagesClient().sendMessage(stream, message);
        } catch (MessagesException e) {
            logger.error("Failed to send message", e);
        }


    }

    @Override
    public void onNewChat(Chat chat) {

        chat.addListener(this);

        logger.debug("New chat session detected on stream {} with {}", chat.getStream().getId(), chat.getRemoteUsers());
    }

    @Override
    public void onRemovedChat(Chat chat) {

    }

    @Override
    public void onConnectionNotification(SymUserConnection userConnection) {
        if (userConnection.getStatus().equals(SymUserConnection.StatusEnum.PENDING_INCOMING)) {
            logger.info("Received new connection request from {}", userConnection.getUserId());
        } else if (userConnection.getStatus().equals(SymUserConnection.StatusEnum.ACCEPTED)) {
            logger.info("Accepted connection request from {}", userConnection.getUserId());

        }
    }
}
