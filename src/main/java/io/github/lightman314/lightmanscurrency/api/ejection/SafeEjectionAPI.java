package io.github.lightman314.lightmanscurrency.api.ejection;

import io.github.lightman314.lightmanscurrency.common.impl.SafeEjectionAPIImpl;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class SafeEjectionAPI {

    private static SafeEjectionAPI api = null;
    @Nonnull
    public static SafeEjectionAPI getApi() {
        if(api == null)
            api = SafeEjectionAPIImpl.INSTANCE;
        return api;
    }

    @Nonnull
    public abstract List<EjectionData> getAllData(@Nonnull IClientTracker context);

    @Nonnull
    public abstract List<EjectionData> getDataForPlayer(@Nonnull Player player);

    @Nullable
    public abstract EjectionData parseData(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup);

    public abstract void handleEjection(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull EjectionData data);

}
