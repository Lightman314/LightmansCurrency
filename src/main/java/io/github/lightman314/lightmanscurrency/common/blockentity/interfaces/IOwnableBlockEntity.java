package io.github.lightman314.lightmanscurrency.common.blockentity.interfaces;

import net.minecraft.world.entity.player.Player;

public interface IOwnableBlockEntity {

	boolean canBreak(Player entity);
	
}