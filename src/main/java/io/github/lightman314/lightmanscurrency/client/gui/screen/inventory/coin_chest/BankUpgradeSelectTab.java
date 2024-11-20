package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_chest;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountSelectionWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestBankUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestUpgradeData;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;

public class BankUpgradeSelectTab extends CoinChestTab.Upgrade {

    public BankUpgradeSelectTab(CoinChestUpgradeData data, Object screen) { super(data, screen); }

    private PlayerReference getPlayer() { return PlayerReference.of(this.menu.player); }

    @Override
    public boolean coinSlotsVisible() { return false; }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.addChild(BankAccountSelectionWidget.builder()
                .position(screenArea.pos.offset(20,15))
                .width(screenArea.width - 40)
                .rows(6)
                .filter(this::allowedAccess)
                .selected(this::selectedAccount)
                .handler(this::selectAccount)
                .build());

    }

    private boolean allowedAccess(@Nonnull BankReference reference)
    {
        return reference.allowedAccess(PlayerReference.of(this.menu.player));
    }

    private BankReference selectedAccount() {
        CoinChestUpgradeData data = this.getUpgradeData();
        if(data != null && data.upgrade instanceof CoinChestBankUpgrade upgrade)
            return upgrade.getTargetAccount(data);
        return null;
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) { }

    private void selectAccount(@Nonnull BankReference reference)
    {
        this.menu.SendMessageToServer(this.builder().setCompound("SetBankAccount",reference.save()));
    }

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(Items.PAPER); }

    @Override
    public Component getTooltip() { return LCText.TOOLTIP_ATM_SELECTION.get(); }

}