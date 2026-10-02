package io.github.lightman314.lightmanscurrency.client.features.atm.tabs;

import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.NotificationDisplayWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.VerticalScrollBar;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationStack;
import io.github.lightman314.lightmanscurrency.client.features.atm.ATMClientTab;
import io.github.lightman314.lightmanscurrency.client.features.atm.ATMScreen;
import io.github.lightman314.lightmanscurrency.features.atm.ATMMenu;
import io.github.lightman314.lightmanscurrency.features.atm.ATMTab;
import io.github.lightman314.lightmanscurrency.features.atm.tabs.AccountLogsTab;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.util.List;

public class AccountLogsClientTab extends ATMClientTab<AccountLogsTab> {

    public static final TabBuilder<ATMMenu,AccountLogsTab,ATMTab,ATMScreen> BUILDER = AccountLogsClientTab::new;

    protected AccountLogsClientTab(ATMMenu menu, AccountLogsTab commonTab, ATMScreen screen) {
        super(menu, commonTab, screen);
    }

    private NotificationDisplayWidget displayWidget;

    @Override
    public IconData getIcon() { return ItemIcon.of(Items.WRITABLE_BOOK); }
    @Override
    public Component getName() { return AccountLogsTab.TOOLTIP.get(); }

    @Override
    protected void initialize(ScreenArea area, FancyPacketMap message) {
        this.displayWidget = this.addChild(NotificationDisplayWidget.builder()
                .atPos(15,15)
                .ofWidth(area.width - 30)
                .withOldWidget(this.displayWidget)
                .withRows(6)
                .withNotifications(this::getNotifications)
                .build());
        this.addChild(VerticalScrollBar.builder(this.displayWidget)
                .rightOf(this.displayWidget)
                .build());
    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        gui.text(this.getName(),8,6,0xFF404040,false);
    }

    private List<NotificationStack> getNotifications() {
        BankAccount account = this.getBankAccount();
        if(account == null)
            return account.getNotifications();
        return List.of();
    }

}
