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

package org.symphonyoss.examples.presenceservice;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.SymphonyClientFactory;
import org.symphonyoss.client.model.SymAuth;
import org.symphonyoss.client.services.PresenceListener;
import org.symphonyoss.exceptions.UsersClientException;
import org.symphonyoss.symphony.clients.AuthorizationClient;
import org.symphonyoss.symphony.pod.model.UserPresence;


/**
 *
 *
 * Simple example of the Presence Service.
 *
 * It will send a message to a call.home.user and listen/create new Chat sessions.
 *
 *
 *
 * REQUIRED VM Arguments or System Properties:
 *
 *        -Dsessionauth.url=https://pod_fqdn:port/sessionauth
 *        -Dkeyauth.url=https://pod_fqdn:port/keyauth
 *        -Dsymphony.agent.pod.url=https://agent_fqdn:port/pod
 *        -Dsymphony.agent.agent.url=https://agent_fqdn:port/agent
 *        -Dcerts.dir=/dev/certs/
 *        -Dkeystore.password=(Pass)
 *        -Dtruststore.file=/dev/certs/server.truststore
 *        -Dtruststore.password=(Pass)
 *        -Dbot.user=bot.user1
 *        -Dbot.domain=@domain.com
 *        -Duser.call.home=frank.tarsillo@markit.com
 *
 *
 *
 *
 * @author  Frank Tarsillo
 */
//NOSONAR
public class PresenceServiceExample implements PresenceListener {


    private final Logger logger = LoggerFactory.getLogger(PresenceServiceExample.class);
    private SymphonyClient symClient;

    public PresenceServiceExample() {


        init();


    }

    public static void main(String[] args) {



        new PresenceServiceExample();

    }

    public void init() {

        logger.info("Presence Service example starting...");

        try {


            logger.debug("{} {}", System.getProperty("sessionauth.url"),
                    System.getProperty("keyauth.url"));


            //Create an initialized client
            symClient = SymphonyClientFactory.getClient(
                    SymphonyClientFactory.TYPE.BASIC,
                    System.getProperty("bot.user") + System.getProperty("bot.domain"), //bot email
                    System.getProperty("certs.dir") + System.getProperty("bot.user") + ".p12", //bot cert
                    System.getProperty("keystore.password"), //bot cert/keystore pass
                    System.getProperty("truststore.file"), //truststore file
                    System.getProperty("truststore.password"));  //truststore password



            symClient.getPresenceService().addPresenceListener(this);

            symClient.getPresenceClient().getAllUserPresence();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //Callback from PresenceService.  This will monitor all presence on the network.
   @Override
    public void onUserPresence(UserPresence userPresence) {

        try {
            logger.debug("Received user presence change from: {} : {}: {}",
                    userPresence.getUid(),
                    symClient.getUsersClient().getUserFromId(userPresence.getUid()).getEmailAddress(),
                    userPresence.getCategory());
        }catch (UsersClientException e){

            logger.error("Failed to retrieve email from userID..",e);
        }

    }

}
