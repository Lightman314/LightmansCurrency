package io.github.lightman314.lightmanscurrency.client.gui.settings.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.CoreTraderSettings;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class MainTab extends SettingsTab{

	public static final MainTab INSTANCE = new MainTab();
	
	/**
	 * Main Tab contains the Trader Name & Creative Settings
	 */
	private MainTab() { }
	
	EditBox nameInput;
	Button buttonSetName;
	Button buttonResetName;
	
	PlainButton buttonToggleBankLink;
	
	IconButton buttonToggleCreative;
	Button buttonAddTrade;
	Button buttonRemoveTrade;
	
	Button buttonSavePersistentTrader;
	
	@Override
	public ImmutableList<String> requiredPermissions() {
		return ImmutableList.of();
	}

	@Override
	public void initTab() {
		
		TraderSettingsScreen screen = this.getScreen();
		
		CoreTraderSettings coreSettings = this.getSetting(CoreTraderSettings.class);
		
		this.nameInput = screen.addRenderableTabWidget(new EditBox(screen.getFont(), screen.guiLeft() + 20, screen.guiTop() + 25, 160, 20, new TextComponent("")));
		this.nameInput.setMaxLength(32);
		this.nameInput.setValue(coreSettings.getCustomName());
		
		this.buttonSetName = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 20, screen.guiTop() + 50, 74, 20, new TranslatableComponent("gui.lightmanscurrency.changename"), this::SetName));
		this.buttonResetName = screen.addRenderableTabWidget(new Button(screen.guiLeft() + screen.xSize - 93, screen.guiTop() + 50, 74, 20, new TranslatableComponent("gui.lightmanscurrency.resetname"), this::ResetName));
		
		//Creative Toggle
		this.buttonToggleCreative = screen.addRenderableTabWidget(IconAndButtonUtil.creativeToggleButton(screen.guiLeft() + 176, screen.guiTop() + 4, this::ToggleCreative, () -> this.getScreen().getSetting(CoreTraderSettings.class).isCreative()));
		this.buttonAddTrade = screen.addRenderableTabWidget(new PlainButton(screen.guiLeft() + 166, screen.guiTop() + 4, 10, 10, this::AddTrade, TraderSettingsScreen.GUI_TEXTURE, 0, 200));
		this.buttonRemoveTrade = screen.addRenderableTabWidget(new PlainButton(screen.guiLeft() + 166, screen.guiTop() + 14, 10, 10, this::RemoveTrade, TraderSettingsScreen.GUI_TEXTURE, 0, 220));
		
		this.buttonToggleBankLink = screen.addRenderableTabWidget(new PlainButton(screen.guiLeft() + 20, screen.guiTop() + 100, 10, 10, this::ToggleBankLink, TraderSettingsScreen.GUI_TEXTURE, 10, coreSettings.isBankAccountLinked() ? 200 : 220));
		this.buttonToggleBankLink.visible = screen.hasPermission(Permissions.BANK_LINK);
		
		if(this.getScreen().getTrader() instanceof UniversalTraderData)
		{
			this.buttonSavePersistentTrader = screen.addRenderableTabWidget(new IconButton(screen.guiLeft() + 10, screen.guiTop() + screen.ySize - 30, this::SavePersistentTraderData, IconAndButtonUtil.ICON_PERSISTENT_DATA, IconAndButtonUtil.TOOLTIP_PERSISTENT_DATA));
			this.buttonSavePersistentTrader.visible = TradingOffice.isAdminPlayer(this.getPlayer());
		}
		
		
		this.tick();
		
	}

	@Override
	public void preRender(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		
		TraderSettingsScreen screen = this.getScreen();
		
		screen.getFont().draw(matrix, new TranslatableComponent("gui.lightmanscurrency.customname"), screen.guiLeft() + 20, screen.guiTop() + 15, 0x404040);
		
		if(screen.hasPermission(Permissions.BANK_LINK))
			this.getFont().draw(matrix, new TranslatableComponent("gui.lightmanscurrency.settings.banklink"), screen.guiLeft() + 32, screen.guiTop() + 101, 0x404040);
		
	}
	
	@Override
	public void postRender(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		TraderSettingsScreen screen = this.getScreen();
		
		IconAndButtonUtil.renderButtonTooltips(matrix, mouseX, mouseY, Lists.newArrayList(this.buttonToggleCreative, this.buttonSavePersistentTrader));
		
		//Render button tooltips
		if(this.buttonAddTrade.isMouseOver(mouseX, mouseY))
		{
			screen.renderTooltip(matrix, new TranslatableComponent("tooltip.lightmanscurrency.trader.creative.addTrade"), mouseX, mouseY);
		}
		else if(this.buttonRemoveTrade.isMouseOver(mouseX, mouseY))
		{
			screen.renderTooltip(matrix, new TranslatableComponent("tooltip.lightmanscurrency.trader.creative.removeTrade"), mouseX, mouseY);
		}
		
	}

	@Override
	public void tick() {
		
		boolean canChangeName = this.getScreen().hasPermission(Permissions.CHANGE_NAME);
		this.nameInput.setEditable(canChangeName);
		this.nameInput.tick();
		
		CoreTraderSettings coreSettings = this.getSetting(CoreTraderSettings.class);
		
		this.buttonSetName.active = !this.nameInput.getValue().contentEquals(coreSettings.getCustomName());
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
		
		if(this.buttonSavePersistentTrader != null)
			this.buttonSavePersistentTrader.visible = TradingOffice.isAdminPlayer(this.getPlayer());
		
	}

	@Override
	public void closeTab() { }

	@Override
	public int getColor() {
		return 0xFFFFFF;
	}

	@Override
	public IconData getIcon() { return IconData.of(ModItems.TRADING_CORE); }
	
	@Override
	public Component getTooltip() { return new TranslatableComponent("tooltip.lightmanscurrency.settings.name"); }

	private void SetName(Button button)
	{
		CoreTraderSettings coreSettings = this.getSetting(CoreTraderSettings.class);
		String customName = coreSettings.getCustomName();
		if(!customName.contentEquals(this.nameInput.getValue()))
		{
			CompoundTag updateInfo = coreSettings.setCustomName(this.getPlayer(), this.nameInput.getValue());
			coreSettings.sendToServer(updateInfo);
		}
	}
	
	private void ResetName(Button button)
	{
		this.nameInput.setValue("");
		this.SetName(button);
	}
	
	private void ToggleCreative(Button button)
	{
		CoreTraderSettings coreSettings = this.getScreen().getSetting(CoreTraderSettings.class);
		CompoundTag updateInfo = coreSettings.toggleCreative(this.getPlayer());
		coreSettings.sendToServer(updateInfo);
	}
	
	private void ToggleBankLink(Button button)
	{
		CoreTraderSettings coreSettings = this.getSetting(CoreTraderSettings.class);
		CompoundTag updateInfo = coreSettings.toggleBankAccountLink(this.getPlayer());
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
	
	@SuppressWarnings("resource")
	private void SavePersistentTraderData(Button button)
	{
		try {
			ITrader t = this.getScreen().getTrader();
			if(t instanceof UniversalTraderData) {
				UniversalTraderData trader = (UniversalTraderData)t;
				JsonObject result = trader.saveToJson(new JsonObject());
				//Copy text to clipboard
				String resultString = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(result);
				LightmansCurrency.LogInfo("Copied persistent trader json to clipboard.\n" + resultString);
				this.getScreen().getMinecraft().keyboardHandler.setClipboard(resultString);
				TranslatableComponent message = new TranslatableComponent("lightmanscurrency.chat.persistenttrader");
				message.setStyle(message.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, resultString)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("tooltip.lightmanscurrency.persistenttrader.copyagain", trader.getName()))));
				this.getScreen().getMinecraft().player.displayClientMessage(message, false);
			}
		} catch(Throwable e) { LightmansCurrency.LogError("Error saving trader to Json.", e); }
	}
	
}
