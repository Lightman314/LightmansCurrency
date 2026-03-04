package io.github.lightman314.lightmanscurrency.api.ejection;

import io.github.lightman314.lightmanscurrency.common.impl.SafeEjectionAPIImpl;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

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

    public abstract void handleEjection(@Nullable Level level, BlockPos pos, EjectionData data);

}
