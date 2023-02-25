package io.github.lightman314.lightmanscurrency.common.blockentity.interfaces;

import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import net.minecraft.world.entity.player.Player;

public interface IPermissions {

	public default boolean hasPermission(Player player, String permission) { return this.getPermissionLevel(player, permission) > 0; }
	public default boolean hasPermission(PlayerReference player, String permission) { return this.getPermissionLevel(player, permission) > 0; }
	public int getPermissionLevel(Player player, String permission);
	public int getPermissionLevel(PlayerReference player, String permission);
	
}
