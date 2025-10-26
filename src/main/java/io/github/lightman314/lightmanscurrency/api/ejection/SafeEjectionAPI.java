package io.github.lightman314.lightmanscurrency.api.ejection;

import io.github.lightman314.lightmanscurrency.common.impl.SafeEjectionAPIImpl;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SafeEjectionAPI {

    private static SafeEjectionAPI instance = null;
    public static SafeEjectionAPI getApi() {
        if(instance == null)
            instance = new SafeEjectionAPIImpl();
        return instance;
    }

    protected SafeEjectionAPI() { if(instance != null)  throw new IllegalCallerException("Cannot create a new SafeEjectionAPI instance as one is already present!"); }

    public final List<EjectionData> getAllData(IClientTracker context) { return this.getAllData(context.isClient()); }
    public abstract List<EjectionData> getAllData(boolean isClient);

    public abstract List<EjectionData> getDataForPlayer(Player player);
    @Nullable
    public abstract EjectionData parseData(CompoundTag tag, HolderLookup.Provider lookup);
    public abstract void handleEjection(Level level, BlockPos pos, EjectionData data);

}
