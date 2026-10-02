package io.github.lightman314.lightmanscurrency.client.features.atm.tabs;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.bank.BankAccountSelectionWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.TextButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.client.features.atm.ATMClientTab;
import io.github.lightman314.lightmanscurrency.client.features.atm.ATMScreen;
import io.github.lightman314.lightmanscurrency.features.atm.ATMMenu;
import io.github.lightman314.lightmanscurrency.features.atm.ATMTab;
import io.github.lightman314.lightmanscurrency.features.atm.tabs.AccountSelectionTab;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class AccountSelectionClientTab extends ATMClientTab<AccountSelectionTab> {

    public static final TabBuilder<ATMMenu,AccountSelectionTab,ATMTab,ATMScreen> BUILDER = AccountSelectionClientTab::new;

    protected AccountSelectionClientTab(ATMMenu menu, AccountSelectionTab commonTab, ATMScreen screen) {
        super(menu,commonTab,screen);
    }

    private boolean adminMode = false;
    private boolean isAdminMode() { return this.adminMode; }
    private boolean isNormalMode() { return !this.adminMode; }

    TextBoxWrapper<String> playerAccountSelect;
    Component responseMessage = null;

    @Override
    public IconData getIcon() { return ItemIcon.of(Items.PAPER); }
    @Override
    public Component getName() { return AccountSelectionTab.TOOLTIP.get(); }

    @Override
    protected void initialize(ScreenArea area, FancyPacketMap message) {
        this.addChild(BankAccountSelectionWidget.builder()
                .atPos(area.pos.offset(20,15))
                .ofWidth(area.width - 40)
                .withRows(6)
                .withFilter(this::canAccess)
                .withSelected(this::getSelectedAccount)
                .withHandler(this.getCommonTab()::selectAccount)
                .visible(this::isNormalMode)
                .build());

        this.addChild(IconButton.builder()
                .atPos(area.pos.offset(area.width,0))
                .onPress(() -> this.adminMode = !this.adminMode)
                .withIcon(ItemIcon.of(Items.COMMAND_BLOCK))
                .visible(() -> LCApi.isInAdminMode(this.getPlayer()))
                .build());

        this.playerAccountSelect = this.addChild(TextBoxWrapper.stringBuilder()
                .atPos(area.pos.offset(7,20))
                .ofWidth(162)
                .withOldWidget(this.playerAccountSelect)
                .withMaxLength(16)
                .visible(this::isAdminMode)
                .build());

        this.addChild(TextButton.builder()
                .atPos(area.pos.offset(7,45))
                .ofWidth(162)
                .withText(AccountSelectionTab.BUTTON_PLAYER_ACCOUNT)
                .onPress(this::selectPlayerAccount)
                .active(() -> !this.playerAccountSelect.getString().isBlank())
                .visible(this::isAdminMode)
                .build());

    }
    @Override
    protected void afterTabClosed() {
        this.adminMode = false;
        this.responseMessage = null;
    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        gui.text(this.getName(),8,6,0xFF404040,false);

        if(this.adminMode && this.responseMessage != null)
            gui.centeredTextWithWordWrap(this.responseMessage,7,70,area.width - 15,0xFF404040,false);
    }

    @Override
    public void handleMessage(FancyPacketMap message) {
        if(message.contains("playerAccountSuccess"))
            this.responseMessage = message.getText("playerAccountSuccess");
    }

    private boolean canAccess(BankReference reference) { return reference.allowedAccess(this.getPlayer()); }

    private void selectPlayerAccount() {
        String playerName = this.playerAccountSelect.getString();
        this.playerAccountSelect.setValue("");
        this.getCommonTab().selectPlayerAccount(playerName);
    }

}
