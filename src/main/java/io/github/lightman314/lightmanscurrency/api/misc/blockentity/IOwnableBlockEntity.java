package io.github.lightman314.lightmanscurrency.api.misc.blockentity;

import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public interface IOwnableBlockEntity {

	boolean canBreak(@Nullable Player entity);
	
}
