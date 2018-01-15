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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.Utils;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.SymphonyClientConfig;
import org.symphonyoss.client.exceptions.AuthenticationException;
import org.symphonyoss.client.exceptions.InitException;
import org.symphonyoss.client.exceptions.MessagesException;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.symphony.clients.model.SymMessage;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StockInfoBot
        implements ChatListener, ChatServiceListener {
    private final static Logger log = LoggerFactory.getLogger(StockInfoBot.class);

    private final static Pattern CASHTAG_REGEX = Pattern.compile("<cash tag=\"([^\"]+)\"/>");
    private final static DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    private SymphonyClientConfig config = new SymphonyClientConfig(true);
    private SymphonyClient symClient;
    private Chat chat;

    public static void main(String[] args) throws Exception {
        new StockInfoBot();
        Utils.hang();
    }

    public StockInfoBot() throws InitException, AuthenticationException {
        // Get SJC instance and register as listener
        this.symClient = Utils.getSymphonyClient(config);
        this.symClient.getChatService().addListener(this);
    }

    @Override
    public void onChatMessage(SymMessage message) {
        String messageText = message.getMessage();

        try {
            if (messageText != null) {
                String[] stocks = parseCashTags(messageText);
                Map<String, Stock> stocksData = YahooFinance.get(stocks);
                StringBuilder stockMessage = new StringBuilder();

                for (String stock : stocksData.keySet()) {
                    Stock stockData = stocksData.get(stock);
                    stockMessage.append(buildStockMessage(stockData));

                }
                sendMessage(stockMessage.toString());
            }
        } catch (MessagesException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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

    private void sendMessage(String message) throws MessagesException {
        Utils.sendMessage(symClient, chat, message);
    }

    private String[] parseCashTags(String messageText) {
        String[] result = null;
        List<String> temp = new ArrayList<>();
        if (messageText != null) {
            Matcher matcher = CASHTAG_REGEX.matcher(messageText);
            while (matcher.find()) {
                temp.add(matcher.group(1));
            }
        }
        result = new String[temp.size()];
        result = temp.toArray(result);
        return (result);
    }

    private String buildStockMessage(Stock stock) {
        StringBuilder result = new StringBuilder();
        result.append("\n--------------------------------\n");
        result.append("Symbol: " + stock.getSymbol() + "\n");
        result.append("Name: " + stock.getName() + "\n");
        result.append("Currency: " + stock.getCurrency() + "\n");
        result.append("Stock Exchange: " + stock.getStockExchange() + "\n");
        result.append("Quote: " + String.valueOf(stock.getQuote()) + "\n");
        result.append("Stats: " + String.valueOf(stock.getStats()) + "\n");
        result.append("Dividend: " + String.valueOf(stock.getDividend()) + "\n");
        return (result.toString());
    }
}
