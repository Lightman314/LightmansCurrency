package io.github.lightman314.lightmanscurrency.blockentity.interfaces;

import java.util.List;

import net.minecraft.world.entity.player.Player;

public interface IPermissions {

	public boolean isOwner(Player player);
	public boolean hasPermissions(Player player);
	public List<String> getAllies();
	public void markAlliesDirty();
	
}
