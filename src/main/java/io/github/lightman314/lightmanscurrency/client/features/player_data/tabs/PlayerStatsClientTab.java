package io.github.lightman314.lightmanscurrency.client.features.player_data.tabs;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.StatDisplayWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.VerticalScrollBar;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.stats.interfaces.StatViewer;
import io.github.lightman314.lightmanscurrency.client.features.player_data.PlayerDataClientTab;
import io.github.lightman314.lightmanscurrency.client.features.player_data.PlayerDataScreen;
import io.github.lightman314.lightmanscurrency.features.player_data.PlayerDataMenu;
import io.github.lightman314.lightmanscurrency.features.player_data.PlayerDataTab;
import io.github.lightman314.lightmanscurrency.features.player_data.tabs.PlayerStatsTab;
import net.minecraft.network.chat.Component;

public class PlayerStatsClientTab extends PlayerDataClientTab<PlayerStatsTab> {

    public static final TabBuilder<PlayerDataMenu,PlayerStatsTab, PlayerDataTab,PlayerDataScreen> BUILDER = PlayerStatsClientTab::new;

    private final IconData icon;

    protected PlayerStatsClientTab(PlayerDataMenu menu,PlayerStatsTab commonTab,PlayerDataScreen screen) {
        super(menu, commonTab, screen);
        this.icon = ItemIcon.of(PlayerReference.of(this.getPlayer()).getSkull());
    }

    private StatDisplayWidget statDisplay = null;

    @Override
    public IconData getIcon() { return this.icon; }

    @Override
    public Component getName() { return PlayerStatsTab.NAME.get(); }

    @Override
    protected void initialize(ScreenArea area, FancyPacketMap message) {
        this.statDisplay = this.addChild(StatDisplayWidget.builder()
                .atPos(area.pos.offset(15,15))
                .ofSize(area.width - 30,area.height - 30)
                .forStats(this::getStats)
                .withOldWidget(this.statDisplay)
                .build());

        this.addChild(VerticalScrollBar.builder(this.statDisplay)
                .rightOf(this.statDisplay)
                .build());

    }

    private StatViewer getStats() { return LCApi.getPlayerStatsAPI().getPlayerStats(this.getPlayer()); }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        //Render the title
        gui.text(PlayerStatsTab.NAME.get(),8,6,0xFF404040,false);
    }

}