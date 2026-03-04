package io.github.lightman314.lightmanscurrency.client.data;

import io.github.lightman314.lightmanscurrency.network.message.player.*;

import javax.annotation.Nullable;
import java.util.*;

public class ClientPlayerNameCache {

    private static final Map<UUID,String> cache = new HashMap<>();

    private static final List<UUID> sentNameRequests = new ArrayList<>();
    private static final List<String> sentIDRequests = new ArrayList<>();

    @Nullable
    public static String lookupName(UUID playerID) { return lookupName(playerID,true); }
    @Nullable
    public static String lookupName(UUID playerID, boolean sendDataRequest)
    {
        if(cache.containsKey(playerID))
            return cache.get(playerID);
        //Send name request to the server if we haven't already
        if(sendDataRequest && !sentNameRequests.contains(playerID))
        {
            sentNameRequests.add(playerID);
            new CPacketRequestName(playerID).sendToServer();
        }
        return null;
    }

    @Nullable
    public static UUID lookupID(String playerName) { return lookupID(playerName,true); }
    @Nullable
    public static UUID lookupID(String playerName, boolean sendDataRequest)
    {
        for(Map.Entry<UUID,String> entry : cache.entrySet())
        {
            String name = entry.getValue();
            if(name != null && name.equalsIgnoreCase(playerName))
                return entry.getKey();
        }
        //Send ID request to the server if we haven't already
        if(sendDataRequest && !sentIDRequests.contains(playerName))
        {
            sentIDRequests.add(playerName);
            new CPacketRequestID(playerName).sendToServer();
        }
        return null;
    }

    public static void addCacheEntry(UUID playerID, String playerName) {
        cache.put(playerID,playerName);
        sentNameRequests.remove(playerID);
        for(int i = 0; i < sentIDRequests.size(); ++i)
        {
            if(sentIDRequests.get(i).equalsIgnoreCase(playerName))
                sentIDRequests.remove(i--);
        }
    }

}
