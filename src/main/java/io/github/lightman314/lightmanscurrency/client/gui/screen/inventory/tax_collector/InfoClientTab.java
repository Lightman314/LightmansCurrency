package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.tax_collector;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.TaxCollectorClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.tabs.InfoTab;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxable;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.common.taxes.data.TaxStats;
import io.github.lightman314.lightmanscurrency.api.taxes.reference.TaxableReference;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class InfoClientTab extends TaxCollectorClientTab<InfoTab> {

    public InfoClientTab(Object screen, InfoTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconUtil.ICON_TRADER; }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TAX_COLLECTOR_INFO.get(); }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {
        Component clearLabel = LCText.BUTTON_TAX_COLLECTOR_STATS_CLEAR.get();
        int buttonWidth = this.getFont().width(clearLabel) + 6;
        this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(screenArea.width - buttonWidth - 8, 15))
                .size(buttonWidth,12)
                .text(clearLabel)
                .pressAction(this.commonTab::ClearInfoCache)
                .addon(EasyAddonHelper.visibleCheck(this::canClearStats))
                .build());
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        gui.drawString(this.getTooltip(), 8, 6, 0x404040);

        TaxEntry entry = this.getEntry();
        if(entry != null)
        {
            TaxStats stats = entry.stats;

            //Total Money Collected
            gui.drawString(LCText.GUI_TAX_COLLECTOR_STATS_TOTAL_COLLECTED.get(), 10, 35, 0x404040);
            gui.drawString(stats.getTotalCollected().getRandomValueText(), 10, 45, 0x404040);

            //Unique Machines Taxed
            gui.drawString(LCText.GUI_TAX_COLLECTOR_STATS_UNIQUE_TAXABLES.get(stats.getUniqueTaxableCount()), 10, 65, 0x404040);
            //Most Taxed
            TaxableReference mostTaxed = stats.getMostTaxed();
            gui.drawString(LCText.GUI_TAX_COLLECTOR_STATS_MOST_TAXED_LABEL.get(), 10, 85, 0x404040);
            if(mostTaxed != null)
            {
                ITaxable taxable = mostTaxed.getTaxable(true);
                if(taxable != null)
                {
                    gui.drawString(taxable.getName(), 10, 95, 0x404040);
                    gui.drawString(LCText.GUI_TAX_COLLECTOR_STATS_MOST_TAXED_FORMAT.get(stats.getMostTaxedCount()), 10, 104, 0x404040);
                }
            }
        }

    }

    private boolean canClearStats() { return this.commonTab.CanClearCache(this.getEntry()); }

}