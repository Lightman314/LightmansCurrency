package io.github.lightman314.lightmanscurrency.api.ownership.listing;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface PotentialOwnerProvider {

    void collectPotentialOwners(Player player,ISidedContext context,Consumer<PotentialOwner> builder);

    static List<PotentialOwner> getPotentialOwners(Player player) {
        List<PotentialOwner> result = new ArrayList<>();
        ISidedContext context = ISidedContext.wrap(player);
        for(PotentialOwnerProvider provider : LCRegistries.Ownership.POTENTIAL_OWNER)
            provider.collectPotentialOwners(player,context,p -> result.add(p.setSidedContext(context)));
        return result;
    }

}