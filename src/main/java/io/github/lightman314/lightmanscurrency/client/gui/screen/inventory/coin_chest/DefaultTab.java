package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_chest;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.CoinChestScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestUpgradeData;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class DefaultTab extends CoinChestTab {

    public DefaultTab(CoinChestScreen screen) { super(screen); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(ModBlocks.COIN_CHEST); }

    @Override
    public Component getTooltip() { return this.menu.be.getDisplayName(); }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {
        for(int i = 0; i < CoinChestBlockEntity.UPGRADE_SIZE; ++i)
        {
            final int index = i;
            this.addChild(IconAndButtonUtil.toggleButton(screenArea.pos.offset(152 - 9, 20 + (i * 18)), b -> this.toggleUpgradeActive(index), () -> this.upgradeActive(index))
                    .withAddons(EasyAddonHelper.visibleCheck(() -> this.showToggle(index))));
        }
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) { }

    @Nonnull
    private CoinChestUpgradeData dataForSlot(int slot)
    {
        return this.menu.be.getChestUpgradeForSlot(slot);
    }

    private boolean showToggle(int index)
    {
        CoinChestUpgradeData data = this.menu.be.getChestUpgradeForSlot(index);
        return data.notNull() && !data.upgrade.alwayActive();
    }

    private boolean upgradeActive(int index)
    {
        return this.menu.be.getChestUpgradeForSlot(index).isActive();
    }

    private void toggleUpgradeActive(int index)
    {
        CoinChestUpgradeData data = this.menu.be.getChestUpgradeForSlot(index);
        this.menu.SendMessageToServer(this.builder().setBoolean("SetUpgradeActive",!data.isActive()).setInt("Slot",index));
    }

}
