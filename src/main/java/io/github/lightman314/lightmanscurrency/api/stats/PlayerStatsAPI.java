package io.github.lightman314.lightmanscurrency.api.stats;

import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.stats.interfaces.StatHolder;
import net.minecraft.world.entity.player.Player;

public interface PlayerStatsAPI {

    default StatHolder getPlayerStats(Player player) { return getPlayerStats(PlayerReference.of(player),ISidedContext.wrap(player)); }
    StatHolder getPlayerStats(PlayerReference player,ISidedContext context);
    default long startTrackingPlayerStats(Player player) { return this.startTrackingPlayerStats(PlayerReference.of(player),player); }
    long startTrackingPlayerStats(PlayerReference player, Player viewer);
    void endPlayerStatTracking(long statKey);

}
