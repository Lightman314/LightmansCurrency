package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.tax_collector;

import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.TaxCollectorClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.tabs.InfoTab;
import io.github.lightman314.lightmanscurrency.common.taxes.ITaxable;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.common.taxes.data.TaxStats;
import io.github.lightman314.lightmanscurrency.common.taxes.reference.TaxableReference;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class InfoClientTab extends TaxCollectorClientTab<InfoTab> {

    public InfoClientTab(Object screen, InfoTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconAndButtonUtil.ICON_TRADER; }

    @Nullable
    @Override
    public Component getTooltip() { return EasyText.translatable("gui.lightmanscurrency.tax_collector.info"); }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {
        Component clearLabel = EasyText.translatable("gui.lightmanscurrency.tax_collector.stats.clear");
        int buttonWidth = this.getFont().width(clearLabel) + 6;
        this.addChild(new EasyTextButton(screenArea.pos.offset(screenArea.width - buttonWidth - 8, 15), buttonWidth, 12, clearLabel, this.commonTab::ClearInfoCache)
                .withAddons(EasyAddonHelper.visibleCheck(this::canClearStats)));
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        gui.drawString(this.getTooltip(), 8, 6, 0x404040);

        TaxEntry entry = this.getEntry();
        if(entry != null)
        {
            TaxStats stats = entry.stats;

            //Total Money Collected
            gui.drawString(EasyText.translatable("gui.lightmanscurrency.tax_collector.stats.total_collected"), 10, 35, 0x404040);
            gui.drawString(stats.getTotalCollected().getComponent("0"), 10, 45, 0x404040);

            //Unique Machines Taxed
            gui.drawString(EasyText.translatable("gui.lightmanscurrency.tax_collector.stats.unique_taxables", stats.getUniqueTaxableCount()), 10, 65, 0x404040);
            //Most Taxed
            TaxableReference mostTaxed = stats.getMostTaxed();
            gui.drawString(EasyText.translatable("gui.lightmanscurrency.tax_collector.stats.most_taxed.label"), 10, 85, 0x404040);
            if(mostTaxed != null)
            {
                ITaxable taxable = mostTaxed.getTaxable(true);
                if(taxable != null)
                {
                    gui.drawString(taxable.getName(), 10, 95, 0x404040);
                    gui.drawString(EasyText.translatable("gui.lightmanscurrency.tax_collector.stats.most_taxed.format", stats.getMostTaxedCount()), 10, 104, 0x404040);
                }
            }
        }

    }

    private boolean canClearStats() { return this.commonTab.CanClearCache(this.getEntry()); }

}
