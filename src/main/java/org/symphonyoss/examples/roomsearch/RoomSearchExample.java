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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.symphonyoss.examples.roomsearch;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.SymphonyClientFactory;
import org.symphonyoss.client.services.RoomService;
import org.symphonyoss.exceptions.StreamsException;
import org.symphonyoss.symphony.clients.model.SymRoomDetail;
import org.symphonyoss.symphony.clients.model.SymRoomSearchCriteria;
import org.symphonyoss.symphony.clients.model.SymRoomSearchResults;


/**
 * Simple example of room search functions.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * REQUIRED VM Arguments or System Properties:
 * <p>
 * -Dsessionauth.url=https://pod_fqdn:port/sessionauth
 * -Dkeyauth.url=https://pod_fqdn:port/keyauth
 * -Dsymphony.agent.pod.url=https://agent_fqdn:port/pod
 * -Dsymphony.agent.agent.url=https://agent_fqdn:port/agent
 * -Dcerts.dir=/dev/certs/
 * -Dkeystore.password=(Pass)
 * -Dtruststore.file=/dev/certs/server.truststore
 * -Dtruststore.password=(Pass)
 * -Dbot.user=bot.user1
 * -Dbot.domain=domain.com
 * -Duser.call.home=frank.tarsillo@markit.com
 * -Droom.search.query=(search term)
 * <p>
 * <p>
 * <p>
 * <p>
 * Created by Frank Tarsillo on 5/15/2016.
 */
public class RoomSearchExample {


    private final Logger logger = LoggerFactory.getLogger(RoomSearchExample.class);
    private RoomService roomService;

    public RoomSearchExample() {


        init();


    }

    public static void main(String[] args) {

        new RoomSearchExample();

    }

    public void init() {

        logger.info("Room Example starting...");

        try {


            logger.debug("{} {}", System.getProperty("sessionauth.url"),
                    System.getProperty("keyauth.url"));


            //Create an initialized client
            SymphonyClient symClient = SymphonyClientFactory.getClient(
                    SymphonyClientFactory.TYPE.BASIC,
                    System.getProperty("bot.user") + System.getProperty("bot.domain"), //bot email
                    System.getProperty("certs.dir") + System.getProperty("bot.user") + ".p12", //bot cert
                    System.getProperty("keystore.password"), //bot cert/keystore pass
                    System.getProperty("truststore.file"), //truststore file
                    System.getProperty("truststore.password"));  //truststore password


            SymRoomSearchCriteria symRoomSearchCriteria = new SymRoomSearchCriteria();
            symRoomSearchCriteria.setQuery(System.getProperty("room.search.query"));

            SymRoomSearchResults symRoomSearchResults = symClient.getStreamsClient().roomSearch(symRoomSearchCriteria, 0, 100);

            for (SymRoomDetail symRoomDetail : symRoomSearchResults.getRooms()) {

                logger.info("Found room {}: {}", symRoomDetail.getRoomAttributes().getName(), symRoomDetail.getRoomSystemInfo().getId());


            }

            symClient.shutdown();

            System.exit(1);


        } catch (StreamsException e) {
            logger.error("error", e);
        } catch (Exception e) {
            logger.error("Unkown Exception", e);
        }

    }

}
