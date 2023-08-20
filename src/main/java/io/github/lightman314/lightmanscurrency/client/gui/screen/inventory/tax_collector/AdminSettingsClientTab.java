package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.tax_collector;

import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.TaxCollectorClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.tabs.AdminTab;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class AdminSettingsClientTab extends TaxCollectorClientTab<AdminTab> {

    public AdminSettingsClientTab(Object screen, AdminTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(Items.COMMAND_BLOCK); }

    @Nullable
    @Override
    public Component getTooltip() { return EasyText.translatable("gui.lightmanscurrency.tax_collector.admin"); }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {
        //Admin Options
        this.addChild(IconAndButtonUtil.checkmarkButton(screenArea.pos.offset(8, 32), b -> this.commonTab.SetForceAcceptance(!this.getCurrentForceAcceptance()), this::getCurrentForceAcceptance));
        this.addChild(IconAndButtonUtil.checkmarkButton(screenArea.pos.offset(8, 42), b -> this.commonTab.SetInfiniteRange(!this.getCurrentInfiniteRange()), this::getCurrentInfiniteRange));
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        //Title
        gui.drawString(this.getTooltip(), 8, 6, 0x404040);
        //Force Acceptance Label
        gui.drawString(EasyText.translatable("gui.lightmanscurrency.tax_collector.force_acceptance"), 20, 34, 0x404040);
        //Infinite Range Label
        gui.drawString(EasyText.translatable("gui.lightmanscurrency.tax_collector.infinite_range"), 20, 44, 0x404040);

    }

    private boolean getCurrentForceAcceptance() { TaxEntry entry = this.getEntry(); return entry != null && entry.forcesAcceptance(); }
    private boolean getCurrentInfiniteRange() { TaxEntry entry = this.getEntry(); return entry != null && entry.isInfiniteRange(); }
}
