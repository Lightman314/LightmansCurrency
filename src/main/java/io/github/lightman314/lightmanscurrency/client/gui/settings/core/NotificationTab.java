package io.github.lightman314.lightmanscurrency.client.gui.settings.core;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class NotificationTab extends SettingsTab{

	public static final NotificationTab INSTANCE = new NotificationTab();
	
	@Override
	public int getColor() { return 0xFFFFFF; }

	@Nonnull
    @Override
	public IconData getIcon() { return IconData.of(Items.ENDER_PEARL); }
	
	@Override
	public ITextComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.settings.notifications"); }
	
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
		
		this.buttonToggleTeamLevel = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 20, screen.guiTop() + 80, screen.xSize - 40, 20, EasyText.empty(), this::ToggleTeamNotificationLevel));
		
		this.tick();
		
	}

	@Override
	public void preRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
		TraderSettingsScreen screen = this.getScreen();
		TraderData trader = this.getTrader();
		
		//Render the enable notification test
		this.getFont().draw(pose, EasyText.translatable("gui.lightmanscurrency.notifications.enabled"), screen.guiLeft() + 32, screen.guiTop() + 35, 0x404040);
		
		//Render the enable chat notification text
		this.getFont().draw(pose, EasyText.translatable("gui.lightmanscurrency.notifications.chat"), screen.guiLeft() + 32, screen.guiTop() + 55, 0x404040);
		
		this.buttonToggleTeamLevel.visible = trader.getOwner().hasTeam();
		if(this.buttonToggleTeamLevel.visible)
		{
			ITextComponent message = EasyText.translatable("gui.button.lightmanscurrency.team.bank.notifications", EasyText.translatable("gui.button.lightmanscurrency.team.bank.limit." + trader.teamNotificationLevel()));
			this.buttonToggleTeamLevel.setMessage(message);
		}
		
	}
	
	@Override
	public void postRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) { }

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
		CompoundNBT message = new CompoundNBT();
		message.putBoolean("Notifications", !this.getTrader().notificationsEnabled());
		this.getTrader().sendNetworkMessage(message);
	}
	
	private void ToggleChatNotifications(Button button) {
		CompoundNBT message = new CompoundNBT();
		message.putBoolean("NotificationsToChat", !this.getTrader().notificationsToChat());
		this.getTrader().sendNetworkMessage(message);
	}
	
	private void ToggleTeamNotificationLevel(Button button) {
		CompoundNBT message = new CompoundNBT();
		message.putInt("TeamNotificationLevel", Team.NextBankLimit(this.getTrader().teamNotificationLevel()));
		this.getTrader().sendNetworkMessage(message);
	}
	
}