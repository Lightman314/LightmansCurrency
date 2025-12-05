package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.tax_collector;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.TaxCollectorClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.tabs.ServerSettingsTab;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public class ServerSettingsClientTab extends TaxCollectorClientTab<ServerSettingsTab> {

    public ServerSettingsClientTab(Object screen, ServerSettingsTab commonTab) { super(screen, commonTab); }

    @Override
    public IconData getIcon() { return ItemIcon.ofItem(Items.REPEATING_COMMAND_BLOCK); }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TAX_COLLECTOR_SERVER_SETTINGS.get(); }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {
        //Server Tax Collector Options
        this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(8,32))
                .pressAction(() -> this.commonTab.SetOnlyTargetsNetworkObjects(!this.getCurrentTargetsNetworkObjects()))
                .sprite(SpriteUtil.createCheckbox(this::getCurrentTargetsNetworkObjects))
                .build());
    }

    @Override
    public void renderBG(EasyGuiGraphics gui) {

        //Title
        gui.drawString(this.getTooltip(), 8, 6, 0x404040);
        //Only Tax Network Objects Label
        gui.drawString(LCText.GUI_TAX_COLLECTOR_ONLY_TARGET_NETWORK.get(), 20, 34, 0x404040);

    }

    private boolean getCurrentTargetsNetworkObjects() { TaxEntry entry = this.getEntry(); return entry != null && entry.isOnlyTargetingNetwork(); }
}