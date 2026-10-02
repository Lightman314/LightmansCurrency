package io.github.lightman314.lightmanscurrency.api.client;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.lightman314.lightmanscurrency.network.message.player.CPacketRequestPlayerID;
import io.github.lightman314.lightmanscurrency.network.message.player.CPacketRequestPlayerName;
import io.github.lightman314.lightmanscurrency.network.message.player.SPacketPlayerCacheResult;

import javax.annotation.Nullable;
import java.util.*;

public final class ClientPlayerNameCache {
    private ClientPlayerNameCache() {}

    private static final BiMap<UUID,String> idToNameCache = HashBiMap.create();
    private static final BiMap<String,UUID> nameToIdCache = idToNameCache.inverse();

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
            idToNameCache.forcePut(packet.playerID,packet.playerName);
            //Clear the sent requests cache
            sentIDRequests.remove(packet.playerName);
            sentNameRequest.remove(packet.playerID);
        } catch (Exception ignored) {}
    }

}
