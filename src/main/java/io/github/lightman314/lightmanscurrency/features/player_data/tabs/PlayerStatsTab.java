package io.github.lightman314.lightmanscurrency.features.player_data.tabs;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.features.player_data.PlayerDataMenu;
import io.github.lightman314.lightmanscurrency.features.player_data.PlayerDataTab;
import net.minecraft.resources.Identifier;

public class PlayerStatsTab extends PlayerDataTab {

    public static final Identifier CLIENT_KEY = LCApi.id("player_stats");

    private final long trackingKey;

    public static final TextEntry NAME = TextEntry.tooltip(LCApi.MODID,"player_data.stats");

    public PlayerStatsTab(PlayerDataMenu menu) {
        super(menu);
        this.trackingKey = LCApi.getPlayerStatsAPI().startTrackingPlayerStats(this.getPlayer());
    }

    @Override
    public void onMenuClosed() {
        LCApi.getPlayerStatsAPI().endPlayerStatTracking(this.trackingKey);
    }

    //Other than the stat tracking request, nothing particularly fancy goes here...
    //I'm not going to support clearing player stats for the time being

    @Override
    public Identifier getClientTabKey() { return CLIENT_KEY; }

}