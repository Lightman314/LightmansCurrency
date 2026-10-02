package io.github.lightman314.lightmanscurrency.client.features.atm.tabs;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.bank.BankInteractionWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.bank.IBankInteractionHandler;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.features.atm.ATMClientTab;
import io.github.lightman314.lightmanscurrency.client.features.atm.ATMScreen;
import io.github.lightman314.lightmanscurrency.core.LCBlocks;
import io.github.lightman314.lightmanscurrency.features.atm.ATMMenu;
import io.github.lightman314.lightmanscurrency.features.atm.ATMTab;
import io.github.lightman314.lightmanscurrency.features.atm.tabs.AccountInteractionTab;
import net.minecraft.network.chat.Component;

public class AccountInteractionClientTab extends ATMClientTab<AccountInteractionTab> implements IBankInteractionHandler {

    public static final TabBuilder<ATMMenu,AccountInteractionTab,ATMTab,ATMScreen> BUILDER = AccountInteractionClientTab::new;

    protected AccountInteractionClientTab(ATMMenu menu,AccountInteractionTab commonTab,ATMScreen screen) { super(menu, commonTab, screen); }

    private BankInteractionWidget interactionWidget;

    @Override
    public IconData getIcon() { return ItemIcon.of(LCBlocks.COIN_PILE_GOLD); }
    @Override
    public Component getName() { return AccountInteractionTab.TOOLTIP.get(); }

    @Override
    protected void initialize(ScreenArea area, FancyPacketMap message) {
        this.interactionWidget = this.addChild(BankInteractionWidget.builder()
                .atPos(area.pos)
                .withHandler(this)
                .withSpacing(19)
                .withOldWidget(this.interactionWidget)
                .build());
    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {

    }

    @Override
    public void attemptDeposit(MoneyValue amount) { this.getCommonTab().depositMoney(amount); }

    @Override
    public void attemptWithdraw(MoneyValue amount) { this.getCommonTab().withdrawMoney(amount); }

}