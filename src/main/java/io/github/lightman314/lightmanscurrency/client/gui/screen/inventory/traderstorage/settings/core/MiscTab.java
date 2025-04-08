package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
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

        this.buttonAlwaysShowSearchBox = this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(35,15))
                .pressAction(this::ToggleShowSearchBox)
                .sprite(IconAndButtonUtil.SPRITE_CHECK(this::alwaysShowSearchBox))
                .build());

        this.buttonToggleBankLink = this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(35,35))
                .pressAction(this::ToggleBankLink)
                .sprite(IconAndButtonUtil.SPRITE_CHECK(this::linkedToBank))
                .addon(EasyAddonHelper.visibleCheck(this::showBankLink))
                .addon(EasyAddonHelper.activeCheck(this::bankLinkPossible))
                .build());

        this.buttonToggleNotifications = this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(35,55))
                .pressAction(this::ToggleNotifications)
                .sprite(IconAndButtonUtil.SPRITE_CHECK(this::notificationsEnabled))
                .build());

        this.buttonToggleChatNotifications = this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(35,75))
                .pressAction(this::ToggleChatNotifications)
                .sprite(IconAndButtonUtil.SPRITE_CHECK(this::notificationsToChat))
                .build());

        this.buttonToggleTeamLevel = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(20,100))
                .width(screenArea.width - 40)
                .text(this::TeamLevelText)
                .pressAction(this::ToggleTeamNotificationLevel)
                .addon(EasyAddonHelper.visibleCheck(this::teamLevelVisible))
                .build());

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

    private boolean linkedToBank() {
        TraderData t = this.menu.getTrader();
        return t != null && t.linkedToBank.get();
    }

    private boolean showBankLink() {
        TraderData t = this.menu.getTrader();
        return t.hasPermission(this.menu.getPlayer(),Permissions.BANK_LINK) && t.canStoreMoney();
    }

    private boolean bankLinkPossible() {
        TraderData t = this.menu.getTrader();
        return t != null && (t.canLinkBankAccount() || t.linkedToBank.get());
    }

    private boolean teamLevelVisible() {
        TraderData t = this.menu.getTrader();
        return t != null && t.getOwner().getValidOwner().hasNotificationLevels();
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;

        //Render the "always show search box" text
        gui.drawString(LCText.GUI_TRADER_SETTINGS_ENABLE_SHOW_SEARCH_BOX.get(), 47, 16, 0x404040);

        //Render the "Link to Bank Account" text
        if(this.menu.hasPermission(Permissions.BANK_LINK) && trader.canStoreMoney())
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
        this.sendMessage(this.builder().setBoolean("LinkToBankAccount", !trader.linkedToBank.get()));
    }

}