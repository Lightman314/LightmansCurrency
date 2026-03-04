package io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin.UpgradesTab;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;

import javax.annotation.Nullable;

public class UpgradesClientTab extends TraderStorageClientTab<UpgradesTab> {

    public UpgradesClientTab(Object screen, UpgradesTab commonTab) { super(screen, commonTab); }

    @Override
    public IconData getIcon() { return IconUtil.ICON_STORAGE; }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TRADER_UPGRADES.get(); }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) { }

    @Override
    public void renderBG(EasyGuiGraphics gui) {
        for(Slot slot : this.commonTab.getSlots())
            gui.renderSlot(this.screen,slot);
    }

}
