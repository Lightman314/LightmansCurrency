package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStatsTab;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TraderStatsClientTab extends TraderStorageClientTab<TraderStatsTab> implements IScrollable {

    public TraderStatsClientTab(Object screen, TraderStatsTab commonTab) { super(screen, commonTab); }

    private static final int LINE_COUNT = 10;
    private static final int LINE_SIZE = 10;
    private static final int START_POS = 37;

    private int scroll = 0;
    private EasyButton buttonClear;

    @Nonnull
    @Override
    public IconData getIcon() { return IconUtil.ICON_PRICE_FLUCTUATION; }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TRADER_STATS.get(); }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {
        this.buttonClear = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(10,10))
                .width(screenArea.width - 20)
                .text(LCText.BUTTON_TRADER_STATS_CLEAR)
                .pressAction(() -> this.commonTab.clearStats(Screen.hasShiftDown()))
                .build());

        this.addChild(new ScrollBarWidget(screenArea.pos.offset(screenArea.width - 10 - ScrollBarWidget.WIDTH, START_POS), LINE_COUNT * LINE_SIZE, this));
        this.addChild(new ScrollListener(screenArea.ofSize(screenArea.width, START_POS + LINE_COUNT * LINE_SIZE), this));

    }

    @Override
    public void tick() {
        this.buttonClear.active = this.menu.hasPermission(Permissions.EDIT_SETTINGS);
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {
        this.validateScroll();
        int yPos = START_POS;
        List<MutableComponent> lines = this.getLines();
        if(lines.isEmpty())
        {
            TextRenderUtil.drawVerticallyCenteredMultilineText(gui, LCText.GUI_TRADER_STATS_EMPTY.get(), 10, this.screen.getXSize() - 20, yPos, LINE_COUNT * LINE_SIZE, 0x404040);
        }
        else
        {
            for(int i = this.scroll; i < this.scroll + LINE_COUNT && i < lines.size(); ++i)
            {
                gui.drawString(lines.get(i), 10, yPos, 0x404040);
                yPos += LINE_SIZE;
            }
        }
    }

    private List<MutableComponent> getLines()
    {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return new ArrayList<>();
        return trader.statTracker.getDisplayLines();
    }

    @Override
    public int currentScroll() { return this.scroll; }
    @Override
    public void setScroll(int newScroll) { this.scroll = newScroll; }
    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(LINE_COUNT,this.getLines().size()); }

}
