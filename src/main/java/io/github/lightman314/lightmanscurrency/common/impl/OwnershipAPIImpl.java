package io.github.lightman314.lightmanscurrency.common.impl;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerType;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnershipAPI;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.PotentialOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.IPotentialOwnerProvider;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OwnershipAPIImpl extends OwnershipAPI {

    private final Map<ResourceLocation,OwnerType> registeredOwnerTypes = new HashMap<>();
    private final List<IPotentialOwnerProvider> potentialOwnerProviders = new ArrayList<>();

    public OwnershipAPIImpl() {}

    @Override
    public void registerOwnerType(OwnerType type) {
        if(this.registeredOwnerTypes.containsKey(type.getID()))
        {
            OwnerType existing = this.registeredOwnerTypes.get(type.getID());
            if(existing == type)
                LightmansCurrency.LogWarning("Owner Type " + type.getID() + " was registered twice!");
            else
                LightmansCurrency.LogError("Tried to register Owner Type " + type.getID() + ", but another type has already been registered under that id!");
            return;
        }
        this.registeredOwnerTypes.put(type.getID(),type);
        LightmansCurrency.LogDebug("Registered Owner Type: " + type.getID());
    }

    @Nullable
    @Override
    public OwnerType getOwnerType(ResourceLocation id) { return this.registeredOwnerTypes.get(id); }

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
