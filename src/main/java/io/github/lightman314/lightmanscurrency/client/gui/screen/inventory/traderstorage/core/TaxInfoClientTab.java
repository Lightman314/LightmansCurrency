package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.core;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxCollector;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.taxes.ITaxInfoInteractable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.taxes.TaxInfoWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.core.TaxInfoTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TaxInfoClientTab extends TraderStorageClientTab<TaxInfoTab> implements IScrollable, ITaxInfoInteractable {

    public TaxInfoClientTab(Object screen, TaxInfoTab commonTab) { super(screen, commonTab); }

    public static final int DISPLAY_ENTRIES = 4;

    private int scroll = 0;

    @Nonnull
    @Override
    public IconData getIcon() { return IconUtil.ICON_TAXES; }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TRADER_TAXES.get(); }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        for(int i = 0; i < DISPLAY_ENTRIES; ++i)
        {
            final int index = i;
            this.addChild(TaxInfoWidget.builder()
                    .position(screenArea.pos.offset(TraderStorageMenu.SLOT_OFFSET,(TaxInfoWidget.HEIGHT * i) + 20))
                    .entry(() -> this.getEntryOfIndex(index))
                    .parent(this)
                    .build());
        }

        this.addChild(ScrollListener.builder()
                .area(screenArea.ofSize(screenArea.width,TaxInfoWidget.HEIGHT * DISPLAY_ENTRIES).offsetPosition(0,10))
                .listener(this)
                .build());
        this.addChild(ScrollBarWidget.builder()
                .position(screenArea.pos.offset(TraderStorageMenu.SLOT_OFFSET + TaxInfoWidget.WIDTH,20))
                .height(TaxInfoWidget.HEIGHT * DISPLAY_ENTRIES)
                .scrollable(this)
                .build());
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {
        TraderData trader = this.getTrader();
        if(trader != null)
        {
            TextRenderUtil.drawCenteredText(gui, LCText.GUI_TRADER_TAXES_TOTAL_RATE.get(trader.getTotalTaxPercentage()), this.screen.getXSize() / 2, 6, 0x404040);
            if(trader.getPossibleTaxes().isEmpty())
                TextRenderUtil.drawCenteredMultilineText(gui, LCText.GUI_TRADER_TAXES_NO_TAX_COLLECTORS.get(), 10, this.screen.getXSize() - 20, 60, 0x404040);
        }
    }

    private List<ITaxCollector> getAllEntries() {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            return trader.getPossibleTaxes();
        return new ArrayList<>();
    }

    @Nullable
    private ITaxCollector getEntryOfIndex(int index)
    {
        List<ITaxCollector> entries = this.getAllEntries();
        index += this.scroll;
        if(index < 0 || index >= entries.size())
            return null;
        return entries.get(index);
    }

    @Override
    public void tick() { this.validateScroll(); }

    @Nullable
    @Override
    public TraderData getTrader() { return this.menu.getTrader(); }

    @Override
    public boolean canPlayerForceIgnore() { return LCAdminMode.isAdminPlayer(this.menu.getPlayer()); }

    @Override
    public void AcceptTaxCollector(long taxEntryID) { this.commonTab.AcceptTaxes(taxEntryID); }

    @Override
    public void ForceIgnoreTaxCollector(long taxEntryID) { this.commonTab.ForceIgnoreTaxCollector(taxEntryID); }

    @Override
    public void PardonIgnoredTaxCollector(long taxEntryID) { this.commonTab.PardonIgnoredTaxCollector(taxEntryID); }

    @Override
    public int currentScroll() { return this.scroll; }

    @Override
    public void setScroll(int newScroll) { this.scroll = newScroll; }

    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(4, this.getAllEntries().size()); }
}
