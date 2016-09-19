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

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.FeedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.SymphonyClientFactory;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.model.SymAuth;
import org.symphonyoss.exceptions.MessagesException;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.clients.AuthorizationClient;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;
import org.symphonyoss.symphony.pod.model.User;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class RssBot {

    private final Logger logger = LoggerFactory.getLogger(RssBot.class);
    private SymphonyClient symClient;
    private Map<String,String> initParams = new HashMap<String,String>();
    private Chat chat;

    private static Set<String> initParamNames = new HashSet<String>();
    static {
        initParamNames.add("sessionauth.url");
        initParamNames.add("keyauth.url");
        initParamNames.add("pod.url");
        initParamNames.add("agent.url");
        initParamNames.add("rss.url");
        initParamNames.add("rss.limit");
        initParamNames.add("truststore.file");
        initParamNames.add("truststore.password");
        initParamNames.add("keystore.password");
        initParamNames.add("certs.dir");
        initParamNames.add("bot.user.name");
        initParamNames.add("bot.user.email");
        initParamNames.add("receiver.user.email");
    }

    public static void main(String[] args) {
        new RssBot();
        System.exit(0);
    }

    public RssBot() {
        initParams();
        initAuth();
        initChat();
        sendMessage("Hey there! I'm the RSS Bot!");
        sendRssFeeds();
        sendMessage("All done here, bye!");
    }

    private void initParams() {
        for(String initParam : initParamNames) {
            String systemProperty = System.getProperty(initParam);
            if (systemProperty == null) {
                throw new IllegalArgumentException("Cannot find system property; make sure you're using -D" + systemProperty + " to run RssBot");
            } else {
                initParams.put(initParam,systemProperty);
            }
        }
    }

    private void initAuth() {
        try {
            symClient = SymphonyClientFactory.getClient(SymphonyClientFactory.TYPE.BASIC);

            logger.debug("{} {}", System.getProperty("sessionauth.url"),
                    System.getProperty("keyauth.url"));


            AuthorizationClient authClient = new AuthorizationClient(
                    initParams.get("sessionauth.url"),
                    initParams.get("keyauth.url"));


            authClient.setKeystores(
                    initParams.get("truststore.file"),
                    initParams.get("truststore.password"),
                    initParams.get("certs.dir") + initParams.get("bot.user.name") + ".p12",
                    initParams.get("keystore.password"));

            SymAuth symAuth = authClient.authenticate();


            symClient.init(
                    symAuth,
                    initParams.get("bot.user.email"),
                    initParams.get("agent.url"),
                    initParams.get("pod.url")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initChat() {
        this.chat = new Chat();
        chat.setLocalUser(symClient.getLocalUser());
        Set<SymUser> remoteUsers = new HashSet<>();

        try {
            remoteUsers.add(symClient.getUsersClient().getUserFromEmail(initParams.get("receiver.user.email")));
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
            sendMessage("Fetching "+initParams.get("rss.url"));
            URL feedUrl = new URL(initParams.get("rss.url"));
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedUrl));
            List<SyndEntry> entries = feed.getEntries();
            sendMessage("Found " + feed.getEntries().size() + " items in the feed; printing the first "+ initParams.get("rss.limit"));
            Integer limit = new Integer(initParams.get("rss.limit"));

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
