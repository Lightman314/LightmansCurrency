package io.github.lightman314.lightmanscurrency.features.admin;

import io.github.lightman314.lightmanscurrency.client.features.admin.ClientAdminMode;
import io.github.lightman314.lightmanscurrency.network.message.system.SPacketSyncAdminStatus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.*;

@EventBusSubscriber
public final class AdminMode {

    private AdminMode() {}

    private static final Set<UUID> admins = new HashSet<>();

    public static boolean isAdmin(Player player) {
        if(player.level() == null)
            return false;
        //Deffer to the client admin data if the player is in a client-side level
        if(player.level().isClientSide())
            return ClientAdminMode.isAdmin(player);
        return admins.contains(player.getUUID());
    }

    public static boolean toggleAdmin(ServerPlayer player) {
        UUID id = player.getUUID();
        boolean isAdmin;
        if(admins.contains(id)) {
            admins.remove(id);
            isAdmin = false;
        }
        else {
            admins.add(id);
            isAdmin = true;
        }
        //Sync with the relevant player
        new SPacketSyncAdminStatus(isAdmin).sendTo(player);
        return isAdmin;
    }

    @SubscribeEvent
    private static void onServerStopped(ServerStoppedEvent event) {
        //Clear the admins when server is stopped
        admins.clear();
    }

    @SubscribeEvent
    private static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        //Remove the admin flag for players when they leave
        admins.remove(event.getEntity().getUUID());
    }

}