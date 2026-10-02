package io.github.lightman314.lightmanscurrency.client.features.admin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import org.jetbrains.annotations.ApiStatus;

@EventBusSubscriber
public class ClientAdminMode {

    @ApiStatus.Internal
    public static boolean isClientAdmin = false;

    public static boolean isAdmin(Player player) { return player.getUUID().equals(Minecraft.getInstance().player.getUUID()) && isClientAdmin; }

    @SubscribeEvent
    private static void onServerLeave(ClientPlayerNetworkEvent.LoggingOut event) {
        isClientAdmin = false;
    }

}