package io.github.lightman314.lightmanscurrency.client.gui.settings.core;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.CoreTraderSettings;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Items;

public class NotificationTab extends SettingsTab{

	public static final NotificationTab INSTANCE = new NotificationTab();
	
	@Override
	public int getColor() { return 0xFFFFFF; }

	@Override
	public IconData getIcon() { return IconData.of(Items.ENDER_PEARL); }
	
	@Override
	public Component getTooltip() { return new TranslatableComponent("tooltip.lightmanscurrency.settings.notifications"); }
	
	private NotificationTab() { }
	
	PlainButton buttonToggleNotifications;
	Button buttonToggleTeamLevel;
	
	@Override
	public ImmutableList<String> requiredPermissions() {
		return ImmutableList.of(Permissions.TRANSFER_OWNERSHIP);
	}

	@Override
	public void initTab() {
		
		TraderSettingsScreen screen = this.getScreen();
		
		this.buttonToggleNotifications = screen.addRenderableTabWidget(new PlainButton(screen.guiLeft() + 20, screen.guiTop() + 35, 10, 10, this::TogglePermission, TraderSettingsScreen.GUI_TEXTURE, 10, 200));
		
		this.buttonToggleTeamLevel = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 20, screen.guiTop() + 60, screen.xSize - 40, 20, new TextComponent(""), this::ToggleTeamNotificationLevel));
		
		this.tick();
		
	}

	@Override
	public void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		TraderSettingsScreen screen = this.getScreen();
		
		//Render the enable notification test
		this.getFont().draw(pose, new TranslatableComponent("gui.lightmanscurrency.notifications.enabled"), screen.guiLeft() + 32, screen.guiTop() + 35, 0x404040);
		
		CoreTraderSettings coreSettings = this.getSetting(CoreTraderSettings.class);
		this.buttonToggleTeamLevel.visible = coreSettings.getTeam() != null;
		if(this.buttonToggleTeamLevel.visible)
		{
			Component message = new TranslatableComponent("gui.button.lightmanscurrency.team.bank.notifications", new TranslatableComponent("gui.button.lightmanscurrency.team.bank.limit." + coreSettings.getTeamNotificationLevel()));
			this.buttonToggleTeamLevel.setMessage(message);
		}
		
	}
	
	@Override
	public void postRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) { }

	@Override
	public void tick() {
		
		CoreTraderSettings settings = this.getSetting(CoreTraderSettings.class);
		if(settings != null)
			this.buttonToggleNotifications.setResource(TraderSettingsScreen.GUI_TEXTURE, 10, settings.notificationsEnabled() ? 200 : 220);
		
	}

	@Override
	public void closeTab() { }
	
	private void TogglePermission(Button button) {
		CoreTraderSettings coreSettings = this.getScreen().getSetting(CoreTraderSettings.class);
		CompoundTag updateInfo = coreSettings.toggleNotifications(this.getPlayer());
		coreSettings.sendToServer(updateInfo);
	}
	
	private void ToggleTeamNotificationLevel(Button button) {
		CoreTraderSettings coreSettings = this.getScreen().getSetting(CoreTraderSettings.class);
		int newLimit = Team.NextBankLimit(coreSettings.getTeamNotificationLevel());
		CompoundTag updateInfo = coreSettings.setTeamNotificationLevel(this.getPlayer(), newLimit);
		coreSettings.sendToServer(updateInfo);
	}
	
}
