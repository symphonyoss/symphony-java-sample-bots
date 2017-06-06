package org.symphonyoss.examples.streams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.SymphonyClientFactory;
import org.symphonyoss.client.model.CacheType;
import org.symphonyoss.client.services.SymUserCache;
import org.symphonyoss.exceptions.StreamsException;
import org.symphonyoss.symphony.clients.model.SymAdminStreamAttributes;
import org.symphonyoss.symphony.clients.model.SymAdminStreamFilter;
import org.symphonyoss.symphony.clients.model.SymAdminStreamInfo;
import org.symphonyoss.symphony.clients.model.SymAdminStreamList;

import java.util.Date;


/**
 * Streams example showing how you can search for bot user associated streams by criteria filer.
 *
 * @author Frank Tarsillo on 5/9/17.
 */
//NOSONAR
public class StreamsExample {


    private final Logger logger = LoggerFactory.getLogger(StreamsExample.class);

    private SymphonyClient symClient;

    public StreamsExample() {


        init();


    }

    public static void main(String[] args) {

        new StreamsExample();

    }

    public void init() {


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

            SymAdminStreamFilter symAdminStreamFilter = new SymAdminStreamFilter();

// Uncomment below to filter on ROOM streams..
//
//            List<SymStreamType> symStreamTypes = new ArrayList<>();
//            SymStreamType symStreamType = new SymStreamType();
//            symStreamType.setType(SymStreamType.Type.ROOM);
//
//            symStreamTypes.add(symStreamType);
//
//            symAdminStreamFilter.setStreamTypes(symStreamTypes);


            SymAdminStreamList symAdminStreamList = symClient.getStreamsClient().getStreams(null, null, symAdminStreamFilter);

            for (SymAdminStreamInfo symAdminStreamInfo : symAdminStreamList.getStreams()) {
                prettyOutput(symAdminStreamInfo);

            }

            symClient.shutdown();

            logger.info("Finished");


        } catch (StreamsException e) {
            logger.error("error", e);
        }

    }


    public void prettyOutput(SymAdminStreamInfo symAdminStreamInfo) {

        SymUserCache symUserCache = (SymUserCache) symClient.getCache(CacheType.USER);

        StringBuffer stringBuffer = new StringBuffer();


        stringBuffer.append("[").append(symAdminStreamInfo.getId()).append("]:");
        stringBuffer.append("[").append(symAdminStreamInfo.getType()).append("]:");
        stringBuffer.append("[EXTERNAL:").append(symAdminStreamInfo.getIsExternal().toString()).append("] ");


        SymAdminStreamAttributes attrib = symAdminStreamInfo.getAttributes();

        try {
            stringBuffer.append("Created By: ").append(symUserCache.getUserById(attrib.getCreatedByUserId()).getDisplayName()).append(" ,");
        } catch (Exception e) {
            logger.error("failed to retrieve user {}", attrib.getCreatedByUserId());
        }


        stringBuffer.append("Created Date: ").append(new Date(attrib.getCreatedDate()).toString()).append(" ,");
        stringBuffer.append("#Members: ").append(attrib.getMembersCount()).append(" ,");
        stringBuffer.append("RoomName: ").append(attrib.getRoomName()).append(" ,");
        stringBuffer.append("Company: ").append(attrib.getOriginCompany()).append(" ,");
        stringBuffer.append("Users: ");


        for (Long uid : attrib.getMembers()) {


            try {
                stringBuffer.append(symUserCache.getUserById(uid).getDisplayName()).append(", ");
            } catch (Exception e) {
                logger.error("failed to retrieve user {}", uid);
            }

        }

        stringBuffer.append("\n");

        logger.info("{}", stringBuffer.toString());

    }

}



