package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;

public class NotificationTab extends SettingsSubTab {

    public NotificationTab(@Nonnull TraderSettingsClientTab parent) { super(parent); }

    PlainButton buttonToggleNotifications;
    PlainButton buttonToggleChatNotifications;
    Button buttonToggleTeamLevel;

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(Items.ENDER_PEARL); }

    @Override
    public MutableComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.settings.notifications"); }

    @Override
    public boolean canOpen() { return this.menu.hasPermission(Permissions.NOTIFICATION); }

    @Override
    public void onOpen() {


        this.buttonToggleNotifications = this.addWidget(new PlainButton(this.screen.getGuiLeft() + 35, this.screen.getGuiTop() + 35, 10, 10, this::ToggleNotifications, IconAndButtonUtil.WIDGET_TEXTURE, 10, 200));

        this.buttonToggleChatNotifications = this.addWidget(new PlainButton(this.screen.getGuiLeft() + 35, this.screen.getGuiTop() + 55, 10, 10, this::ToggleChatNotifications, IconAndButtonUtil.WIDGET_TEXTURE, 10, 200));

        this.buttonToggleTeamLevel = this.addWidget(EasyButton.builder(EasyText.empty(), this::ToggleTeamNotificationLevel).pos(this.screen.getGuiLeft() + 20, this.screen.getGuiTop() + 80).size(this.screen.getXSize() - 40, 20).build());

        this.tick();

    }

    @Override
    public void onClose() {

    }

    @Override
    public void renderBG(@Nonnull PoseStack pose, int mouseX, int mouseY, float partialTicks) {

        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;

        //Render the enable notification test
        this.font.draw(pose, EasyText.translatable("gui.lightmanscurrency.notifications.enabled"), this.screen.getGuiLeft() + 47, this.screen.getGuiTop() + 35, 0x404040);

        //Render the enable chat notification text
        this.font.draw(pose, EasyText.translatable("gui.lightmanscurrency.notifications.chat"), this.screen.getGuiLeft() + 47, this.screen.getGuiTop() + 55, 0x404040);

        this.buttonToggleTeamLevel.visible = trader.getOwner().hasTeam();
        if(this.buttonToggleTeamLevel.visible)
        {
            Component message = EasyText.translatable("gui.button.lightmanscurrency.team.bank.notifications", EasyText.translatable("gui.button.lightmanscurrency.team.bank.limit." + trader.teamNotificationLevel()));
            this.buttonToggleTeamLevel.setMessage(message);
        }

    }

    @Override
    public void renderTooltips(@Nonnull PoseStack pose, int mouseX, int mouseY) {

    }

    @Override
    public void tick() {

        TraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            this.buttonToggleNotifications.setResource(IconAndButtonUtil.WIDGET_TEXTURE, 10, trader.notificationsEnabled() ? 200 : 220);
            this.buttonToggleChatNotifications.setResource(IconAndButtonUtil.WIDGET_TEXTURE, 10, trader.notificationsToChat() ? 200 : 220);
        }

    }

    private void ToggleNotifications(Button button) {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;
        CompoundTag message = new CompoundTag();
        message.putBoolean("Notifications", !trader.notificationsEnabled());
        this.sendNetworkMessage(message);
    }

    private void ToggleChatNotifications(Button button) {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;
        CompoundTag message = new CompoundTag();
        message.putBoolean("NotificationsToChat", !trader.notificationsToChat());
        this.sendNetworkMessage(message);
    }

    private void ToggleTeamNotificationLevel(Button button) {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;
        CompoundTag message = new CompoundTag();
        message.putInt("TeamNotificationLevel", Team.NextBankLimit(trader.teamNotificationLevel()));
        this.sendNetworkMessage(message);
    }

}