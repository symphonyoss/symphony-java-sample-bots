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

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.Utils;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.SymphonyClientConfig;
import org.symphonyoss.client.SymphonyClientConfigID;
import org.symphonyoss.client.exceptions.MessagesException;
import org.symphonyoss.client.exceptions.StreamsException;
import org.symphonyoss.client.exceptions.SymException;
import org.symphonyoss.client.exceptions.UsersClientException;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RssBot {

    private final Logger logger = LoggerFactory.getLogger(RssBot.class);

    private SymphonyClientConfig config = new SymphonyClientConfig(true);
    private SymphonyClient symClient;
    private Chat chat;
    private URL feedUrl;
    private Integer limit;

    public static void main(String[] args) throws Exception {
        new RssBot();
        System.exit(0);
    }

    public RssBot() throws SymException, IOException, FeedException {
        // Get SJC instance
        this.symClient = Utils.getSymphonyClient(config);

        // Read bot configuration
        this.feedUrl = new URL(config.getRequired("rss.url"));
        this.limit = new Integer(config.getRequired("rss.limit"));

        initChat();
        sendMessage("Hey there! I'm the RSS Bot!");
        sendRssFeeds();
        sendMessage("All done here, bye!");
        System.exit(0);
    }

    private void initChat() throws UsersClientException, StreamsException {
        this.chat = new Chat();
        chat.setLocalUser(symClient.getLocalUser());
        Set<SymUser> remoteUsers = new HashSet<>();
        remoteUsers.add(symClient.getUsersClient().getUserFromEmail(config.getRequired(SymphonyClientConfigID.RECEIVER_EMAIL)));
        chat.setRemoteUsers(remoteUsers);
        chat.setStream(symClient.getStreamsClient().getStream(remoteUsers));
    }

    private void sendRssFeeds() throws MessagesException, IOException, FeedException {
        sendMessage("Fetching " + feedUrl);
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(feedUrl));
        List<SyndEntry> entries = feed.getEntries();
        sendMessage("Found " + feed.getEntries().size() + " items in the feed; printing the first " + limit);

        for (int i = 0; i < limit; i++) {
            SyndEntry entry = entries.get(i);
            sendMessage(entry.getTitle() + "-" + entry.getLink());
        }
    }

    private void sendMessage(String message) throws MessagesException {
        Utils.sendMessage(symClient, chat, message);
    }
}
