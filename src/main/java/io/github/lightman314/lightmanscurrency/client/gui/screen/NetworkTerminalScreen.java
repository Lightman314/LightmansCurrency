package io.github.lightman314.lightmanscurrency.client.gui.screen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.terminal.filters.TraderSearchFilter;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenTrades;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

public class NetworkTerminalScreen extends EasyMenuScreen<TerminalMenu> implements IScrollable {
	
	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/trader_selection.png");
	public static final Comparator<TraderData> TERMINAL_SORTER = new TraderSorter(true, true, true);
	public static final Comparator<TraderData> NAME_ONLY_SORTER = new TraderSorter(false, false, false);
	
	private EditBox searchField;
	private static int scroll = 0;
	
	ScrollBarWidget scrollBar;
	
	List<NetworkTraderButton> traderButtons;
	
	private List<TraderData> traderList(){
		List<TraderData> traderList = TraderSaveData.GetAllTerminalTraders(true);
		//No longer need to remove the auction house, as the 'showInTerminal' function now confirms the auction houses enabled/visible status.
		//traderList.removeIf(d -> d instanceof AuctionHouseTrader && !Config.SERVER.enableAuctionHouse.get());
		traderList.sort(TERMINAL_SORTER);
		return traderList;
	}
	private List<TraderData> filteredTraderList = new ArrayList<>();

	public NetworkTerminalScreen(TerminalMenu menu, Inventory inventory, Component ignored)
	{
		super(menu, inventory, EasyText.translatable("block.lightmanscurrency.terminal"));
		this.resize(176,187);
	}
	
	@Override
	protected void initialize(ScreenArea screenArea)
	{
		
		this.searchField = this.addChild(new EditBox(this.font, screenArea.x + 28, screenArea.y + 6, 101, 9, EasyText.translatable("gui.lightmanscurrency.terminal.search")));
		this.searchField.setBordered(false);
		this.searchField.setMaxLength(32);
		this.searchField.setTextColor(0xFFFFFF);
		
		this.scrollBar = this.addChild(new ScrollBarWidget(screenArea.pos.offset(16 + NetworkTraderButton.WIDTH, 17), NetworkTraderButton.HEIGHT * 5 + 2, this));
		
		this.initTraderButtons(screenArea);
		
		this.tick();
		
		this.updateTraderList();
		
		this.validateScroll();
		
	}
	
	@Override
	public boolean isPauseScreen() { return false; }
	
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
		return this.searchField.isFocused() && this.searchField.isVisible() && key != GLFW_KEY_ESCAPE || super.keyPressed(key, scanCode, mods);
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
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenTrades(this.filteredTraderList.get(index).getID()));
	}
	
	private int getTraderIndex(EasyButton button)
	{
		if(button instanceof NetworkTraderButton && this.traderButtons.contains(button))
			return traderButtons.indexOf(button) + scroll;
		return -1;
	}
	
	private void updateTraderList()
	{
		//Filtering of results moved to the TradingOffice.filterTraders
		this.filteredTraderList = this.searchField.getValue().isBlank() ? this.traderList() : TraderSearchFilter.FilterTraders(this.traderList(), this.searchField.getValue());
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

	private record TraderSorter(boolean creativeAtTop, boolean emptyAtBottom, boolean auctionHousePriority) implements Comparator<TraderData> {

		@Override
		public int compare(TraderData a, TraderData b) {
			try {

				if (this.auctionHousePriority) {
					boolean ahA = a instanceof AuctionHouseTrader;
					boolean ahB = b instanceof AuctionHouseTrader;
					if (ahA && !ahB)
						return -1;
					else if (ahB && !ahA)
						return 1;
				}

				if (this.emptyAtBottom) {
					boolean emptyA = !a.hasValidTrade();
					boolean emptyB = !b.hasValidTrade();
					if (emptyA != emptyB)
						return emptyA ? 1 : -1;
				}

				if (this.creativeAtTop) {
					//Prioritize creative traders at the top of the list
					if (a.isCreative() && !b.isCreative())
						return -1;
					else if (b.isCreative() && !a.isCreative())
						return 1;
					//If both or neither are creative, sort by name.
				}

				//Sort by trader name
				int sort = a.getName().getString().toLowerCase().compareTo(b.getName().getString().toLowerCase());
				//Sort by owner name if trader name is equal
				if (sort == 0)
					sort = a.getOwner().getOwnerName(true).compareToIgnoreCase(b.getOwner().getOwnerName(true));

				return sort;

			} catch (Throwable t) { return 0; }
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
