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
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.services.ChatService;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.exceptions.AuthorizationException;
import org.symphonyoss.exceptions.InitException;
import org.symphonyoss.exceptions.StreamsException;
import org.symphonyoss.exceptions.SymException;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.clients.AuthorizationClient;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;
import org.symphonyoss.symphony.pod.model.Stream;
import org.symphonyoss.symphony.pod.model.User;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class HelloWorldBot
{
    private final static Logger log = LoggerFactory.getLogger(HelloWorldBot.class);

    private SymphonyClient     symClient;
    private Map<String,String> initParams = new HashMap<>();
    private Chat               chat;

    private static Set<String> initParamNames = new HashSet<>();

    static
    {
        initParamNames.add("sessionauth.url");
        initParamNames.add("keyauth.url");
        initParamNames.add("pod.url");
        initParamNames.add("agent.url");
        initParamNames.add("truststore.file");
        initParamNames.add("truststore.password");
        initParamNames.add("bot.user.cert.file");
        initParamNames.add("bot.user.cert.password");
        initParamNames.add("bot.user.email");
        initParamNames.add("receiver.user.email");
    }

    public static void main(String[] args)
    {
        int returnCode = 0;

        try
        {
            HelloWorldBot bot = new HelloWorldBot();
            bot.start();
        }
        catch (Exception e)
        {
            returnCode = -1;
            log.error("Unexpected exception.", e);
        }

        System.exit(returnCode);
    }

    public HelloWorldBot()
        throws Exception
    {
        initParams();
        initAuth();
        initChat();
    }

    public void start()
        throws Exception
    {
        sendMessage("Hello world!");
    }

    private void initParams()
    {
        for (String initParam : initParamNames)
        {
            String initParamValue = System.getProperty(initParam);

            if (initParamValue == null)
            {
                throw new IllegalArgumentException("Cannot find required property; make sure you're using -D" + initParam + " to run HelloWorldBot");
            }
            else
            {
                initParams.put(initParam, initParamValue);
            }
        }
    }

    private void initAuth()
        throws AuthorizationException, InitException
    {
        symClient = SymphonyClientFactory.getClient(SymphonyClientFactory.TYPE.BASIC);

        log.debug("{} {}", System.getProperty("sessionauth.url"),
                           System.getProperty("keyauth.url"));

        AuthorizationClient authClient = new AuthorizationClient(
                initParams.get("sessionauth.url"),
                initParams.get("keyauth.url"));

        authClient.setKeystores(
                initParams.get("truststore.file"),
                initParams.get("truststore.password"),
                initParams.get("bot.user.cert.file"),
                initParams.get("bot.user.cert.password"));

        SymAuth symAuth = authClient.authenticate();

        symClient.init(
                symAuth,
                initParams.get("bot.user.email"),
                initParams.get("agent.url"),
                initParams.get("pod.url")
        );
    }

    private void initChat()
        throws SymException
    {
        this.chat = new Chat();
        chat.setLocalUser(symClient.getLocalUser());
        Set<SymUser> remoteUsers = new HashSet<>();

        remoteUsers.add(symClient.getUsersClient().getUserFromEmail(initParams.get("receiver.user.email")));
        chat.setRemoteUsers(remoteUsers);
        chat.setStream(symClient.getStreamsClient().getStream(remoteUsers));
    }

    private void sendMessage(String message)
        throws SymException
    {
        SymMessage messageSubmission = new SymMessage();
        messageSubmission.setFormat(SymMessage.Format.TEXT);
        messageSubmission.setMessage(message);

        symClient.getMessageService().sendMessage(chat, messageSubmission);
    }

}
