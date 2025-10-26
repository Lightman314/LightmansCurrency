package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core.addons.MiscTabAddon;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MiscTab extends SettingsSubTab {

    public MiscTab(TraderSettingsClientTab parent) { super(parent); }

    PlainButton buttonAlwaysShowSearchBox;

    PlainButton buttonToggleBankLink;

    PlainButton buttonToggleNotifications;
    PlainButton buttonToggleChatNotifications;
    EasyButton buttonToggleTeamLevel;

    int yOffset = 0;

    private List<MiscTabAddon> addons = new ArrayList<>();

    public List<MiscTabAddon> getAddons() {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            return trader.getMiscTabAddons();
        return new ArrayList<>();
    }

    @Override
    public IconData getIcon() { return IconUtil.ICON_SETTINGS; }

    @Override
    public MutableComponent getTooltip() { return LCText.TOOLTIP_TRADER_SETTINGS_MISC.get(); }

    @Override
    public boolean canOpen() { return this.menu.hasPermission(Permissions.EDIT_SETTINGS); }

    @Override
    public boolean shouldRenderInventoryText() { return false; }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        AtomicInteger nextYLevel = new AtomicInteger(15);

        this.addons = this.getAddons();
        this.addons.forEach(a -> a.initialize(this));

        this.addons.forEach(a -> a.onOpenBefore(this, screenArea, firstOpen, nextYLevel));

        this.yOffset = nextYLevel.get();

        this.buttonAlwaysShowSearchBox = this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(35,nextYLevel.getAndAdd(20)))
                .pressAction(this::ToggleShowSearchBox)
                .sprite(SpriteUtil.createCheckbox(this::alwaysShowSearchBox))
                .build());

        this.buttonToggleBankLink = this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(35,nextYLevel.getAndAdd(20)))
                .pressAction(this::ToggleBankLink)
                .sprite(SpriteUtil.createCheckbox(this::linkedToBank))
                .addon(EasyAddonHelper.visibleCheck(this::showBankLink))
                .addon(EasyAddonHelper.activeCheck(this::bankLinkPossible))
                .build());

        this.buttonToggleNotifications = this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(35,nextYLevel.getAndAdd(20)))
                .pressAction(this::ToggleNotifications)
                .sprite(SpriteUtil.createCheckbox(this::notificationsEnabled))
                .build());

        this.buttonToggleChatNotifications = this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(35,nextYLevel.getAndAdd(25)))
                .pressAction(this::ToggleChatNotifications)
                .sprite(SpriteUtil.createCheckbox(this::notificationsToChat))
                .build());

        this.buttonToggleTeamLevel = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(20,nextYLevel.getAndAdd(25)))
                .width(screenArea.width - 40)
                .text(this::TeamLevelText)
                .pressAction(this::ToggleTeamNotificationLevel)
                .addon(EasyAddonHelper.visibleCheck(this::teamLevelVisible))
                .build());

        this.addons.forEach(a -> a.onOpenAfter(this, screenArea, firstOpen, nextYLevel));

        this.tick();

    }

    @Override
    protected void onSubtabClose() { this.addons.forEach(a -> a.onClose(this)); }

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
        return t != null && t.isLinkedToBank();
    }

    private boolean showBankLink() {
        TraderData t = this.menu.getTrader();
        return t.hasPermission(this.menu.getPlayer(),Permissions.BANK_LINK) && t.canStoreMoney();
    }

    private boolean bankLinkPossible() {
        TraderData t = this.menu.getTrader();
        return t != null && (t.canLinkBankAccount() || t.isLinkedToBank());
    }

    private boolean teamLevelVisible() {
        TraderData t = this.menu.getTrader();
        return t != null && t.getOwner().getValidOwner().hasNotificationLevels();
    }

    @Override
    public void renderBG(EasyGuiGraphics gui) {

        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;

        //Render the "always show search box" text
        gui.drawString(LCText.GUI_TRADER_SETTINGS_ENABLE_SHOW_SEARCH_BOX.get(), 47, this.yOffset + 1, 0x404040);

        //Render the "Link to Bank Account" text
        if(this.menu.hasPermission(Permissions.BANK_LINK) && trader.canStoreMoney())
            gui.drawString(LCText.GUI_SETTINGS_BANK_LINK.get(), 47, this.yOffset + 21, 0x404040);

        //Render the enable notification test
        gui.drawString(LCText.GUI_TRADER_SETTINGS_NOTIFICATIONS_ENABLED.get(), 47, this.yOffset + 41, 0x404040);

        //Render the enable chat notification text
        gui.drawString(LCText.GUI_TRADER_SETTINGS_NOTIFICATIONS_CHAT.get(), 47, this.yOffset + 61, 0x404040);


        this.addons.forEach(a -> a.renderBG(this, gui));

    }

    @Override
    public void renderAfterWidgets(EasyGuiGraphics gui) {
        this.addons.forEach(a -> a.renderAfterWidgets(this, gui));
    }

    @Override
    public void tick() { this.addons.forEach(a -> a.tick(this)); }

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
        this.sendMessage(this.builder().setBoolean("LinkToBankAccount", !trader.isLinkedToBank()));
    }

}
