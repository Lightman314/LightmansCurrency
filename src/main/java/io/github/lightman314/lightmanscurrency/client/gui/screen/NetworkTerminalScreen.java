package io.github.lightman314.lightmanscurrency.client.gui.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.NetworkTraderButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.menus.TerminalMenu;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.TerminalSorter;
import io.github.lightman314.lightmanscurrency.network.message.trader.CPacketOpenTrades;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

public class NetworkTerminalScreen extends EasyMenuScreen<TerminalMenu> implements IScrollable {

	private static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource("textures/gui/container/network_terminal.png");
	
	private EditBox searchField;
	private int searchWidth = 118;
	private static int scroll = 0;
	private static String lastSearch = "";
	
	ScrollBarWidget scrollBar;

	private int columns;
	private int rows;
	
	List<NetworkTraderButton> traderButtons;
	
	private List<TraderData> traderList(){
		List<TraderData> traderList = TraderAPI.API.GetAllNetworkTraders(true);
		//No longer need to remove the auction house, as the 'showInTerminal' function now confirms the auction houses enabled/visible status.
		traderList.sort(TerminalSorter.getDefaultSorter());
		return traderList;
	}
	private List<TraderData> filteredTraderList = new ArrayList<>();

	public NetworkTerminalScreen(TerminalMenu menu, Inventory inventory, Component ignored)
	{
		super(menu, inventory, LCText.GUI_NETWORK_TERMINAL_TITLE.get());
	}

	private ScreenArea calculateSize()
	{
		if(this.minecraft == null)
			return this.getArea();
		this.columns = 1;
		int columnLimit = LCConfig.CLIENT.terminalColumnLimit.get();
		int availableWidth = this.minecraft.getWindow().getGuiScaledWidth() - NetworkTraderButton.WIDTH - 30;
		while(availableWidth >= NetworkTraderButton.WIDTH && this.columns < columnLimit)
		{
			availableWidth -= NetworkTraderButton.WIDTH;
			this.columns++;
		}
		int availableHeight = this.minecraft.getWindow().getGuiScaledHeight() - NetworkTraderButton.HEIGHT - 45;
		this.rows = 1;
		int rowLimit = LCConfig.CLIENT.terminalRowLimit.get();
		while(availableHeight >= NetworkTraderButton.HEIGHT && this.rows < rowLimit)
		{
			availableHeight -= NetworkTraderButton.HEIGHT;
			this.rows++;
		}
		this.resize((this.columns * NetworkTraderButton.WIDTH) + 30, (this.rows * NetworkTraderButton.HEIGHT) + 36);
		return this.getArea();
	}
	
	@Override
	protected void initialize(ScreenArea screenArea)
	{

		screenArea = this.calculateSize();

		this.searchWidth = 50 + ((this.columns - 1) * NetworkTraderButton.WIDTH);

		this.searchField = this.addChild(new EditBox(this.font, screenArea.x + 28, screenArea.y + 10, this.searchWidth - 17, 9, this.searchField, LCText.GUI_NETWORK_TERMINAL_SEARCH.get()));
		this.searchField.setBordered(false);
		this.searchField.setMaxLength(32);
		this.searchField.setTextColor(0xFFFFFF);
		this.searchField.setValue(lastSearch);
		this.searchField.setResponder(this::onSearchChanged);

		this.addChild(IconButton.builder()
				.position(screenArea.pos.offset(screenArea.width - 24,4))
				.pressAction(this::OpenAllTraders)
				.icon(IconData.of(ModBlocks.ITEM_NETWORK_TRADER_4))
				.addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_NETWORK_TERMINAL_OPEN_ALL))
				.build());

		this.scrollBar = this.addChild(ScrollBarWidget.builder()
				.position(screenArea.pos.offset(16 + (NetworkTraderButton.WIDTH * this.columns),17))
				.height((NetworkTraderButton.HEIGHT * this.rows) * 2)
				.scrollable(this)
				.build());
		
		this.initTraderButtons(screenArea);
		
		this.updateTraderList();
		
		this.validateScroll();
		
	}
	
	private void initTraderButtons(ScreenArea screenArea)
	{
		this.traderButtons = new ArrayList<>();
		for(int y = 0; y < this.rows; y++)
		{
			for(int x = 0; x < this.columns; ++x)
			{
				NetworkTraderButton newButton = this.addChild(NetworkTraderButton.builder()
						.position(screenArea.pos.offset(15 + (x * NetworkTraderButton.WIDTH),26 + (y * NetworkTraderButton.HEIGHT)))
						.pressAction(this::OpenTrader)
						.build());
				this.traderButtons.add(newButton);
			}
		}
	}
	
	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui)
	{

		//Render the background
		gui.blitNineSplit(GUI_TEXTURE, 0, 0, this.imageWidth, this.imageHeight, 0, 0, 100, 100, 25);
		//Render the search icon
		gui.blit(GUI_TEXTURE, 14, 7, 100, 0,11, 14);
		//Render search input background
		gui.blitHorizSplit(GUI_TEXTURE,25,7,this.searchWidth,14,111,0,107,4);
		//Render the button background
		gui.blitNineSplit(GUI_TEXTURE, 14, 25, this.imageWidth - 28, this.imageHeight - 42, 0, 100, 100, 100, 25);
		
	}

	protected void onSearchChanged(String newSearch)
	{
		if(newSearch.equals(lastSearch))
			return;
		lastSearch = newSearch;
		this.updateTraderList();
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
	public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
		if(this.handleScrollWheel(deltaY))
			return true;
		return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
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
			return this.traderButtons.indexOf(button) + (scroll * this.columns);
		return -1;
	}
	
	private void updateTraderList()
	{
		//Filtering of results moved to the TradingOffice.filterTraders
		StringBuilder fullSearch = new StringBuilder();
		String extra = LCConfig.CLIENT.terminalBonusFilters.get();
		if(!extra.isBlank())
		{
			fullSearch = fullSearch.append(extra);
			if(!extra.endsWith(" "))
				fullSearch.append(" ");
		}
		if(!this.searchField.getValue().isBlank())
			fullSearch.append(this.searchField.getValue());
		this.filteredTraderList = TraderAPI.API.FilterTraders(this.traderList(), fullSearch.toString());
		//Validate the scroll
		this.validateScroll();
		//Update the trader buttons
		this.updateTraderButtons();
	}
	
	private void updateTraderButtons()
	{
		int startIndex = scroll * this.columns;
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
	public int getMaxScroll() { return IScrollable.calculateMaxScroll(this.columns * this.rows, this.columns, this.filteredTraderList.size()); }

	private void OpenAllTraders(EasyButton button) { new CPacketOpenTrades(-1).send(); }

}
