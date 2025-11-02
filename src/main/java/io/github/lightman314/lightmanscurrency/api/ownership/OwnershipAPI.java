package io.github.lightman314.lightmanscurrency.api.ownership;

import io.github.lightman314.lightmanscurrency.api.ownership.listing.PotentialOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.IPotentialOwnerProvider;
import io.github.lightman314.lightmanscurrency.common.impl.OwnershipAPIImpl;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class OwnershipAPI {

    private static OwnershipAPI instance;
    public static OwnershipAPI getApi()
    {
        if(instance == null)
            instance = new OwnershipAPIImpl();
        return instance;
    }

    protected OwnershipAPI() { if(instance != null)  throw new IllegalCallerException("Cannot create a new OwnershipAPI instance as one is already present!"); }

    public abstract void registerOwnerType(OwnerType type);
    @Nullable
    public abstract OwnerType getOwnerType(ResourceLocation id);

    public abstract void registerPotentialOwnerProvider(IPotentialOwnerProvider provider);

    public abstract List<PotentialOwner> getPotentialOwners(Player player);

}
