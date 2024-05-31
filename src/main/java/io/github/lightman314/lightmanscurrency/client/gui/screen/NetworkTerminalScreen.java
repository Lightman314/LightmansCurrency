package io.github.lightman314.lightmanscurrency.client.gui.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.NetworkTraderButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.TerminalMenu;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.TerminalSorter;
import io.github.lightman314.lightmanscurrency.network.message.trader.CPacketOpenTrades;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

public class NetworkTerminalScreen extends EasyMenuScreen<TerminalMenu> implements IScrollable {

	//TODO rework terminal screen

	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/network_terminal.png");

	private static NetworkTerminalState networkTerminalState;
	
	private EditBox searchField;

	private PlainButton hideEmptyTradersCheckbox;
	private static int scroll = 0;
	
	ScrollBarWidget scrollBar;

	private int columns;
	private int rows;
	
	List<NetworkTraderButton> traderButtons;
	
	private List<TraderData> traderList(){
		List<TraderData> traderList = TraderSaveData.GetAllTerminalTraders(true);
		//No longer need to remove the auction house, as the 'showInTerminal' function now confirms the auction houses enabled/visible status.
		//traderList.removeIf(d -> d instanceof AuctionHouseTrader && !Config.SERVER.enableAuctionHouse.get());
		traderList.sort(TerminalSorter.getDefaultSorter());
		return traderList;
	}
	private List<TraderData> filteredTraderList = new ArrayList<>();

	public NetworkTerminalScreen(TerminalMenu menu, Inventory inventory, Component ignored)
	{
		super(menu, inventory, EasyText.translatable("block.lightmanscurrency.terminal"));
	}

	private ScreenArea calculateSize()
	{
		if(this.minecraft == null)
			return this.getArea();
		this.columns = 1;
		int availableWidth = this.minecraft.getWindow().getGuiScaledWidth() - NetworkTraderButton.WIDTH - 30;
		while(availableWidth >= NetworkTraderButton.WIDTH)
		{
			availableWidth -= NetworkTraderButton.WIDTH;
			this.columns++;
		}
		int availableHeight = this.minecraft.getWindow().getGuiScaledHeight() - NetworkTraderButton.HEIGHT - 37;
		this.rows = 1;
		while(availableHeight >= NetworkTraderButton.HEIGHT)
		{
			availableHeight -= NetworkTraderButton.HEIGHT;
			this.rows++;
		}
		this.resize((this.columns * NetworkTraderButton.WIDTH) + 30, (this.rows * NetworkTraderButton.HEIGHT) + 37);
		return this.getArea();
	}

	@Override
	protected void initialize(ScreenArea screenArea)
	{

		networkTerminalState = NetworkTerminalState.getInstance();

		screenArea = this.calculateSize();

		this.searchField = this.addChild(new EditBox(this.font, screenArea.x + 28, screenArea.y + 6, 101, 9, EasyText.translatable("gui.lightmanscurrency.terminal.search")));
		this.searchField.setBordered(false);
		this.searchField.setMaxLength(32);
		this.searchField.setTextColor(0xFFFFFF);
		this.searchField.setValue(networkTerminalState.searchQuery);

		this.hideEmptyTradersCheckbox = this.addChild(
				IconAndButtonUtil.checkmarkButton(
						screenArea.x + 135,
						screenArea.y + 5,
						(EasyButton b) -> {
							networkTerminalState.hideEmptyTraders = !networkTerminalState.hideEmptyTraders;
							this.updateTraderList();
						},
						() -> networkTerminalState.hideEmptyTraders
				)
		);

		this.scrollBar = this.addChild(new ScrollBarWidget(screenArea.pos.offset(16 + (NetworkTraderButton.WIDTH * this.columns), 17), (NetworkTraderButton.HEIGHT * this.rows) + 2, this));
		
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
				NetworkTraderButton newButton = this.addChild(new NetworkTraderButton(screenArea.pos.offset(15 + (x * NetworkTraderButton.WIDTH), 18 + (y * NetworkTraderButton.HEIGHT)), this::OpenTrader));
				this.traderButtons.add(newButton);
			}
		}
	}
	
	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui)
	{

		//Render the background
		gui.blitBackgroundOfSize(GUI_TEXTURE, 0, 0, this.imageWidth, this.imageHeight, 0, 0, 100, 100, 25);
		//Render the search bar
		gui.blit(GUI_TEXTURE, 14, 3, 100, 0,118, 14);
		//Render the button background
		gui.blitBackgroundOfSize(GUI_TEXTURE, 14, 17, this.imageWidth - 28, this.imageHeight - 34, 0, 100, 100, 100, 25);
		//Render the hideNoStockCheckbox text
		gui.drawString(EasyText.literal("Hide empty traders"), 147, 6, 0x404040);
		
	}
	
	@Override
	public boolean charTyped(char c, int code)
	{
		String s = this.searchField.getValue();
		if(this.searchField.charTyped(c, code))
		{
			if(!Objects.equals(s, this.searchField.getValue()))
			{
				networkTerminalState.searchQuery = this.searchField.getValue();
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
				networkTerminalState.searchQuery = this.searchField.getValue();
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
		this.filteredTraderList = TraderAPI.filterTraders(this.traderList(), networkTerminalState.searchQuery, networkTerminalState.hideEmptyTraders);
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

	private static class NetworkTerminalState {
		private static NetworkTerminalState instance;
		private String searchQuery;
		private boolean hideEmptyTraders;

		private NetworkTerminalState() {
			searchQuery = "";
			hideEmptyTraders = true;
		}

		public static NetworkTerminalState getInstance() {
			if(instance == null) {
				instance = new NetworkTerminalState();
			}
			return instance;
		}

	}

}
