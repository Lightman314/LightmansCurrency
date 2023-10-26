package io.github.lightman314.lightmanscurrency.client.gui.screen;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.NetworkTraderButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.TerminalMenu;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.terminal.filters.TraderSearchFilter;
import io.github.lightman314.lightmanscurrency.common.traders.terminal.sorting.TerminalSorter;
import io.github.lightman314.lightmanscurrency.network.message.trader.CPacketOpenTrades;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class NetworkTerminalScreen extends EasyMenuScreen<TerminalMenu> implements IScrollable {
	
	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/trader_selection.png");
	
	private EditBox searchField;
	private static int scroll = 0;
	
	ScrollBarWidget scrollBar;
	
	List<NetworkTraderButton> traderButtons;
	
	private List<TraderData> traderList(){
		List<TraderData> traderList = TraderSaveData.GetAllTerminalTraders(true);
		//No longer need to remove the auction house, as the 'showInTerminal' function now confirms the auction houses enabled/visible status.
		//traderList.removeIf(d -> d instanceof AuctionHouseTrader && !Config.SERVER.enableAuctionHouse.get());
		traderList.sort(TerminalSorter.getDefaultSorter());
		return traderList;
	}
	private List<TraderData> filteredTraderList = new ArrayList<>();

	public NetworkTerminalScreen(TerminalMenu menu, Inventory inventory, Component ignored) {
		super(menu, inventory, EasyText.translatable("block.lightmanscurrency.terminal"));
		this.resize(176,187);
	}

	@Override
	protected void initialize(ScreenArea screenArea)
	{

		this.searchField = this.addChild(new EditBox(this.font, screenArea.x + 28, screenArea.y + 6, 101, 9, this.searchField, EasyText.translatable("gui.lightmanscurrency.terminal.search")));
		this.searchField.setBordered(false);
		this.searchField.setMaxLength(32);
		this.searchField.setTextColor(0xFFFFFF);
		this.searchField.setResponder(this::updateTraderList);
		
		this.scrollBar = this.addChild(new ScrollBarWidget(screenArea.pos.offset(16 + NetworkTraderButton.WIDTH, 17), NetworkTraderButton.HEIGHT * 5 + 2, this));
		
		this.initTraderButtons(screenArea);
		
		this.tick();
		
		this.updateTraderList(this.searchField.getValue());
		
		this.validateScroll();
		
	}
	
	private void initTraderButtons(ScreenArea screenArea)
	{
		this.traderButtons = new ArrayList<>();
		for(int y = 0; y < 5; y++)
		{
			NetworkTraderButton newButton = this.addChild(new NetworkTraderButton(screenArea.pos.offset(15, 18 + (y * NetworkTraderButton.HEIGHT)), this::OpenTrader));
			this.traderButtons.add(newButton);
		}
	}
	
	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui)
	{

		//Render the background
		gui.renderNormalBackground(GUI_TEXTURE, this);
		
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if(this.handleScrollWheel(delta))
			return true;
		return super.mouseScrolled(mouseX, mouseY, delta);
	}
	
	private void OpenTrader(EasyButton button)
	{
		int index = getTraderIndex(button);
		if(index >= 0 && index < this.filteredTraderList.size())
			new CPacketOpenTrades(this.filteredTraderList.get(index).getID()).send();
	}
	
	private int getTraderIndex(EasyButton button)
	{
		if(button instanceof NetworkTraderButton && this.traderButtons.contains(button))
			return traderButtons.indexOf(button) + scroll;
		return -1;
	}
	
	private void updateTraderList(String searchString)
	{
		//Filtering of results moved to the TradingOffice.filterTraders
		this.filteredTraderList = searchString.isBlank() ? this.traderList() : TraderSearchFilter.FilterTraders(this.traderList(), searchString);
		//Validate the scroll
		this.validateScroll();
		//Update the trader buttons
		this.updateTraderButtons();
	}
	
	private void updateTraderButtons()
	{
		int startIndex = scroll;
		for(int i = 0; i < this.traderButtons.size(); i++)
		{
			if(startIndex + i < this.filteredTraderList.size())
				this.traderButtons.get(i).SetData(this.filteredTraderList.get(startIndex + i));
			else
				this.traderButtons.get(i).SetData(null);
		}
	}
	
	@Override
	public int currentScroll() { return scroll; }

	@Override
	public void setScroll(int newScroll) {
		scroll = newScroll;
		this.updateTraderButtons();
	}

	@Override
	public int getMaxScroll() {
		return Math.max(0, this.filteredTraderList.size() - this.traderButtons.size());
	}

}
