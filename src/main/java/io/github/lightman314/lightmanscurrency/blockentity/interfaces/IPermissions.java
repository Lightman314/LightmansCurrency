package io.github.lightman314.lightmanscurrency.blockentity.interfaces;

import net.minecraft.world.entity.player.Player;

public interface IPermissions {

	public default boolean hasPermission(Player player, String permission) { return this.getPermissionLevel(player, permission) > 0; }
	public int getPermissionLevel(Player player, String permission);
	
}
