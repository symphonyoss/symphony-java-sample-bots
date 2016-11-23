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

package org.symphonyoss.simplebot;

import org.junit.Test;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.exceptions.StreamsException;
import org.symphonyoss.exceptions.UsersClientException;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class EchoBotTest {

    private static final long TIMEOUT_MS = 10000;
    private static Set<String> senderParamNames = new HashSet<>();
    private static String TEST_MESSAGE = "Testing the EchoBot";

    static
    {
        senderParamNames.add("sessionauth.url");
        senderParamNames.add("keyauth.url");
        senderParamNames.add("pod.url");
        senderParamNames.add("agent.url");
        senderParamNames.add("truststore.file");
        senderParamNames.add("truststore.password");
        senderParamNames.add("sender.user.cert.file");
        senderParamNames.add("sender.user.cert.password");
        senderParamNames.add("sender.user.email");
    }

    @Test
    public void sendAndReceiveEcho() {
        try {
            //Creating and running the EchoBot
            EchoBot echoBot = new EchoBot();
            echoBot.start();

            // Reading sender user credentials and getting the SymphonyClient
            Utils utils = new Utils();
            Map<String,String> senderBotParams = utils.readInitParams(senderParamNames);
            senderBotParams = adaptSenderParams(senderBotParams);
            SymphonyClient senderBot = utils.getSymphonyClient(senderBotParams);

            try {
                //Sender user creates a chat adding the echoBot user
                Chat chat = createChat(senderBot, echoBot.getUserEmail());

                //A MessageMatcher will listen to the responses on the chat just created
                MessageMatcher messageMatcher = new MessageMatcher(chat, TEST_MESSAGE);
                chat.registerListener(messageMatcher);

                //The sender sends a message to the chat
                utils.sendMessage(senderBot, chat, TEST_MESSAGE, SymMessage.Format.MESSAGEML);

                //We ask the MessageMatcher if something have arrived every half second, until timeout hits
                waitForMessage(messageMatcher);

                //We expect the MessageMatcher to have found the match
                assertTrue(messageMatcher.hasMatched());
            } catch (UsersClientException e) {
                throw new RuntimeException(e);
            } catch (StreamsException e) {
                throw new RuntimeException(e);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Chat createChat(SymphonyClient client, String remoteUserEmail) throws StreamsException, UsersClientException {
        Chat chat = new Chat();
        chat.setLocalUser(client.getLocalUser());
        Set<SymUser> remoteUsers = new HashSet<>();
        remoteUsers.add(client.getUsersClient().getUserFromEmail(remoteUserEmail));
        chat.setRemoteUsers(remoteUsers);
        chat.setStream(client.getStreamsClient().getStream(remoteUsers));
        client.getChatService().addChat(chat);
        return chat;
    }

    private void waitForMessage(MessageMatcher messageMatcher) {
        long endTimeMillis = System.currentTimeMillis() + TIMEOUT_MS;
        while (true) {
            if (System.currentTimeMillis() > endTimeMillis || messageMatcher.hasMatched()) {
                break;
            }
            try {
                Thread.sleep(500);
            }
            catch (InterruptedException t) {}
        }
    }

    private Map<String, String> adaptSenderParams(Map<String, String> senderBotParams) {
        Map<String, String> ret = senderBotParams;
        for(Iterator<Map.Entry<String, String>> it = ret.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, String> entry = it.next();
            if (entry.getKey().startsWith("bot.user")) {
                ret.put(entry.getKey().replace("sender.user","bot.user"), entry.getValue());
            }
        }
        return ret;
    }

    private class MessageMatcher implements ChatListener {
        private String messageTest;
        private boolean matched = false;

        @Override
        public void onChatMessage(SymMessage symMessage) {
            if (symMessage.equals(messageTest)) {
                this.matched = true;
            }
        }

        public MessageMatcher(Chat chat, String messageTest) {
            this.messageTest = messageTest;
        }

        public boolean hasMatched() {
            return matched;
        }
    }
}