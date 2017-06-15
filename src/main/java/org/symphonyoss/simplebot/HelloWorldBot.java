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

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.SymphonyClientConfig;
import org.symphonyoss.client.SymphonyClientConfigID;
import org.symphonyoss.client.SymphonyClientFactory;
import org.symphonyoss.client.exceptions.AuthorizationException;
import org.symphonyoss.client.exceptions.InitException;
import org.symphonyoss.client.exceptions.NetworkException;
import org.symphonyoss.client.exceptions.SymException;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;

public class HelloWorldBot
{
    private final static Logger log = LoggerFactory.getLogger(HelloWorldBot.class);

	private SymphonyClientConfig	config;
	private SymphonyClient			symClient;
	private Chat					chat;
	
    public HelloWorldBot() throws SymException
    {
    	initAuth();
        initChat();
    }

    private void initAuth() throws AuthorizationException, InitException, NetworkException {
    	config = new SymphonyClientConfig();
	
        symClient = SymphonyClientFactory.getClient(SymphonyClientFactory.TYPE.BASIC);

        symClient.init(config);
    }
    
	public void start() throws SymException
    {
    	log.info("Say hello");
        sendMessage("Hello " + config.get(SymphonyClientConfigID.RECEIVER_EMAIL) + "!");
    }

    private void initChat() throws SymException
    {
        this.chat = new Chat();
        chat.setLocalUser(symClient.getLocalUser());
        Set<SymUser> remoteUsers = new HashSet<>();

        
		remoteUsers.add(symClient.getUsersClient().getUserFromEmail(config.get(SymphonyClientConfigID.RECEIVER_EMAIL)));
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
