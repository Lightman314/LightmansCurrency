package io.github.lightman314.lightmanscurrency.client.gui.settings.core;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class NotificationTab extends SettingsTab{

	public static final NotificationTab INSTANCE = new NotificationTab();
	
	@Override
	public int getColor() { return 0xFFFFFF; }

	@Override
	public @NotNull IconData getIcon() { return IconData.of(Items.ENDER_PEARL); }
	
	@Override
	public MutableComponent getTooltip() { return Component.translatable("tooltip.lightmanscurrency.settings.notifications"); }
	
	private NotificationTab() { }
	
	PlainButton buttonToggleNotifications;
	PlainButton buttonToggleChatNotifications;
	Button buttonToggleTeamLevel;
	
	@Override
	public boolean canOpen() { return this.hasPermissions(Permissions.TRANSFER_OWNERSHIP); }

	@Override
	public void initTab() {
		
		TraderSettingsScreen screen = this.getScreen();
		
		this.buttonToggleNotifications = screen.addRenderableTabWidget(new PlainButton(screen.guiLeft() + 20, screen.guiTop() + 35, 10, 10, this::ToggleNotifications, TraderSettingsScreen.GUI_TEXTURE, 10, 200));
		
		this.buttonToggleChatNotifications = screen.addRenderableTabWidget(new PlainButton(screen.guiLeft() + 20, screen.guiTop() + 55, 10, 10, this::ToggleChatNotifications, TraderSettingsScreen.GUI_TEXTURE, 10, 200));
		
		this.buttonToggleTeamLevel = screen.addRenderableTabWidget(Button.builder(Component.empty(), this::ToggleTeamNotificationLevel).pos(screen.guiLeft() + 20, screen.guiTop() + 80).size(screen.xSize - 40, 20).build());
		
		this.tick();
		
	}

	@Override
	public void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		TraderSettingsScreen screen = this.getScreen();
		TraderData trader = this.getTrader();
		if(trader == null)
			return;
		
		//Render the enable notification test
		this.getFont().draw(pose, Component.translatable("gui.lightmanscurrency.notifications.enabled"), screen.guiLeft() + 32, screen.guiTop() + 35, 0x404040);
		
		//Render the enable chat notification text
		this.getFont().draw(pose, Component.translatable("gui.lightmanscurrency.notifications.chat"), screen.guiLeft() + 32, screen.guiTop() + 55, 0x404040);
		
		this.buttonToggleTeamLevel.visible = trader.getOwner().hasTeam();
		if(this.buttonToggleTeamLevel.visible)
		{
			Component message = Component.translatable("gui.button.lightmanscurrency.team.bank.notifications", Component.translatable("gui.button.lightmanscurrency.team.bank.limit." + trader.teamNotificationLevel()));
			this.buttonToggleTeamLevel.setMessage(message);
		}
		
	}
	
	@Override
	public void postRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) { }

	@Override
	public void tick() {
		
		TraderData trader = this.getTrader();
		if(trader != null)
		{
			this.buttonToggleNotifications.setResource(TraderSettingsScreen.GUI_TEXTURE, 10, trader.notificationsEnabled() ? 200 : 220);
			this.buttonToggleChatNotifications.setResource(TraderSettingsScreen.GUI_TEXTURE, 10, trader.notificationsToChat() ? 200 : 220);
		}
		
	}

	@Override
	public void closeTab() { }
	
	private void ToggleNotifications(Button button) {
		TraderData trader = this.getTrader();
		if(trader == null)
			return;
		CompoundTag message = new CompoundTag();
		message.putBoolean("Notifications", !trader.notificationsEnabled());
		this.getTrader().sendNetworkMessage(message);
	}
	
	private void ToggleChatNotifications(Button button) {
		TraderData trader = this.getTrader();
		if(trader == null)
			return;
		CompoundTag message = new CompoundTag();
		message.putBoolean("NotificationsToChat", !trader.notificationsToChat());
		this.getTrader().sendNetworkMessage(message);
	}
	
	private void ToggleTeamNotificationLevel(Button button) {
		TraderData trader = this.getTrader();
		if(trader == null)
			return;
		CompoundTag message = new CompoundTag();
		message.putInt("TeamNotificationLevel", Team.NextBankLimit(trader.teamNotificationLevel()));
		this.getTrader().sendNetworkMessage(message);
	}
	
}
