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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.SymphonyClientConfig;
import org.symphonyoss.client.SymphonyClientConfigID;
import org.symphonyoss.client.SymphonyClientFactory;
import org.symphonyoss.client.exceptions.MessagesException;
import org.symphonyoss.client.exceptions.ProgramFault;
import org.symphonyoss.client.exceptions.SymException;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

public class RssBot {

	private final Logger			logger			= LoggerFactory.getLogger(RssBot.class);

	private SymphonyClientConfig	config	= new SymphonyClientConfig();
	private SymphonyClient			symClient;
	private Chat					chat;
	private URL						feedUrl;
	private Integer					limit;

    public RssBot() throws SymException {
    	initConfig();
        initAuth();
        initChat();
        sendMessage("Hey there! I'm the RSS Bot!");
        sendRssFeeds();
        sendMessage("All done here, bye!");
    }

    private void initConfig() {
    	String s = config.getRequired("rss.url");
		
    	try {
    		feedUrl = new URL(s);
		} catch (MalformedURLException e) {
			throw new ProgramFault("Invalid RSS URL \"" + s + "\"", e);
		}
        limit = new Integer(config.getRequired("rss.limit"));
	}

	private void initAuth() throws SymException {
    	symClient = SymphonyClientFactory.getClient(SymphonyClientFactory.TYPE.BASIC);

        symClient.init(config);
    }

    private void initChat() {
        this.chat = new Chat();
        chat.setLocalUser(symClient.getLocalUser());
        Set<SymUser> remoteUsers = new HashSet<>();

        try {
            remoteUsers.add(symClient.getUsersClient().getUserFromEmail(config.getRequired(SymphonyClientConfigID.RECEIVER_EMAIL)));
            chat.setRemoteUsers(remoteUsers);
            chat.setStream(symClient.getStreamsClient().getStream(remoteUsers));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SymMessage getMessage(String message) {
        SymMessage aMessage = new SymMessage();
        aMessage.setFormat(SymMessage.Format.TEXT);
        aMessage.setMessage(message);
        return aMessage;
    }

    private void sendMessage(String message) {
        SymMessage messageSubmission = getMessage(message);

        try {
            symClient.getMessageService().sendMessage(this.chat, messageSubmission);
            logger.info("[MESSAGE] - "+message);
            System.out.println("[MESSAGE] - "+message);
        } catch (MessagesException e) {
            e.printStackTrace();
        }


    }

    private void sendRssFeeds() {
        try {
            sendMessage("Fetching "+feedUrl);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedUrl));
            List<SyndEntry> entries = feed.getEntries();
            sendMessage("Found " + feed.getEntries().size() + " items in the feed; printing the first "+ limit);
            
            for (int i=0; i<limit; i++) {
                SyndEntry entry = entries.get(i);
                sendMessage(entry.getTitle() + "-" + entry.getLink());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FeedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
