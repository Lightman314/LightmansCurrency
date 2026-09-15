package io.github.lightman314.lightmanscurrency.client.features.admin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

public class ClientAdminMode {

    @ApiStatus.Internal
    public static boolean isClientAdmin = false;

    public static boolean isAdmin(Player player) { return player.getUUID().equals(Minecraft.getInstance().player.getUUID()) && isClientAdmin; }

}