package io.github.lightman314.lightmanscurrency.api.client;

import io.github.lightman314.lightmanscurrency.network.message.player.CPacketRequestPlayerID;
import io.github.lightman314.lightmanscurrency.network.message.player.CPacketRequestPlayerName;
import io.github.lightman314.lightmanscurrency.network.message.player.SPacketPlayerCacheResult;

import javax.annotation.Nullable;
import java.util.*;

public final class ClientPlayerNameCache {
    private ClientPlayerNameCache() {}

    private static final Map<UUID,String> idToNameCache = new HashMap<>();
    private static final Map<String,UUID> nameToIdCache = new HashMap<>();

    private static final Set<UUID> sentNameRequest = new HashSet<>();
    private static final Set<String> sentIDRequests = new HashSet<>();

    @Nullable
    public static String lookupName(UUID playerID) { return lookupName(playerID,true); }
    @Nullable
    public static String lookupName(UUID playerID,boolean sendRequest) {
        if(idToNameCache.containsKey(playerID))
            return idToNameCache.get(playerID);
        if(sendRequest && !sentNameRequest.contains(playerID)) {
            sentNameRequest.add(playerID);
            new CPacketRequestPlayerName(playerID).send();
        }
        return null;
    }

    @Nullable
    public static UUID lookupID(String playerName) { return lookupID(playerName,true); }
    @Nullable
    public static UUID lookupID(String playerName,boolean sendRequest) {
        //Ignore completely if the name is blank
        if(playerName.isBlank())
            return null;
        //Always process id request in lower-case english so that "HeLlOwOrLd" will have the same lookup result as "HelloWorld"
        playerName = playerName.toLowerCase(Locale.ENGLISH);
        if(nameToIdCache.containsKey(playerName))
            return nameToIdCache.get(playerName);
        if(sendRequest && !sentIDRequests.contains(playerName)) {
            sentIDRequests.add(playerName);
            new CPacketRequestPlayerID(playerName).send();
        }
        return null;
    }

    public static void handlePacket(SPacketPlayerCacheResult packet) {
        try {
            idToNameCache.put(packet.playerID,packet.playerName);
            //Cache name to id with the lower-case name so that case is properly ignored when checking for the players ID
            String lowerName = packet.playerName.toLowerCase(Locale.ENGLISH);
            nameToIdCache.put(lowerName,packet.playerID);
            //Clear the sent requests cache
            sentIDRequests.remove(lowerName);
            sentNameRequest.remove(packet.playerID);
        } catch (Exception ignored) {}
    }

}
