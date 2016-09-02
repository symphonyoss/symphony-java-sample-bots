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
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.clients.AuthorizationClient;
import org.symphonyoss.symphony.pod.model.User;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StockInfoBot
    implements ChatListener
{
    private final static Logger log = LoggerFactory.getLogger(StockInfoBot.class);

    private final static Pattern    CASHTAG_REGEX  = Pattern.compile("<cash tag=\"([^\"]+)\"/>");
    private final static DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");


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
            StockInfoBot bot = new StockInfoBot();
            bot.start();
        }
        catch (Exception e)
        {
            returnCode = -1;
            log.error("Unexpected exception.", e);
        }

        System.exit(returnCode);
    }

    public StockInfoBot()
        throws Exception
    {
        initParams();
        initAuth();
        initChat();
    }

    public void start()
        throws Exception
    {
        Thread.sleep(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES));
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
        throws Exception
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
        throws Exception
    {
        this.chat = new Chat();
        chat.setLocalUser(symClient.getLocalUser());
        Set<User> remoteUsers = new HashSet<>();

        remoteUsers.add(symClient.getUsersClient().getUserFromEmail(initParams.get("receiver.user.email")));
        chat.setRemoteUsers(remoteUsers);
        chat.setStream(symClient.getStreamsClient().getStream(remoteUsers));

        chat.registerListener(this);
        symClient.getChatService().addChat(chat);
    }

    private void sendMessage(String message, MessageSubmission.FormatEnum messageFormat)
        throws Exception
    {
        MessageSubmission messageSubmission = new MessageSubmission();
        messageSubmission.setFormat(messageFormat);
        messageSubmission.setMessage(message);

        symClient.getMessageService().sendMessage(chat, messageSubmission);
    }

    private String[] parseCashTags(String messageText)
    {
        String[]     result = null;
        List<String> temp   = new ArrayList<>();

        if (messageText != null)
        {
            Matcher matcher = CASHTAG_REGEX.matcher(messageText);

            while (matcher.find())
            {
                temp.add(matcher.group(1));
            }
        }

        result = new String[temp.size()];
        result = temp.toArray(result);

        return(result);
    }

    private String buildStockMessage(Stock stock)
        throws Exception
    {
        StringBuilder result = new StringBuilder();

        result.append("\n--------------------------------\n");
        result.append("Symbol: " + stock.getSymbol() + "\n");
        result.append("Name: " + stock.getName() + "\n");
        result.append("Currency: " + stock.getCurrency() + "\n");
        result.append("Stock Exchange: " + stock.getStockExchange() + "\n");
        result.append("Quote: " + String.valueOf(stock.getQuote()) + "\n");
        result.append("Stats: " + String.valueOf(stock.getStats()) + "\n");
        result.append("Dividend: " + String.valueOf(stock.getDividend()) + "\n");

        return(result.toString());
    }

    @Override
    public void onChatMessage(Message message)
    {
        try
        {
            String messageText = message.getMessage();

            if (messageText != null)
            {
                String[]           stocks       = parseCashTags(messageText);
                Map<String, Stock> stocksData   = YahooFinance.get(stocks);
                StringBuilder      stockMessage = new StringBuilder();

                for (String stock : stocksData.keySet())
                {
                    Stock  stockData    = stocksData.get(stock);
                    stockMessage.append(buildStockMessage(stockData));

                }

                sendMessage(stockMessage.toString(), MessageSubmission.FormatEnum.TEXT);
            }
        }
        catch (Exception e)
        {
            log.error("Unexpected exception.", e);
        }
    }

}
