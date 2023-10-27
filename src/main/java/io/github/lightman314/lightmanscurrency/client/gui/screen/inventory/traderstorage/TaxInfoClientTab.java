package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage;

import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.taxes.ITaxInfoInteractable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.taxes.TaxInfoWidget;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TaxInfoTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
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
    public IconData getIcon() { return IconAndButtonUtil.ICON_TAXES; }

    @Nullable
    @Override
    public Component getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.trader.tax_info"); }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        for(int i = 0; i < DISPLAY_ENTRIES; ++i)
        {
            final int index = i;
            this.addChild(new TaxInfoWidget(screenArea.pos.offset(TraderStorageMenu.SLOT_OFFSET, (TaxInfoWidget.HEIGHT * i) + 20), () -> this.getEntryOfIndex(index), this));
        }

        this.addChild(new ScrollListener(screenArea.ofSize(screenArea.width, TaxInfoWidget.HEIGHT * DISPLAY_ENTRIES).atPosition(screenArea.pos.offset(0,10)), this));
        this.addChild(new ScrollBarWidget(screenArea.pos.offset(TraderStorageMenu.SLOT_OFFSET + TaxInfoWidget.WIDTH, 20), TaxInfoWidget.HEIGHT * DISPLAY_ENTRIES, this));

        //Hide Coin Slots
        this.menu.SetCoinSlotsActive(false);
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {
        TraderData trader = this.getTrader();
        if(trader != null)
        {
            TextRenderUtil.drawCenteredText(gui, EasyText.translatable("tooltip.lightmanscurrency.trader.tax_info.total_rate", trader.getTotalTaxPercentage()), this.screen.getXSize() / 2, 6, 0x404040);
            if(trader.getPossibleTaxes().size() == 0)
                TextRenderUtil.drawCenteredMultilineText(gui, EasyText.translatable("tooltip.lightmanscurrency.trader.tax_info.no_tax_collectors"), 10, this.screen.getXSize() - 20, 60, 0x404040);
        }
    }

    private List<TaxEntry> getAllEntries() {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            return trader.getPossibleTaxes();
        return new ArrayList<>();
    }

    @Nullable
    private TaxEntry getEntryOfIndex(int index)
    {
        List<TaxEntry> entries = this.getAllEntries();
        index += this.scroll;
        if(index < 0 || index >= entries.size())
            return null;
        return entries.get(index);
    }

    @Override
    protected void closeAction() {
        //Hide Coin Slots
        this.menu.SetCoinSlotsActive(true);
    }

    @Override
    public void tick() { this.validateScroll(); }

    @Nullable
    @Override
    public TraderData getTrader() { return this.menu.getTrader(); }

    @Override
    public boolean canPlayerForceIgnore() { return LCAdminMode.isAdminPlayer(this.menu.player); }

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