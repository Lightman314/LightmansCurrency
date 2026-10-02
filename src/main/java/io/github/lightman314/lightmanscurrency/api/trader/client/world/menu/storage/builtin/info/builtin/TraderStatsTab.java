package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.info.builtin;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.StatDisplayWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.FancyButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.TextButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.VerticalScrollBar;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.stats.interfaces.StatHolder;
import io.github.lightman314.lightmanscurrency.api.stats.interfaces.StatViewer;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.info.InfoClientSubTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.info.InfoClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.TraderStatsNode;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class TraderStatsTab extends InfoClientSubTab {

    public TraderStatsTab(InfoClientTab tab) { super(tab); }

    private StatDisplayWidget statDisplay = null;

    @Override
    public IconData getIcon() { return SpriteIcon.of(LCApi.id("icon/price_fluctuation")); }
    @Override
    public Component getName() { return TraderStatsNode.TOOLTIP_TRADER_STATS.get(); }

    @Override
    protected void initialize(ScreenArea area,FancyPacketMap message) {
        //Clear Stats Listener
        this.addChild(TextButton.builder()
                .atPos( area.pos.offset(10,10))
                .ofWidth(area.width - 20)
                .withText(StatHolder.BUTTON_CLEAR_STATS)
                .visible(this::canClearStats)
                .onPress(this::clearStats)
                .build());

        //Stat Display
        this.statDisplay = this.addChild(StatDisplayWidget.builder()
                .atPos(area.pos.offset(20,37))
                .ofSize(area.width - 40,100)
                .forStats(this::getStats)
                .withOldWidget(this.statDisplay)
                .build());
        this.addChild(VerticalScrollBar.builder(this.statDisplay)
                .rightOf(this.statDisplay)
                .build());

    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) { }

    @Nullable
    private StatViewer getStats() { return this.getNodeValue(TraderStatsNode.TYPE,TraderStatsNode::getStats); }

    private boolean canClearStats() { return this.getPermission(LCPermissions.VIEW_LOGS).hasHigherPermission(); }

    private void clearStats(FancyButton button,MouseButtonEvent event) {
        this.sendSettingRequest(TraderStatsNode.TYPE,FancyPacketMap.map().setBoolean("clearStats",event.hasShiftDown()));
    }
}
