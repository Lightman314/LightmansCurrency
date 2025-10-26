package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_chest;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.CoinChestScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestUpgradeData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DefaultTab extends CoinChestTab {

    public DefaultTab(CoinChestScreen screen) { super(screen); }

    
    @Override
    public IconData getIcon() { return ItemIcon.ofItem(ModBlocks.COIN_CHEST); }

    @Override
    public Component getTooltip() { return this.menu.be.getDisplayName(); }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {
        for(int i = 0; i < CoinChestBlockEntity.UPGRADE_SIZE; ++i)
        {
            final int index = i;
            this.addChild(PlainButton.builder()
                    .position(screenArea.pos.offset(152 - 9, 20 + (i * 18)))
                    .pressAction(() -> this.toggleUpgradeActive(index))
                    .sprite(SpriteUtil.createColoredToggle(() -> this.upgradeActive(index)))
                    .addon(EasyAddonHelper.visibleCheck(() -> this.showToggle(index)))
                    .build());
        }
    }

    @Override
    public void renderBG(EasyGuiGraphics gui) { }

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
