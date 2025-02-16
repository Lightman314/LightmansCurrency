package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.UpgradesTab;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpgradesClientTab extends TraderStorageClientTab<UpgradesTab> {

    public UpgradesClientTab(Object screen, UpgradesTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(Items.CHEST); }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TRADER_UPGRADES.get(); }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) { }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {
        for(Slot slot : this.commonTab.getSlots())
            gui.renderSlot(this.screen,slot);
    }

}