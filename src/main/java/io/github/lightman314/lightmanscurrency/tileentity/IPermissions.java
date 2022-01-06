package io.github.lightman314.lightmanscurrency.tileentity;

import net.minecraft.entity.player.PlayerEntity;

public interface IPermissions {

	public default boolean hasPermission(PlayerEntity player, String permission) { return this.getPermissionLevel(player, permission) > 0; }
	public int getPermissionLevel(PlayerEntity player, String permission);
	
}
