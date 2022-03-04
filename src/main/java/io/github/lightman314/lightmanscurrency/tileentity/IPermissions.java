package io.github.lightman314.lightmanscurrency.tileentity;

import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import net.minecraft.entity.player.PlayerEntity;

public interface IPermissions {

	public default boolean hasPermission(PlayerEntity player, String permission) { return this.getPermissionLevel(player, permission) > 0; }
	public default boolean hasPermission(PlayerReference player, String permission) { return this.getPermissionLevel(player, permission) > 0; }
	public int getPermissionLevel(PlayerEntity player, String permission);
	public int getPermissionLevel(PlayerReference player, String permission);
	
}
