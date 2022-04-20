package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.TraderInterfaceBlockEntity.InteractionType;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradingTerminalScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.UniversalTraderButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.menus.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.menus.traderinterface.base.TraderSelectTab;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class TraderSelectClientTab extends TraderInterfaceClientTab<TraderSelectTab> {

	public TraderSelectClientTab(TraderInterfaceScreen screen, TraderSelectTab tab) { super(screen,tab); }

	@Override
	public IconData getIcon() { return IconData.of(ModBlocks.TERMINAL); }

	@Override
	public Component getTooltip() { return new TranslatableComponent("tooltip.lightmanscurrency.interface.trader"); }

	@Override
	public boolean blockInventoryClosing() { return true; }
	
	EditBox searchField;
	
	Button buttonPreviousPage;
	Button buttonNextPage;
	
	List<UniversalTraderButton> traderButtons;
	
	private int page = 0;
	
	private List<UniversalTraderData> filteredTraderList = new ArrayList<>();
	
	private List<UniversalTraderData> traderList() {
		List<UniversalTraderData> traderList = this.filterTraders(ClientTradingOffice.getTraderList());
		traderList.sort(TradingTerminalScreen.TERMINAL_SORTER);
		return traderList;
	}
	
	private List<UniversalTraderData> filterTraders(List<UniversalTraderData> allTraders) {
		List<UniversalTraderData> traders = new ArrayList<>();
		TraderInterfaceBlockEntity be = this.menu.getBE();
		if(be == null)
			return traders;
		InteractionType interaction = be.getInteractionType();
		for(UniversalTraderData trader : allTraders) {
			//Confirm that the trader is the trade type that our interface is compatible with.
			if(be.validTraderType(trader))
			{
				//Confirm that the trader either has a valid trade, or we have interaction permissions
				if((interaction.trades && trader.hasValidTrade()) || (interaction.requiresPermissions && trader.hasPermission(this.menu.getBE().getOwner(), Permissions.INTERACTION_LINK)))
					traders.add(trader);
			}
		}
		return traders;
	}
	
	@Override
	public void onOpen() {
		
		this.searchField = this.screen.addRenderableTabWidget(new EditBox(this.font, this.screen.getGuiLeft() + 43, this.screen.getGuiTop() + 6, 101, 9, new TranslatableComponent("gui.lightmanscurrency.terminal.search")));
		this.searchField.setBordered(false);
		this.searchField.setMaxLength(32);
		this.searchField.setTextColor(0xFFFFFF);
		
		this.buttonPreviousPage = this.screen.addRenderableTabWidget(IconAndButtonUtil.leftButton(this.screen.getGuiLeft(), this.screen.getGuiTop() - 20, this::PreviousPage));
		this.buttonNextPage = this.screen.addRenderableTabWidget(IconAndButtonUtil.rightButton(this.screen.getGuiLeft() + this.screen.getXSize() - 20, this.screen.getGuiTop() - 20, this::NextPage));
		
		this.initTraderButtons(this.screen.getGuiLeft(), this.screen.getGuiTop());
		
		this.page = MathUtil.clamp(page, 0, this.pageLimit());
		
		this.tick();
		
		this.updateTraderList();
		
		//Automatically go to the page with the currently selected trader.
		UniversalTraderData selectedTrader = this.menu.getBE().getTrader();
		if(selectedTrader!= null)
		{
			this.page = this.pageOf(selectedTrader);
			this.updateTraderButtons();
		}
			
		
	}
	
	private void initTraderButtons(int guiLeft, int guiTop)
	{
		this.traderButtons = new ArrayList<>();
		for(int y = 0; y < 4; ++y)
		{
			UniversalTraderButton newButton = this.screen.addRenderableTabWidget(new UniversalTraderButton(guiLeft + 30, guiTop + 18 + (y * UniversalTraderButton.HEIGHT), this::SelectTrader, this.font));
			this.traderButtons.add(newButton);
		}
	}

	@Override
	public void renderBG(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		RenderSystem.setShaderTexture(0, TraderInterfaceScreen.GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		this.screen.blit(pose, this.screen.getGuiLeft() + 28, this.screen.getGuiTop() + 4, 0, TraderInterfaceScreen.HEIGHT, 117, 12);
		
	}

	@Override
	public void renderTooltips(PoseStack pose, int mouseX, int mouseY) {
		
	}
	
	@Override
	public void tick() {
		
		this.searchField.tick();
		
		this.buttonPreviousPage.visible = this.pageLimit() > 0;
		this.buttonPreviousPage.active = page > 0;
		this.buttonNextPage.visible = this.pageLimit() > 0;
		this.buttonNextPage.active = page < this.pageLimit();
		
		for(int i = 0; i < this.traderButtons.size(); ++i)
		{
			UniversalTraderButton button = this.traderButtons.get(i);
			if(button.getData() != null && button.getData() == this.screen.getMenu().getBE().getTrader())
				button.selected = true;
			else
				button.selected = false;
		}
	}
	
	@Override
	public boolean charTyped(char c, int code)
	{
		String s = this.searchField.getValue();
		if(this.searchField.charTyped(c, code))
		{
			if(!Objects.equals(s, this.searchField.getValue()))
			{
				this.updateTraderList();
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean keyPressed(int key, int scanCode, int mods)
	{
		String s = this.searchField.getValue();
		if(this.searchField.keyPressed(key, scanCode, mods))
		{
			if(!Objects.equals(s,  this.searchField.getValue()))
			{
				this.updateTraderList();
			}
			return true;
		}
		return false;
	}
	
	private void PreviousPage(Button button) {
		if(this.page > 0)
		{
			this.page--;
			this.updateTraderButtons();
		}
	}
	
	private void NextPage(Button button) {
		if(this.page < this.pageLimit())
		{
			this.page++;
			this.updateTraderButtons();
		}
	}
	
	private void SelectTrader(Button button) {
		int index = getTraderIndex(button);
		if(index >= 0 && index < this.filteredTraderList.size())
		{
			UUID traderID = this.filteredTraderList.get(index).getTraderID();
			this.commonTab.setTrader(traderID);
		}
	}
	
	private int getTraderIndex(Button button) {
		if(!traderButtons.contains(button))
			return -1;
		int index = traderButtons.indexOf(button);
		index += page * this.traderButtons.size();
		return index;
	}
	
	private int pageLimit() {
		return (this.filteredTraderList.size() - 1) / this.traderButtons.size();
	}
	
	private int pageOf(UniversalTraderData trader) {
		if(this.filteredTraderList != null)
		{
			int index = this.filteredTraderList.indexOf(trader);
			if(index >= 0)
				return index / this.traderButtons.size();
			return this.page;
		}
		return this.page;
	}
	
	private void updateTraderList()
	{
		//Filtering of results moved to the TradingOffice.filterTraders
		this.filteredTraderList = TradingOffice.filterTraders(this.searchField.getValue(), this.traderList());
		this.updateTraderButtons();
		//Limit the page
		if(page > pageLimit())
			page = pageLimit();
	}
	
	private void updateTraderButtons()
	{
		int startIndex = page * this.traderButtons.size();
		for(int i = 0; i < this.traderButtons.size(); ++i)
		{
			if(startIndex + i < this.filteredTraderList.size())
				this.traderButtons.get(i).SetData(this.filteredTraderList.get(startIndex + i));
			else
				this.traderButtons.get(i).SetData(null);
		}
	}
	
	
}
