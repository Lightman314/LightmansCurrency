package io.github.lightman314.lightmanscurrency.api.ownership;

import io.github.lightman314.lightmanscurrency.api.ownership.listing.PotentialOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.IPotentialOwnerProvider;
import io.github.lightman314.lightmanscurrency.common.impl.OwnershipAPIImpl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class OwnershipAPI {

    public static final OwnershipAPI API = OwnershipAPIImpl.INSTANCE;

    public abstract void registerOwnerType(@Nonnull OwnerType type);
    @Nullable
    public abstract OwnerType getOwnerType(@Nonnull ResourceLocation id);

    public abstract void registerPotentialOwnerProvider(@Nonnull IPotentialOwnerProvider provider);

    public abstract List<PotentialOwner> getPotentialOwners(@Nonnull Player player);

}
