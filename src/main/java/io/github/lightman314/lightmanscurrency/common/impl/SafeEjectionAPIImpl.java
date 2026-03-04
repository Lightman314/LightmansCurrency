package io.github.lightman314.lightmanscurrency.common.impl;

import io.github.lightman314.lightmanscurrency.api.ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.api.ejection.SafeEjectionAPI;
import io.github.lightman314.lightmanscurrency.common.data.types.EjectionDataCache;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class SafeEjectionAPIImpl extends SafeEjectionAPI {

    public SafeEjectionAPIImpl() {}

    @Override
    public List<EjectionData> getAllData(boolean isClient) { return EjectionDataCache.TYPE.get(isClient).getData(); }

    @Override
    public List<EjectionData> getDataForPlayer(Player player) { return getAllData(IClientTracker.entityWrapper(player)).stream().filter(d -> d.canAccess(player) && !d.isEmpty()).toList(); }

    @Override
    public void handleEjection(@Nullable Level level, BlockPos pos, EjectionData data) {
        if(level != null && level.isClientSide)
            return;
        EjectionDataCache d = EjectionDataCache.TYPE.get(false);
        if(d != null)
            d.handleEjection(level,pos,data);
    }

}
