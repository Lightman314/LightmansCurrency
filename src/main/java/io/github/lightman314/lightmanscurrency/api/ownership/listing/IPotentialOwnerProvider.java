package io.github.lightman314.lightmanscurrency.api.ownership.listing;

import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.List;

public interface IPotentialOwnerProvider {

    @Nonnull
    List<PotentialOwner> collectPotentialOwners(@Nonnull Player player);

}
