package io.github.lightman314.lightmanscurrency.features.api_impl;

import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.stats.PlayerStatsAPI;
import io.github.lightman314.lightmanscurrency.api.stats.interfaces.StatHolder;
import io.github.lightman314.lightmanscurrency.features.api_impl.data.PlayerStatsDataCache;
import net.minecraft.world.entity.player.Player;

public final class PlayerStatsAPIImpl implements PlayerStatsAPI {

    private PlayerStatsAPIImpl() {}

    public static final PlayerStatsAPIImpl INSTANCE = new PlayerStatsAPIImpl();

    @Override
    public StatHolder getPlayerStats(PlayerReference player,ISidedContext context) {
        return PlayerStatsDataCache.TYPE.get(context).getOrCreatePlayerStats(player);
    }

    @Override
    public void startTrackingPlayerStats(PlayerReference player,Player viewer) {
        if(viewer.level().isClientSide())
            return;
        PlayerStatsDataCache.TYPE.get(ISidedContext.LOGICAL_SERVER).requestStatTracking(player,viewer);
    }

    @Override
    public void endPlayerStatTracking(PlayerReference player,Player viewer) {
        if(viewer.level().isClientSide())
            return;
        PlayerStatsDataCache.TYPE.get(ISidedContext.LOGICAL_SERVER).endStatTracking(player,viewer);
    }

}