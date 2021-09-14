package io.github.lightman314.lightmanscurrency.tileentity;

import net.minecraft.entity.player.PlayerEntity;

public interface IOwnableTileEntity {

	public boolean canBreak(PlayerEntity entity);
	
}
