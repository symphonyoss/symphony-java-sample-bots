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
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.exceptions.*;
import org.symphonyoss.symphony.clients.model.SymMessage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static spark.Spark.*;
import static java.lang.Thread.sleep;

public class EchoBot
    implements ChatListener, ChatServiceListener {

    private final static String CHECK_URL_PATH = "/status";

    private final static Logger log = LoggerFactory.getLogger(EchoBot.class);

    private SymphonyClient symClient;
    private Map<String,String> initParams = new HashMap<>();
    private Utils utils = new Utils();

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
        initParamNames.add("sender.user.email");
    }

    public EchoBot() {
        this.initParams = utils.readInitParams(initParamNames);
        this.symClient = utils.getSymphonyClient(initParams);
        this.symClient.getChatService().addListener(this);

        runHealthCheckServer();

        while (true) {
          try {
            sleep(5000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
    }

  private void runHealthCheckServer() {
      List<String> urls = Arrays.asList(symClient.getAgentUrl(), symClient.getServiceUrl());
      get(CHECK_URL_PATH, (req, res) -> {
        log.debug("Running health check server; monitoring urls: " + urls);
        boolean result = checkUrls(urls);
        log.debug("Check passed: " + result);
        if (result) {
          res.type("application/json");
          return "ok";
        } else {
          res.status(500);
          return "fail";
        }
      });
  }

  private boolean checkUrls(List<String> urls) {
    for(String url : urls) {
      try (Socket socket = new Socket()) {
        String domain = new URI(url).getHost();
        log.debug("connecting to "+domain);
        socket.connect(new InetSocketAddress(domain, 443), 1000);
        log.debug("connected to "+domain);
      } catch (IOException e) {
        log.error("i/o error checking urls "+urls,e);
        return false;
      } catch (URISyntaxException e) {
        e.printStackTrace();
        log.error("uri syntax error checking urls "+urls,e);
        return false;
      }
    }
    return true;
  }


  public String getUserEmail() {
    return initParams.get("sender.user.email");
  }

  @Override
    public void onChatMessage(SymMessage message) {
        log.debug("on chat message");
        String messageText = message.getMessage();

        try {
          Chat chat = symClient.getChatService().getChatByStream(message.getStreamId());
          utils.sendMessage(symClient, chat, messageText, SymMessage.Format.MESSAGEML);
        } catch (MessagesException e) {
          e.printStackTrace();
          log.error("Error sending message",e);
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
