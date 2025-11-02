package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.misc;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.misc.UpgradesTab;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
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