package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MiscTab extends SettingsSubTab {

    public MiscTab(@Nonnull TraderSettingsClientTab parent) { super(parent); }

    PlainButton buttonAlwaysShowSearchBox;

    PlainButton buttonToggleBankLink;

    PlainButton buttonToggleNotifications;
    PlainButton buttonToggleChatNotifications;
    EasyButton buttonToggleTeamLevel;

    @Nonnull
    @Override
    public IconData getIcon() { return IconUtil.ICON_SETTINGS; }

    @Override
    public MutableComponent getTooltip() { return LCText.TOOLTIP_TRADER_SETTINGS_MISC.get(); }

    @Override
    public boolean canOpen() { return this.menu.hasPermission(Permissions.EDIT_SETTINGS); }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.buttonAlwaysShowSearchBox = this.addChild(IconAndButtonUtil.checkmarkButton(screenArea.pos.offset(35, 15), this::ToggleShowSearchBox, this::alwaysShowSearchBox));

        this.buttonToggleBankLink = this.addChild(IconAndButtonUtil.checkmarkButton(screenArea.pos.offset(35, 35), this::ToggleBankLink, () -> { TraderData t = this.menu.getTrader(); return t != null && t.getLinkedToBank(); }));
        this.buttonToggleBankLink.visible = this.menu.hasPermission(Permissions.BANK_LINK);

        this.buttonToggleNotifications = this.addChild(IconAndButtonUtil.checkmarkButton(screenArea.pos.offset(35, 55), this::ToggleNotifications, this::notificationsEnabled));

        this.buttonToggleChatNotifications = this.addChild(IconAndButtonUtil.checkmarkButton(screenArea.pos.offset(35, 75), this::ToggleChatNotifications, this::notificationsToChat));

        this.buttonToggleTeamLevel = this.addChild(new EasyTextButton(screenArea.pos.offset(20, 100), screenArea.width - 40, 20, this::TeamLevelText, this::ToggleTeamNotificationLevel));

        this.tick();

    }

    private boolean notificationsEnabled() {
        TraderData t = this.menu.getTrader();
        return t != null && t.notificationsEnabled();
    }

    private boolean notificationsToChat() {
        TraderData t = this.menu.getTrader();
        return t != null && t.notificationsToChat();
    }

    private boolean alwaysShowSearchBox() {
        TraderData t = this.menu.getTrader();
        return t != null && t.alwaysShowSearchBox();
    }

    @Override
    public void tick() {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;

        this.buttonToggleTeamLevel.visible = trader.getOwner().getValidOwner().hasNotificationLevels();

        boolean canLinkAccount = this.menu.hasPermission(Permissions.BANK_LINK) && !trader.isCreative();
        this.buttonToggleBankLink.visible = canLinkAccount;
        if(canLinkAccount)
            this.buttonToggleBankLink.active = trader.canLinkBankAccount() || trader.getLinkedToBank();
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;

        //Render the "always show search box" text
        gui.drawString(LCText.GUI_TRADER_SETTINGS_ENABLE_SHOW_SEARCH_BOX.get(), 47, 16, 0x404040);

        //Render the "Link to Bank Account" text
        if(this.menu.hasPermission(Permissions.BANK_LINK) && !trader.isCreative())
            gui.drawString(LCText.GUI_SETTINGS_BANK_LINK.get(), 47, 36, 0x404040);

        //Render the enable notification test
        gui.drawString(LCText.GUI_TRADER_SETTINGS_NOTIFICATIONS_ENABLED.get(), 47, 56, 0x404040);

        //Render the enable chat notification text
        gui.drawString(LCText.GUI_TRADER_SETTINGS_NOTIFICATIONS_CHAT.get(), 47, 76, 0x404040);

    }

    private Component TeamLevelText()
    {
        TraderData trader = this.menu.getTrader();
        int level = trader == null ? 0 : trader.teamNotificationLevel();
        return LCText.GUI_TRADER_SETTINGS_NOTIFICATIONS_TARGET.get(Owner.getOwnerLevelBlurb(level));
    }

    private void ToggleNotifications(EasyButton button) {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;
        this.sendMessage(this.builder().setBoolean("Notifications", !trader.notificationsEnabled()));
    }

    private void ToggleChatNotifications(EasyButton button) {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;
        this.sendMessage(this.builder().setBoolean("NotificationsToChat", !trader.notificationsToChat()));
    }

    private void ToggleTeamNotificationLevel(EasyButton button) {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;
        this.sendMessage(this.builder().setInt("TeamNotificationLevel", Team.NextBankLimit(trader.teamNotificationLevel())));
    }

    private void ToggleShowSearchBox(EasyButton button) {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;
        this.sendMessage(this.builder().setBoolean("AlwaysShowSearchBox", !trader.alwaysShowSearchBox()));
    }

    private void ToggleBankLink(EasyButton button)
    {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;
        this.sendMessage(this.builder().setBoolean("LinkToBankAccount", !trader.getLinkedToBank()));
    }

}
