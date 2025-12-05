package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.tax_collector;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.TaxCollectorClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.tabs.AdminTab;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public class AdminSettingsClientTab extends TaxCollectorClientTab<AdminTab> {

    public AdminSettingsClientTab(Object screen, AdminTab commonTab) { super(screen, commonTab); }

    @Override
    public IconData getIcon() { return ItemIcon.ofItem(Items.COMMAND_BLOCK); }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TAX_COLLECTOR_ADMIN.get(); }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {
        //Admin Options
        this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(8,32))
                .pressAction(() -> this.commonTab.SetForceAcceptance(!this.getCurrentForceAcceptance()))
                .sprite(SpriteUtil.createCheckbox(this::getCurrentForceAcceptance))
                .build());
        this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(8,42))
                .pressAction(() -> this.commonTab.SetInfiniteRange(!this.getCurrentInfiniteRange()))
                .sprite(SpriteUtil.createCheckbox(this::getCurrentInfiniteRange))
                .build());
    }

    @Override
    public void renderBG(EasyGuiGraphics gui) {

        //Title
        gui.drawString(this.getTooltip(), 8, 6, 0x404040);
        //Force Acceptance Label
        gui.drawString(LCText.GUI_TAX_COLLECTOR_FORCE_ACCEPTANCE.get(), 20, 34, 0x404040);
        //Infinite Range Label
        gui.drawString(LCText.GUI_TAX_COLLECTOR_INFINITE_RANGE.get(), 20, 44, 0x404040);

    }

    private boolean getCurrentForceAcceptance() { TaxEntry entry = this.getEntry(); return entry != null && entry.forcesAcceptance(); }
    private boolean getCurrentInfiniteRange() { TaxEntry entry = this.getEntry(); return entry != null && entry.isInfiniteRange(); }
}