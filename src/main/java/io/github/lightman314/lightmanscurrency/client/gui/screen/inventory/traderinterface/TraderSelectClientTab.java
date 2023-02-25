package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.common.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderInterfaceBlockEntity.InteractionType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradingTerminalScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.UniversalTraderButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.terminal.filters.TraderSearchFilter;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base.TraderSelectTab;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

public class TraderSelectClientTab extends TraderInterfaceClientTab<TraderSelectTab> implements IScrollable{

	public TraderSelectClientTab(TraderInterfaceScreen screen, TraderSelectTab tab) { super(screen,tab); }

	@Override
	public @NotNull IconData getIcon() { return IconData.of(ModBlocks.TERMINAL); }

	@Override
	public MutableComponent getTooltip() { return Component.translatable("tooltip.lightmanscurrency.interface.trader"); }

	@Override
	public boolean blockInventoryClosing() { return true; }
	
	EditBox searchField;
	
	ScrollBarWidget scrollBar;
	
	List<UniversalTraderButton> traderButtons;
	
	private int scroll;
	
	private List<TraderData> filteredTraderList = new ArrayList<>();
	
	private List<TraderData> traderList() {
		List<TraderData> traderList = this.filterTraders(TraderSaveData.GetAllTerminalTraders(true));
		traderList.sort(TradingTerminalScreen.TERMINAL_SORTER);
		return traderList;
	}
	
	private List<TraderData> filterTraders(List<TraderData> allTraders) {
		List<TraderData> traders = new ArrayList<>();
		TraderInterfaceBlockEntity be = this.menu.getBE();
		if(be == null)
			return traders;
		InteractionType interaction = be.getInteractionType();
		for(TraderData trader : allTraders) {
			//Confirm that the trader is the trade type that our interface is compatible with.
			if(be.validTraderType(trader))
			{
				//Confirm that the trader either has a valid trade, or we have interaction permissions
				if((interaction.trades && trader.hasValidTrade()) || (interaction.requiresPermissions && be.hasTraderPermissions(trader)))
					traders.add(trader);
			}
		}
		return traders;
	}
	
	@Override
	public void onOpen() {
		
		this.searchField = this.screen.addRenderableTabWidget(new EditBox(this.font, this.screen.getGuiLeft() + 43, this.screen.getGuiTop() + 6, 101, 9, Component.translatable("gui.lightmanscurrency.terminal.search")));
		this.searchField.setBordered(false);
		this.searchField.setMaxLength(32);
		this.searchField.setTextColor(0xFFFFFF);
		
		this.initTraderButtons(this.screen.getGuiLeft(), this.screen.getGuiTop());
		
		this.scrollBar = this.screen.addRenderableTabWidget(new ScrollBarWidget(this.screen.getGuiLeft() + 30 + UniversalTraderButton.WIDTH, this.screen.getGuiTop() + 18, UniversalTraderButton.HEIGHT * 4, this));
		
		this.tick();
		
		this.updateTraderList();
		
		//Automatically go to the page with the currently selected trader.
		TraderData selectedTrader = this.menu.getBE().getTrader();
		if(selectedTrader!= null)
		{
			this.scroll = this.scrollOf(selectedTrader);
			this.updateTraderButtons();
		}
		
		this.screen.addTabListener(new ScrollListener(0,0, this.screen.width, this.screen.height, this::onMouseScrolled));
			
		
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
		
		this.scrollBar.beforeWidgetRender(mouseY);
		
	}

	@Override
	public void renderTooltips(PoseStack pose, int mouseX, int mouseY) {
		
	}
	
	@Override
	public void tick() {
		
		this.searchField.tick();
		
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
	
	private void SelectTrader(Button button) {
		int index = getTraderIndex(button);
		if(index >= 0 && index < this.filteredTraderList.size())
		{
			long traderID = this.filteredTraderList.get(index).getID();
			this.commonTab.setTrader(traderID);
		}
	}
	
	private int getTraderIndex(Button button) {
		if(!traderButtons.contains(button))
			return -1;
		int index = traderButtons.indexOf(button);
		index += this.scroll;
		return index;
	}
	
	public int getMaxScroll() { return Math.max(this.filteredTraderList.size() - this.traderButtons.size(), 0); }
	
	private int scrollOf(TraderData trader) {
		if(this.filteredTraderList != null)
		{
			int index = this.filteredTraderList.indexOf(trader);
			if(index >= 0)
				return Math.min(index, this.getMaxScroll());
			return this.scroll;
		}
		return this.scroll;
	}
	
	private void updateTraderList()
	{
		//Filtering of results moved to the TradingOffice.filterTraders
		this.filteredTraderList = TraderSearchFilter.FilterTraders(this.traderList(), this.searchField.getValue());
		this.updateTraderButtons();
		//Limit the page
		if(this.scroll > this.getMaxScroll())
			this.scroll = this.getMaxScroll();
	}
	
	private void updateTraderButtons()
	{
		int startIndex = this.scroll;
		for(int i = 0; i < this.traderButtons.size(); ++i)
		{
			if(startIndex + i < this.filteredTraderList.size())
				this.traderButtons.get(i).SetData(this.filteredTraderList.get(startIndex + i));
			else
				this.traderButtons.get(i).SetData(null);
		}
	}

	@Override
	public int currentScroll() { return this.scroll; }

	@Override
	public void setScroll(int newScroll) {
		this.scroll = Math.min(newScroll, this.getMaxScroll());
		this.updateTraderButtons();
	}
	
	private boolean onMouseScrolled(double mouseX, double mouseY, double delta) {
		if(delta < 0)
		{			
			if(this.scroll < this.getMaxScroll())
				this.setScroll(this.scroll + 1);
		}
		else if(delta > 0)
		{
			if(this.scroll > 0)
				this.setScroll(this.scroll - 1);
		}
		return false;
	}
	
}
