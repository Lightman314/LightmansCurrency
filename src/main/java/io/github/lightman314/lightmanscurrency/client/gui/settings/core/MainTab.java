package io.github.lightman314.lightmanscurrency.client.gui.settings.core;

import com.google.common.collect.Lists;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.persistentdata.MessageAddPersistentTrader;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageAddOrRemoveTrade;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class MainTab extends SettingsTab{

	public static final MainTab INSTANCE = new MainTab();
	
	/**
	 * Main Tab contains the Trader Name & Creative Settings
	 */
	private MainTab() { }
	
	TextFieldWidget nameInput;
	Button buttonSetName;
	Button buttonResetName;
	
	PlainButton buttonToggleBankLink;
	
	IconButton buttonToggleCreative;
	Button buttonAddTrade;
	Button buttonRemoveTrade;
	
	Button buttonSavePersistentTrader;
	TextFieldWidget persistentTraderIDInput;
	TextFieldWidget persistentTraderOwnerInput;
	
	@Override
	public boolean canOpen() { return true; }

	@Override
	public void initTab() {
		
		TraderSettingsScreen screen = this.getScreen();
		
		TraderData trader = this.getTrader();
		
		this.nameInput = screen.addRenderableTabWidget(new TextFieldWidget(screen.getFont(), screen.guiLeft() + 20, screen.guiTop() + 25, 160, 20, EasyText.empty()));
		this.nameInput.setMaxLength(32);
		this.nameInput.setValue(trader.getCustomName());
		
		this.buttonSetName = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 20, screen.guiTop() + 50, 74, 20, EasyText.translatable("gui.lightmanscurrency.changename"), this::SetName));
		this.buttonResetName = screen.addRenderableTabWidget(new Button(screen.guiLeft() + screen.xSize - 93, screen.guiTop() + 50, 74, 20, EasyText.translatable("gui.lightmanscurrency.resetname"), this::ResetName));
		
		//Creative Toggle
		this.buttonToggleCreative = screen.addRenderableTabWidget(IconAndButtonUtil.creativeToggleButton(screen.guiLeft() + 176, screen.guiTop() + screen.ySize - 30, this::ToggleCreative, () -> this.getTrader().isCreative()));
		this.buttonAddTrade = screen.addRenderableTabWidget(new PlainButton(screen.guiLeft() + 166, screen.guiTop() + screen.ySize - 30, 10, 10, this::AddTrade, TraderSettingsScreen.GUI_TEXTURE, 0, 200));
		this.buttonRemoveTrade = screen.addRenderableTabWidget(new PlainButton(screen.guiLeft() + 166, screen.guiTop() + screen.ySize - 20, 10, 10, this::RemoveTrade, TraderSettingsScreen.GUI_TEXTURE, 0, 220));
		
		this.buttonToggleBankLink = screen.addRenderableTabWidget(new PlainButton(screen.guiLeft() + 20, screen.guiTop() + 100, 10, 10, this::ToggleBankLink, TraderSettingsScreen.GUI_TEXTURE, 10, trader.getLinkedToBank() ? 200 : 220));
		this.buttonToggleBankLink.visible = screen.hasPermission(Permissions.BANK_LINK);
		
		this.buttonSavePersistentTrader = screen.addRenderableTabWidget(new IconButton(screen.guiLeft() + 10, screen.guiTop() + screen.ySize - 30, this::SavePersistentTraderData, IconAndButtonUtil.ICON_PERSISTENT_DATA, IconAndButtonUtil.TOOLTIP_PERSISTENT_TRADER));
		this.buttonSavePersistentTrader.visible = CommandLCAdmin.isAdminPlayer(this.getPlayer());
		
		
		int idWidth = this.getFont().width(EasyText.translatable("gui.lightmanscurrency.settings.persistent.id"));
		this.persistentTraderIDInput = screen.addRenderableTabWidget(new TextFieldWidget(this.getFont(), screen.guiLeft() + 37 + idWidth, screen.guiTop() + screen.ySize - 30, 108 - idWidth, 18, EasyText.empty()));
		
		int ownerWidth = this.getFont().width(EasyText.translatable("gui.lightmanscurrency.settings.persistent.owner"));
		this.persistentTraderOwnerInput = screen.addRenderableTabWidget(new TextFieldWidget(this.getFont(), screen.guiLeft() + 12 + ownerWidth, screen.guiTop() + screen.ySize - 55, 178 - ownerWidth, 18, EasyText.empty()));
		
		this.tick();
		
	}

	@Override
	public void preRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
		TraderSettingsScreen screen = this.getScreen();
		TraderData trader = this.getScreen().getTrader();
		
		screen.getFont().draw(pose, EasyText.translatable("gui.lightmanscurrency.customname"), screen.guiLeft() + 20, screen.guiTop() + 15, 0x404040);
		
		if(screen.hasPermission(Permissions.BANK_LINK))
			this.getFont().draw(pose, EasyText.translatable("gui.lightmanscurrency.settings.banklink"), screen.guiLeft() + 32, screen.guiTop() + 101, 0x404040);
		
		//Draw current trade count
		if(CommandLCAdmin.isAdminPlayer(this.getScreen().getPlayer()) && trader != null)
		{
			String count = String.valueOf(trader.getTradeCount());
			int width = this.getFont().width(count);
			this.getFont().draw(pose, count, screen.guiLeft() + 164 - width, screen.guiTop() + screen.ySize - 25, 0x404040);
			
			if(this.persistentTraderIDInput != null)
			{
				//Draw ID input label
				this.getFont().draw(pose, EasyText.translatable("gui.lightmanscurrency.settings.persistent.id"), screen.guiLeft() + 35, screen.guiTop() + screen.ySize - 25, 0xFFFFFF);
				//Draw Owner input label
				this.getFont().draw(pose, EasyText.translatable("gui.lightmanscurrency.settings.persistent.owner"), screen.guiLeft() + 10, screen.guiTop() + screen.ySize - 50, 0xFFFFFF);
				
			}
			
		}
		
	}
	
	@Override
	public void postRender(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
		TraderSettingsScreen screen = this.getScreen();

		IconAndButtonUtil.renderButtonTooltips(matrix, mouseX, mouseY, Lists.newArrayList(this.buttonToggleCreative, this.buttonSavePersistentTrader));
		
		//Render button tooltips
		if(this.buttonAddTrade.isMouseOver(mouseX, mouseY))
		{
			screen.renderTooltip(matrix, EasyText.translatable("tooltip.lightmanscurrency.trader.creative.addTrade"), mouseX, mouseY);
		}
		else if(this.buttonRemoveTrade.isMouseOver(mouseX, mouseY))
		{
			screen.renderTooltip(matrix, EasyText.translatable("tooltip.lightmanscurrency.trader.creative.removeTrade"), mouseX, mouseY);
		}
		
	}

	@Override
	public void tick() {
		
		boolean canChangeName = this.getScreen().hasPermission(Permissions.CHANGE_NAME);
		this.nameInput.setEditable(canChangeName);
		this.nameInput.tick();
		
		TraderData trader = this.getTrader();
		
		this.buttonSetName.active = !this.nameInput.getValue().contentEquals(trader.getCustomName());
		this.buttonSetName.visible = canChangeName;
		this.buttonResetName.active = trader.hasCustomName();
		this.buttonResetName.visible = canChangeName;
		
		boolean isAdmin = CommandLCAdmin.isAdminPlayer(this.getPlayer());
		this.buttonToggleCreative.visible = isAdmin;
		if(this.buttonToggleCreative.visible)
		{
			this.buttonAddTrade.visible = true;
			this.buttonAddTrade.active = trader.getTradeCount() < TraderData.GLOBAL_TRADE_LIMIT;
			this.buttonRemoveTrade.visible = true;
			this.buttonRemoveTrade.active = trader.getTradeCount() > 1;
		}
		else
		{
			this.buttonAddTrade.visible = false;
			this.buttonRemoveTrade.visible = false;
		}
		
		boolean canLinkAccount = this.getScreen().hasPermission(Permissions.BANK_LINK);
		this.buttonToggleBankLink.visible = canLinkAccount;
		if(canLinkAccount)
		{
			this.buttonToggleBankLink.setResource(TraderSettingsScreen.GUI_TEXTURE, 10, trader.getLinkedToBank() ? 200 : 220);
			this.buttonToggleBankLink.active = trader.canLinkBankAccount() || trader.getLinkedToBank();
		}
		
		
		if(this.buttonSavePersistentTrader != null)
		{
			this.buttonSavePersistentTrader.visible = isAdmin;
			this.buttonSavePersistentTrader.active = trader.hasValidTrade();
		}
		if(this.persistentTraderIDInput != null)
		{
			this.persistentTraderIDInput.visible = isAdmin;
			this.persistentTraderIDInput.tick();
		}
		if(this.persistentTraderOwnerInput != null)
		{
			this.persistentTraderOwnerInput.visible = isAdmin;
			this.persistentTraderOwnerInput.tick();
		}
			
	}

	@Override
	public void closeTab() { }

	@Override
	public int getColor() {
		return 0xFFFFFF;
	}

	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(ModItems.TRADING_CORE); }
	
	@Override
	public ITextComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.settings.name"); }

	private void SetName(Button button)
	{
		TraderData trader = this.getTrader();
		String customName = trader.getCustomName();
		if(!customName.contentEquals(this.nameInput.getValue()))
		{
			CompoundNBT message = new CompoundNBT();
			message.putString("ChangeName", this.nameInput.getValue());
			this.sendNetworkMessage(message);
			//LightmansCurrency.LogInfo("Sent 'Change Name' message with value:" + this.nameInput.getValue());
		}
	}
	
	private void ResetName(Button button)
	{
		this.nameInput.setValue("");
		this.SetName(button);
	}
	
	private void ToggleCreative(Button button)
	{
		TraderData trader = this.getTrader();
		CompoundNBT message = new CompoundNBT();
		message.putBoolean("MakeCreative", !trader.isCreative());
		this.sendNetworkMessage(message);
	}
	
	private void ToggleBankLink(Button button)
	{
		TraderData trader = this.getTrader();
		CompoundNBT message = new CompoundNBT();
		message.putBoolean("LinkToBankAccount", !trader.getLinkedToBank());
		this.sendNetworkMessage(message);
	}
	
	private void AddTrade(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveTrade(this.getTrader().getID(), true));
	}
	
	private void RemoveTrade(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveTrade(this.getTrader().getID(), false));
	}
	
	private void SavePersistentTraderData(Button button)
	{
		TraderData trader = this.getScreen().getTrader();
		if(trader != null && trader.canMakePersistent()) 
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddPersistentTrader(trader.getID(), this.persistentTraderIDInput.getValue(), this.persistentTraderOwnerInput.getValue()));
	}
	
}