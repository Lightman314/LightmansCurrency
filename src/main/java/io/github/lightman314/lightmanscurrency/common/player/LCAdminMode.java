package io.github.lightman314.lightmanscurrency.common.player;

import io.github.lightman314.lightmanscurrency.network.message.command.SPacketSyncAdminList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LCAdminMode {

    private static final List<UUID> adminPlayers = new ArrayList<>();

    public static boolean isAdminPlayer(@Nullable Player player) { return player != null && adminPlayers.contains(player.getUUID()) && (player.hasPermissions(2)); }

    public static void ToggleAdminPlayer(ServerPlayer player) {
        UUID playerID = player.getUUID();
        if(adminPlayers.contains(playerID))
            adminPlayers.remove(playerID);
        else
            adminPlayers.add(playerID);
        sendSyncPacketToAll();
    }

    public static void sendSyncPacket(Player target) { new SPacketSyncAdminList(adminPlayers).sendTo(target); }
    public static void sendSyncPacketToAll() { new SPacketSyncAdminList(adminPlayers).sendToAll(); }

    public static void loadAdminPlayers(List<UUID> serverAdminList) { adminPlayers.clear(); adminPlayers.addAll(serverAdminList); }

}
