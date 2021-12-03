package io.github.lightman314.lightmanscurrency.tileentity;

import java.util.List;

import net.minecraft.entity.player.PlayerEntity;

public interface IPermissions {

	public boolean isOwner(PlayerEntity player);
	public boolean hasPermissions(PlayerEntity player);
	public List<String> getAllies();
	public void markAlliesDirty();
	
}
