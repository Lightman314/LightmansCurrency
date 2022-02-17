package io.github.lightman314.lightmanscurrency.client.gui.settings.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.CoreTraderSettings;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

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
	
	@Override
	public ImmutableList<String> requiredPermissions() {
		return ImmutableList.of(Permissions.EDIT_SETTINGS);
	}

	@Override
	public void initTab() {
		
		TraderSettingsScreen screen = this.getScreen();
		
		CoreTraderSettings coreSettings = this.getSetting(CoreTraderSettings.class);
		
		this.nameInput = screen.addRenderableTabWidget(new TextFieldWidget(screen.getFont(), screen.guiLeft() + 20, screen.guiTop() + 25, 160, 20, new StringTextComponent("")));
		this.nameInput.setMaxStringLength(32);
		this.nameInput.setText(coreSettings.getCustomName());
		
		this.buttonSetName = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 20, screen.guiTop() + 50, 74, 20, new TranslationTextComponent("gui.lightmanscurrency.changename"), this::SetName));
		this.buttonResetName = screen.addRenderableTabWidget(new Button(screen.guiLeft() + screen.xSize - 93, screen.guiTop() + 50, 74, 20, new TranslationTextComponent("gui.lightmanscurrency.resetname"), this::ResetName));
		
		//Creative Toggle
		this.buttonToggleCreative = screen.addRenderableTabWidget(IconAndButtonUtil.creativeToggleButton(screen.guiLeft() + 176, screen.guiTop() + 4, this::ToggleCreative, () -> this.getScreen().getSetting(CoreTraderSettings.class).isCreative()));
		this.buttonAddTrade = screen.addRenderableTabWidget(new PlainButton(screen.guiLeft() + 166, screen.guiTop() + 4, 10, 10, this::AddTrade, TraderSettingsScreen.GUI_TEXTURE, 0, 200));
		this.buttonRemoveTrade = screen.addRenderableTabWidget(new PlainButton(screen.guiLeft() + 166, screen.guiTop() + 14, 10, 10, this::RemoveTrade, TraderSettingsScreen.GUI_TEXTURE, 0, 220));
		
		this.buttonToggleBankLink = screen.addRenderableTabWidget(new PlainButton(screen.guiLeft() + 20, screen.guiTop() + 100, 10, 10, this::ToggleBankLink, TraderSettingsScreen.GUI_TEXTURE, 10, coreSettings.isBankAccountLinked() ? 200 : 220));
		this.buttonToggleBankLink.visible = screen.hasPermission(Permissions.BANK_LINK);
		
		this.tick();
		
	}

	@Override
	public void preRender(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
		
		TraderSettingsScreen screen = this.getScreen();
		
		screen.getFont().drawString(matrix, new TranslationTextComponent("gui.lightmanscurrency.customname").getString(), screen.guiLeft() + 20, screen.guiTop() + 15, 0x404040);
		
		if(screen.hasPermission(Permissions.BANK_LINK))
			this.getFont().drawString(matrix, new TranslationTextComponent("gui.lightmanscurrency.settings.banklink").getString(), screen.guiLeft() + 30, screen.guiTop() + 101, 0x404040);
		
	}
	
	@Override
	public void postRender(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
		TraderSettingsScreen screen = this.getScreen();
		
		IconAndButtonUtil.renderButtonTooltips(matrix, mouseX, mouseY, Lists.newArrayList(this.buttonToggleCreative));
		
		//Render button tooltips
		if(this.buttonAddTrade.isMouseOver(mouseX, mouseY))
		{
			screen.renderTooltip(matrix, new TranslationTextComponent("tooltip.lightmanscurrency.trader.creative.addTrade"), mouseX, mouseY);
		}
		else if(this.buttonRemoveTrade.isMouseOver(mouseX, mouseY))
		{
			screen.renderTooltip(matrix, new TranslationTextComponent("tooltip.lightmanscurrency.trader.creative.removeTrade"), mouseX, mouseY);
		}
		
	}

	@Override
	public void tick() {
		
		boolean canChangeName = this.getScreen().hasPermission(Permissions.CHANGE_NAME);
		this.nameInput.setEnabled(canChangeName);
		this.nameInput.tick();
		
		CoreTraderSettings coreSettings = this.getSetting(CoreTraderSettings.class);
		
		this.buttonSetName.active = !this.nameInput.getText().contentEquals(coreSettings.getCustomName());
		this.buttonSetName.visible = canChangeName;
		this.buttonResetName.active = coreSettings.hasCustomName();
		this.buttonResetName.visible = canChangeName;
		
		this.buttonToggleCreative.visible = TradingOffice.isAdminPlayer(this.getScreen().getPlayer());
		if(this.buttonToggleCreative.visible)
		{
			IconAndButtonUtil.updateCreativeToggleButton(this.buttonToggleCreative, coreSettings.isCreative());
			if(coreSettings.isCreative())
			{
				ITrader trader = this.getScreen().getTrader();
				this.buttonAddTrade.visible = true;
				this.buttonAddTrade.active = trader.getTradeCount() < trader.getTradeCountLimit();
				this.buttonRemoveTrade.visible = true;
				this.buttonRemoveTrade.active = trader.getTradeCount() > 1;
			}
			else
			{
				this.buttonAddTrade.visible = false;
				this.buttonRemoveTrade.visible = false;
			}
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
			this.buttonToggleBankLink.setResource(TraderSettingsScreen.GUI_TEXTURE, 10, coreSettings.isBankAccountLinked() ? 200 : 220);
			this.buttonToggleBankLink.active = coreSettings.canLinkBankAccount() || coreSettings.isBankAccountLinked();
		}
		
	}

	@Override
	public void closeTab() { }

	@Override
	public int getColor() {
		return 0xFFFFFF;
	}

	@Override
	public IconData getIcon() { return IconData.of(new ItemStack(ModItems.TRADING_CORE)); }
	
	@Override
	public ITextComponent getTooltip() { return new TranslationTextComponent("tooltip.lightmanscurrency.settings.name"); }

	private void SetName(Button button)
	{
		CoreTraderSettings coreSettings = this.getSetting(CoreTraderSettings.class);
		String customName = coreSettings.getCustomName();
		if(!customName.contentEquals(this.nameInput.getText()))
		{
			CompoundNBT updateInfo = coreSettings.setCustomName(this.getPlayer(), this.nameInput.getText());
			coreSettings.sendToServer(updateInfo);
		}
	}
	
	private void ResetName(Button button)
	{
		this.nameInput.setText("");
		this.SetName(button);
	}
	
	private void ToggleCreative(Button button)
	{
		CoreTraderSettings coreSettings = this.getScreen().getSetting(CoreTraderSettings.class);
		CompoundNBT updateInfo = coreSettings.toggleCreative(this.getPlayer());
		coreSettings.sendToServer(updateInfo);
	}
	
	private void ToggleBankLink(Button button)
	{
		CoreTraderSettings coreSettings = this.getSetting(CoreTraderSettings.class);
		CompoundNBT updateInfo = coreSettings.toggleBankAccountLink(this.getPlayer());
		coreSettings.sendToServer(updateInfo);
	}
	
	private void AddTrade(Button button)
	{
		this.getScreen().getTrader().requestAddOrRemoveTrade(true);
	}
	
	private void RemoveTrade(Button button)
	{
		this.getScreen().getTrader().requestAddOrRemoveTrade(false);
	}
	
}
