package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core;

import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;

public class NotificationTab extends SettingsSubTab {

    public NotificationTab(@Nonnull TraderSettingsClientTab parent) { super(parent); }

    PlainButton buttonToggleNotifications;
    PlainButton buttonToggleChatNotifications;
    EasyButton buttonToggleTeamLevel;

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(Items.ENDER_PEARL); }

    @Override
    public MutableComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.settings.notifications"); }

    @Override
    public boolean canOpen() { return this.menu.hasPermission(Permissions.NOTIFICATION); }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {


        this.buttonToggleNotifications = this.addChild(IconAndButtonUtil.checkmarkButton(screenArea.pos.offset(35, 35), this::ToggleNotifications, this::notificationsEnabled));

        this.buttonToggleChatNotifications = this.addChild(IconAndButtonUtil.checkmarkButton(screenArea.pos.offset(35, 55), this::ToggleChatNotifications, this::notificationsToChat));

        this.buttonToggleTeamLevel = this.addChild(new EasyTextButton(screenArea.pos.offset(20, 80), screenArea.width - 40, 20, EasyText.empty(), this::ToggleTeamNotificationLevel));

    }

    private boolean notificationsEnabled() {
        TraderData t = this.menu.getTrader();
        return t != null && t.notificationsEnabled();
    }

    private boolean notificationsToChat() {
        TraderData t = this.menu.getTrader();
        return t != null && t.notificationsToChat();
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;

        //Render the enable notification test
        gui.drawString(EasyText.translatable("gui.lightmanscurrency.notifications.enabled"), 47, 35, 0x404040);

        //Render the enable chat notification text
        gui.drawString(EasyText.translatable("gui.lightmanscurrency.notifications.chat"), 47, 55, 0x404040);

        this.buttonToggleTeamLevel.visible = trader.getOwner().hasTeam();
        if(this.buttonToggleTeamLevel.visible)
        {
            Component message = EasyText.translatable("gui.button.lightmanscurrency.team.bank.notifications", EasyText.translatable("gui.button.lightmanscurrency.team.bank.limit." + trader.teamNotificationLevel()));
            this.buttonToggleTeamLevel.setMessage(message);
        }

    }

    private void ToggleNotifications(EasyButton button) {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;
        CompoundTag message = new CompoundTag();
        message.putBoolean("Notifications", !trader.notificationsEnabled());
        this.sendNetworkMessage(message);
    }

    private void ToggleChatNotifications(EasyButton button) {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;
        CompoundTag message = new CompoundTag();
        message.putBoolean("NotificationsToChat", !trader.notificationsToChat());
        this.sendNetworkMessage(message);
    }

    private void ToggleTeamNotificationLevel(EasyButton button) {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;
        CompoundTag message = new CompoundTag();
        message.putInt("TeamNotificationLevel", Team.NextBankLimit(trader.teamNotificationLevel()));
        this.sendNetworkMessage(message);
    }

}
