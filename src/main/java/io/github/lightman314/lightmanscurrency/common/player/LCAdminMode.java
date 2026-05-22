package io.github.lightman314.lightmanscurrency.common.player;

import io.github.lightman314.lightmanscurrency.client.ClientLCAdminMode;
import io.github.lightman314.lightmanscurrency.network.message.command.SPacketSyncAdminList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber
public class LCAdminMode {

    private static final List<UUID> adminPlayers = new ArrayList<>();

    public static boolean isAdminPlayer(@Nullable Player player) {
        if(player == null)
            return false;
        //Get the data from the client-side data
        if(player.level().isClientSide)
            return ClientLCAdminMode.isAdmin(player);
        return adminPlayers.contains(player.getUUID()) && (player.hasPermissions(2));
    }

    public static void ToggleAdminPlayer(ServerPlayer player) {
        UUID playerID = player.getUUID();
        if(adminPlayers.contains(playerID))
            adminPlayers.remove(playerID);
        else
            adminPlayers.add(playerID);
        //Only send the sync packet to the relevant player, as nobody else needs to know this information
        sendSyncPacket(player);
    }

    public static void sendSyncPacket(ServerPlayer target) { new SPacketSyncAdminList(adminPlayers.contains(target.getUUID())).sendTo(target); }

    public static void loadAdminPlayers(List<UUID> serverAdminList) { adminPlayers.clear(); adminPlayers.addAll(serverAdminList); }

    @SubscribeEvent
    private static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(event.getEntity() instanceof ServerPlayer sp)
            sendSyncPacket(sp);
    }

}
