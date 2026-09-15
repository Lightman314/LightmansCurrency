package io.github.lightman314.lightmanscurrency.api.ownership.interfaces;

import net.minecraft.world.entity.player.Player;

public interface IOwnable {
    IOwnerHolder getOwner();
    default boolean canBreak(Player player) { return this.getOwner().isMember(player); }
}