/*
 *
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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *
 */

package org.symphonyoss.samples;

import org.junit.Test;
import org.symphonyoss.Utils;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.SymphonyClientConfig;
import org.symphonyoss.client.SymphonyClientFactory;
import org.symphonyoss.client.exceptions.*;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class EchoBotIT {

    private static final long TIMEOUT_MS = 10000;
    private static String TEST_MESSAGE = "Testing the EchoBot";

    @Test
    public void sendAndReceiveEcho() throws InitException, NetworkException, MessagesException, UsersClientException, StreamsException {
        //Creating and running the EchoBot
        EchoBot echoBot = new EchoBot();

        // Getting SJC for the sender bot
        SymphonyClient senderBot = SymphonyClientFactory.getClient(SymphonyClientFactory.TYPE.BASIC);
        SymphonyClientConfig senderConfig = new SymphonyClientConfig();
        senderBot.init(senderConfig);

        // Getting SJC for the receiver bot
        SymphonyClient receiverBot = SymphonyClientFactory.getClient(SymphonyClientFactory.TYPE.BASIC);
        if (new File("symphony.properties.it").exists()) {
            System.setProperty("symphony.config.file", "symphony.properties.it");
        } else {
            //Setting up properties for Travis CI environment variable setup
            System.setProperty("user.cert.file", System.getenv("SENDER_USER_CERT_FILE"));
            System.setProperty("user.cert.password", System.getenv("SENDER_USER_CERT_PASSWORD"));
            System.setProperty("user.email", System.getenv("SENDER_USER_EMAIL"));
        }
        SymphonyClientConfig receiverConfig = new SymphonyClientConfig();
        receiverBot.init(receiverConfig);

        //Sender user creates a chat and adds the receiver
        Chat chat = createChat(senderBot, receiverBot);

        //A MessageMatcher will listen to the responses on the chat just created
        MessageMatcher messageMatcher = new MessageMatcher(TEST_MESSAGE);
        chat.addListener(messageMatcher);

        //The sender sends a message to the chat
        Utils.sendMessage(senderBot, chat, TEST_MESSAGE);

        //We ask the MessageMatcher if something have arrived every half second, until timeout hits
        waitForMessage(messageMatcher, TIMEOUT_MS);

        //We expect the MessageMatcher to have found the match
        assertTrue(messageMatcher.hasMatched());
    }

    private Chat createChat(SymphonyClient senderClient, SymphonyClient receiverClient) throws
            StreamsException,
            UsersClientException {
        Chat chat = new Chat();
        chat.setLocalUser(senderClient.getLocalUser());
        Set<SymUser> remoteUsers = new HashSet<>();

        //You need to add both users because you are using the same chat object for both sender and receiver.
        remoteUsers.add(receiverClient.getLocalUser());
        remoteUsers.add(senderClient.getLocalUser());
        chat.setRemoteUsers(remoteUsers);
        chat.setStream(senderClient.getStreamsClient().getStream(remoteUsers));

        receiverClient.getChatService().addChat(chat);
        return chat;
    }

    private class MessageMatcher implements ChatListener {
        private String messageTest;
        private boolean matched = false;

        @Override
        public void onChatMessage(SymMessage symMessage) {

            //PresentationML now, so switched to getMessageText().trim()
            if (symMessage.getMessageText().trim().equals(messageTest)) {
                this.matched = true;
            }
        }

        public MessageMatcher(String messageTest) {
            this.messageTest = messageTest;
        }

        public boolean hasMatched() {
            return matched;
        }
    }

    public void waitForMessage(MessageMatcher messageMatcher, long timeout) {
        long endTimeMillis = System.currentTimeMillis() + timeout;
        while (true) {
            if (System.currentTimeMillis() > endTimeMillis || messageMatcher.hasMatched()) {
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException t) {
            }
        }
    }
}