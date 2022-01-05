package io.github.lightman314.lightmanscurrency.tileentity;

import net.minecraft.entity.player.PlayerEntity;

public interface IPermissions {

	public boolean hasPermission(PlayerEntity player, String permission);
	
}
