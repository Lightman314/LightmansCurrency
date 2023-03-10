package io.github.lightman314.lightmanscurrency.common.blockentity.interfaces;

import net.minecraft.entity.player.PlayerEntity;

public interface IOwnableBlockEntity {

	boolean canBreak(PlayerEntity entity);
	
}
