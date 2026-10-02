package io.github.lightman314.lightmanscurrency.client.features.atm.tabs;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.TextButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.money.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.helpers.ListHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.keys.DualKey;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.money.MoneyDisplayHelper;
import io.github.lightman314.lightmanscurrency.api.money.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.features.atm.ATMClientTab;
import io.github.lightman314.lightmanscurrency.client.features.atm.ATMScreen;
import io.github.lightman314.lightmanscurrency.features.atm.ATMMenu;
import io.github.lightman314.lightmanscurrency.features.atm.ATMTab;
import io.github.lightman314.lightmanscurrency.features.atm.tabs.AccountSettingsTab;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;

public class AccountSettingsClientTab extends ATMClientTab<AccountSettingsTab> {

    public static final TabBuilder<ATMMenu,AccountSettingsTab,ATMTab,ATMScreen> BUILDER = AccountSettingsClientTab::new;

    private MoneyValueWidget notificationSelection;

    protected AccountSettingsClientTab(ATMMenu menu, AccountSettingsTab commonTab, ATMScreen screen) {
        super(menu, commonTab, screen);
    }

    @Override
    public IconData getIcon() { return SpriteIcon.of(LCApi.id("icon/settings")); }
    @Override
    public Component getName() { return AccountSettingsTab.TOOLTIP.get(); }

    @Override
    protected void initialize(ScreenArea area, FancyPacketMap message) {
        this.notificationSelection = this.addChild(MoneyValueWidget.builder()
                .atPos(area.pos)
                .oldWidget(this.notificationSelection)
                .handler(this::onValueChanged)
                .typeChangeListener(this::onValueTypeChanged)
                .disallowFreeInput()
                .build());
        //Manually trigger type change to set the widgets current value as appropriate
        this.onValueTypeChanged();

        //Reset bank card validation button
        this.addChild(TextButton.builder()
                .atPos(area.pos.offset(20,117))
                .ofWidth(area.width - 40)
                .withText(AccountSettingsTab.BUTTON_BANK_CARD_RESET)
                .onPress(this.getCommonTab()::resetBankCardKey)
                .tooltip(TooltipSource.simple(AccountSettingsTab.TOOLTIP_BANK_CARD_RESET).withAutoWrap())
                .build());

    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        BankAccount account = this.getBankAccount();
        if(account != null) {
            int yPos = 70;
            Component text = this.getRandomNotificationLevelText(account);
            gui.centeredTextWithWordWrap(text,area.centerX(),yPos,area.width - 10,0xFF404040,false);
            int lines = gui.getFont().split(text,area.width - 10).size();
            //Tooltip if hovering over the text
            ScreenArea hoverArea = ScreenArea.of(5,yPos,area.width - 10,10 * lines);
            if(hoverArea.isInArea(gui.getRelativeMousePos())) {
                MoneyView tooltipValues = MoneyView.builder().add(account.getNotificationLevels().values()).build();
                if(!tooltipValues.isEmpty())
                    gui.renderTooltipAtMouse(MoneyDisplayHelper.contentsAsMultiLineText(tooltipValues));
            }
        }

    }

    private Component getRandomNotificationLevelText(BankAccount account) {
        Map<DualKey,MoneyValue> limits = account.getNotificationLevels();
        if(limits.isEmpty())
            return AccountSettingsTab.GUI_NOTIFICATIONS_DISABLED.get();
        List<MoneyValue> values = List.copyOf(limits.values());
        return AccountSettingsTab.GUI_NOTIFICATIONS_DETAILS.get(ListHelper.cyclingValueFromList(values,MoneyValue.empty()).getText());
    }

    private void onValueChanged(MoneyValue value) {
        if(this.notificationSelection == null)
            return;
        if(value.isEmpty() || value.isFree()) {
            DualKey type = this.notificationSelection.getCurrentHandlerType();
            this.getCommonTab().setNotificationLevel(type,MoneyValue.empty());
        }
        else
            this.getCommonTab().setNotificationLevel(value.getKey(),value);
    }

    private void onValueTypeChanged() {
        BankAccount account = this.getBankAccount();
        if(account != null) {
            DualKey key = this.notificationSelection.getCurrentHandlerType();
            MoneyValue currentValue = account.getNotificationLevelFor(key);
            this.notificationSelection.changeValue(currentValue);
        }
    }

}
