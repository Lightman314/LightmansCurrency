package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderInterfaceBlockEntity.InteractionType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.NetworkTerminalScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.NetworkTraderButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.terminal.filters.TraderSearchFilter;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base.TraderSelectTab;
import io.github.lightman314.lightmanscurrency.common.traders.terminal.sorting.TerminalSorter;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class TraderSelectClientTab extends TraderInterfaceClientTab<TraderSelectTab> implements IScrollable {

	public TraderSelectClientTab(TraderInterfaceScreen screen, TraderSelectTab tab) { super(screen,tab); }

	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(ModBlocks.TERMINAL); }

	@Override
	public MutableComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.interface.trader"); }

	@Override
	public boolean blockInventoryClosing() { return true; }
	
	EditBox searchField;
	
	ScrollBarWidget scrollBar;
	
	List<NetworkTraderButton> traderButtons;
	
	private int scroll;
	
	private List<TraderData> filteredTraderList = new ArrayList<>();

	String previousInput = "";
	
	private List<TraderData> traderList() {
		List<TraderData> traderList = this.filterTraders(TraderSaveData.GetAllTerminalTraders(true));
		traderList.sort(TerminalSorter.getDefaultSorter());
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
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		
		this.searchField = this.addChild(new EditBox(this.getFont(), screenArea.x + 43, screenArea.y + 6, 101, 9, EasyText.translatable("gui.lightmanscurrency.terminal.search")));
		this.searchField.setBordered(false);
		this.searchField.setMaxLength(32);
		this.searchField.setTextColor(0xFFFFFF);
		if(firstOpen)
			this.previousInput = "";
		else
			this.searchField.setValue(this.previousInput);
		
		this.initTraderButtons(screenArea.pos);
		
		this.scrollBar = this.addChild(new ScrollBarWidget(screenArea.pos.offset(30 + NetworkTraderButton.WIDTH, 18), NetworkTraderButton.HEIGHT * 4, this));
		
		this.tick();
		
		this.updateTraderList();
		
		//Automatically go to the page with the currently selected trader.
		TraderData selectedTrader = this.menu.getBE().getTrader();
		if(selectedTrader!= null)
		{
			this.scroll = this.scrollOf(selectedTrader);
			this.updateTraderButtons();
		}
		
		this.addChild(new ScrollListener(0,0, this.screen.width, this.screen.height, this));
			
		
	}
	
	private void initTraderButtons(ScreenPosition corner)
	{
		this.traderButtons = new ArrayList<>();
		for(int y = 0; y < 4; ++y)
		{
			NetworkTraderButton newButton = this.addChild(new NetworkTraderButton(corner.offset(30, 18 + (y * NetworkTraderButton.HEIGHT)), this::SelectTrader));
			this.traderButtons.add(newButton);
		}
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {
		
		RenderSystem.setShaderTexture(0, TraderInterfaceScreen.GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		gui.blit(TraderInterfaceScreen.GUI_TEXTURE,  28, 4, 0, TraderInterfaceScreen.HEIGHT, 117, 12);
		
	}
	
	@Override
	public void tick() {

		for (NetworkTraderButton button : this.traderButtons) {
			button.selected = button.getData() != null && button.getData() == this.screen.getMenu().getBE().getTrader();
		}

		if(!Objects.equals(this.previousInput, this.searchField.getValue()))
		{
			this.previousInput = this.searchField.getValue();
			this.updateTraderList();
		}

	}
	
	private void SelectTrader(EasyButton button) {
		int index = getTraderIndex(button);
		if(index >= 0 && index < this.filteredTraderList.size())
		{
			long traderID = this.filteredTraderList.get(index).getID();
			this.commonTab.setTrader(traderID);
		}
	}
	
	private int getTraderIndex(EasyButton button) {
		if(button instanceof NetworkTraderButton)
		{
			if(!traderButtons.contains(button))
				return -1;
			int index = traderButtons.indexOf(button);
			index += this.scroll;
			return index;
		}
		return -1;
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
