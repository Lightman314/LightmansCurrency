package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_chest;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestBankUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestUpgradeData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class BankUpgradeSettingsTab extends CoinChestTab.Upgrade {

    public BankUpgradeSettingsTab(@Nonnull CoinChestUpgradeData data, @Nonnull Object screen) { super(data, screen); }

    private MoneyValueWidget oldWidget = null;

    @Override
    public boolean coinSlotsVisible() { return false; }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(20,20))
                .width(screen.width - 40)
                .text(this::getModeButtonText)
                .pressAction(this::toggleDepositMode)
                .build());

        MoneyValue moneyLimit = MoneyValue.empty();
        CoinChestUpgradeData data = this.getUpgradeData();
        if(data != null && data.upgrade instanceof CoinChestBankUpgrade upgrade)
            moneyLimit = upgrade.getMoneyLimit(data);

        this.oldWidget = this.addChild(new MoneyValueWidget(screenArea.pos.offset((screenArea.width / 2) - (MoneyValueWidget.WIDTH / 2),50),this.oldWidget, moneyLimit, this::onMoneyLimitChange));
        this.oldWidget.allowFreeInput = false;
        this.oldWidget.drawBG = false;

        this.addChild(new IconButton(screenArea.pos.offset(screen.width,0), this::collectOverflowItems, IconUtil.ICON_STORAGE)
                .withAddons(EasyAddonHelper.visibleCheck(this::hasOverflowItems)));

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        CoinChestUpgradeData data = this.getUpgradeData();
        if(data != null && data.upgrade instanceof CoinChestBankUpgrade upgrade)
        {
            boolean depositMode = upgrade.isDepositMode(data);
            MoneyValue moneyLimit = upgrade.getMoneyLimit(data);
            IBankAccount account = upgrade.getSelectedBankAccount(this.menu.be,data);

            //Draw operation info
            Component text;
            if(account == null)
                text = LCText.GUI_BANK_UPGRADE_DETAILS_NO_ACCOUNT.get();
            else if(depositMode)
            {
                if(moneyLimit.isEmpty())
                    text = LCText.GUI_BANK_UPGRADE_DETAILS_DEPOSIT_UNLIMITED.get();
                else
                    text = LCText.GUI_BANK_UPGRADE_DETAILS_DEPOSIT_LIMITED.get(moneyLimit.getText());
            }
            else
            {
                if(moneyLimit.isEmpty())
                    text = LCText.GUI_BANK_UPGRADE_DETAILS_WITHDRAW_INVALID.get();
                else
                    text = LCText.GUI_BANK_UPGRADE_DETAILS_WITHDRAW.get(moneyLimit.getText());
            }

            TextRenderUtil.drawCenteredMultilineText(gui,text, 20, this.screen.getXSize() - 40, 122, 0x404040);
        }

    }

    private Component getModeButtonText()
    {
        boolean depositMode = this.getUpgradeData().upgrade instanceof CoinChestBankUpgrade upgrade && upgrade.isDepositMode(this.getUpgradeData());
        return depositMode ? LCText.BUTTON_BANK_UPGRADE_MODE_DEPOSIT.get() : LCText.BUTTON_BANK_UPGRADE_MODE_WITHDRAW.get();
    }

    private void toggleDepositMode()
    {
        CoinChestUpgradeData data = this.getUpgradeData();
        boolean currentState = data != null && data.upgrade instanceof CoinChestBankUpgrade upgrade && upgrade.isDepositMode(data);
        this.menu.SendMessageToServer(this.builder().setBoolean("SetDepositMode",!currentState));
    }

    private void onMoneyLimitChange(@Nonnull MoneyValue newLimit)
    {
        this.menu.SendMessageToServer(this.builder().setMoneyValue("SetMoneyLimit",newLimit));
    }

    private boolean hasOverflowItems() {
        CoinChestUpgradeData data = this.getUpgradeData();
        return data != null && data.upgrade instanceof CoinChestBankUpgrade upgrade && !upgrade.getOverflowItems(data).isEmpty();
    }

    private void collectOverflowItems(EasyButton button)
    {
        this.menu.SendMessageToServer(this.builder().setFlag("CollectOverflowItems"));
    }

}
