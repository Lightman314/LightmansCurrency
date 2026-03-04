package io.github.lightman314.lightmanscurrency.api.ownership.listing;

import net.minecraft.world.entity.player.Player;

import java.util.List;

public interface IPotentialOwnerProvider {

    List<PotentialOwner> collectPotentialOwners(Player player);

}
