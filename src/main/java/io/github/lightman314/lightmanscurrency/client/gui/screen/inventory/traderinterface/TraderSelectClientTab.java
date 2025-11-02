package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity.InteractionType;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.NetworkTraderButton;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base.TraderSelectTab;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.TerminalSorter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TraderSelectClientTab extends TraderInterfaceClientTab<TraderSelectTab> implements IScrollable {

	public TraderSelectClientTab(Object screen, TraderSelectTab tab) { super(screen,tab); }

	@Override
	public IconData getIcon() { return ItemIcon.ofItem(ModBlocks.TERMINAL); }

	@Override
	public MutableComponent getTooltip() { return LCText.TOOLTIP_INTERFACE_TRADER_SELECT.get(); }

	@Override
	public boolean blockInventoryClosing() { return true; }

	EditBox searchField;

	ScrollBarWidget scrollBar;

	List<NetworkTraderButton> traderButtons = new ArrayList<>();

	private int scroll;

	private List<TraderData> filteredTraderList = new ArrayList<>();

	private List<TraderData> traderList() {
		List<TraderData> traderList = this.filterTraders(TraderAPI.getApi().GetAllNetworkTraders(true));
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
				if((interaction.trades() && trader.hasValidTrade()) || (interaction.targetsTraders() && be.hasTraderPermissions(trader)))
					traders.add(trader);
			}
		}
		return traders;
	}

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {

		this.searchField = this.addChild(new EditBox(this.getFont(), screenArea.x + 43, screenArea.y + 6, 101, 9, firstOpen ? null : this.searchField, LCText.GUI_NETWORK_TERMINAL_SEARCH.get()));
		this.searchField.setBordered(false);
		this.searchField.setMaxLength(32);
		this.searchField.setTextColor(0xFFFFFF);
		this.searchField.setResponder(this::onSearchChanged);

		this.initTraderButtons(screenArea.pos);

		this.scrollBar = this.addChild(ScrollBarWidget.builder()
				.position(screenArea.pos.offset(30 + NetworkTraderButton.WIDTH,18))
				.height(NetworkTraderButton.HEIGHT * 4)
				.scrollable(this)
				.build());

		this.tick();

		this.updateTraderList();

		this.addChild(ScrollListener.builder()
				.position(0,0)
				.size(screen.width,screen.height)
				.listener(this)
				.build());

	}

	private void initTraderButtons(ScreenPosition corner)
	{
		this.traderButtons = new ArrayList<>();
		for(int y = 0; y < 4; ++y)
		{
			final int buttonIndex = y;
			NetworkTraderButton newButton = this.addChild(NetworkTraderButton.builder()
					.position(corner.offset(30,18 + (y * NetworkTraderButton.HEIGHT)))
					.pressAction(() -> this.ToggleTrader(buttonIndex))
					.build());
			this.traderButtons.add(newButton);
		}
	}

	@Override
	public void renderBG(EasyGuiGraphics gui) {

		gui.resetColor();
        SpriteUtil.SEARCH_ICON.render(gui,28,3);
        SpriteUtil.SEARCH_FIELD.render(gui,40,4,105);

	}

	@Override
	public void tick() {

		List<TraderData> selectedTraders = this.menu.getBE().targets.getTraders();
		for (NetworkTraderButton button : this.traderButtons) {
			button.selected = button.getData() != null && selectedTraders.contains(button.getData());
		}

	}

	private void onSearchChanged(String newSearch) { this.updateTraderList(); }

	private void ToggleTrader(int traderButtonIndex) {
		int index = traderButtonIndex + this.scroll;
		if(index >= 0 && index < this.filteredTraderList.size())
		{
			long traderID = this.filteredTraderList.get(index).getID();
			this.commonTab.toggleTrader(traderID);
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
		this.filteredTraderList = TraderAPI.getApi().FilterTraders(this.traderList(), this.searchField.getValue());
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

}