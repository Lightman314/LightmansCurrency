package io.github.lightman314.lightmanscurrency.client;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.world.entity.player.Player;

public class ClientLCAdminMode {

    private static boolean isLocalPlayerAdmin = false;

    //Only true if querying the local player on the client, otherwise we don't need to know who else is in admin mode
    public static boolean isAdmin(Player player) { return LightmansCurrency.getProxy().isSelf(player) && isLocalPlayerAdmin; }

    public static void handleAdminSyncPacket(boolean isAdmin) { isLocalPlayerAdmin = isAdmin; }



}
