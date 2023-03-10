package io.github.lightman314.lightmanscurrency.common.blockentity.interfaces;

import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import net.minecraft.entity.player.PlayerEntity;

public interface IPermissions {

	default boolean hasPermission(PlayerEntity player, String permission) { return this.getPermissionLevel(player, permission) > 0; }
	default boolean hasPermission(PlayerReference player, String permission) { return this.getPermissionLevel(player, permission) > 0; }
	int getPermissionLevel(PlayerEntity player, String permission);
	int getPermissionLevel(PlayerReference player, String permission);
	
}
