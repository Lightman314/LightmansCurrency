package io.github.lightman314.lightmanscurrency.common.impl;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnershipAPI;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.PotentialOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.IPotentialOwnerProvider;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class OwnershipAPIImpl extends OwnershipAPI {

    private final List<IPotentialOwnerProvider> potentialOwnerProviders = new ArrayList<>();

    public OwnershipAPIImpl() {}

    @Override
    public void registerPotentialOwnerProvider(IPotentialOwnerProvider provider) {
        if(this.potentialOwnerProviders.contains(provider))
        {
            LightmansCurrency.LogError("Tried to register Potential Owner Provider " + provider.getClass().getSimpleName() + " twice!");
            return;
        }
        this.potentialOwnerProviders.add(provider);
    }

    @Override
    public List<PotentialOwner> getPotentialOwners(Player player) {
        List<PotentialOwner> results = new ArrayList<>();
        for(IPotentialOwnerProvider provider : this.potentialOwnerProviders)
            results.addAll(provider.collectPotentialOwners(player));
        if(player.level().isClientSide)
            results.forEach(PotentialOwner::flagAsClient);
        return results;
    }

}
