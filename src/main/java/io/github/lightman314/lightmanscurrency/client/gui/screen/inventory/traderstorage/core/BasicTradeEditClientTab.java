package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.core;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.core.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import io.github.lightman314.lightmanscurrency.common.util.TooltipHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.List;

public class BasicTradeEditClientTab<T extends BasicTradeEditTab> extends TraderStorageClientTab<T> implements TradeInteractionHandler {

	public BasicTradeEditClientTab(Object screen, T commonTab) {
		super(screen, commonTab);
		//this.commonTab.overrideTabChangeHandler(this.screen::ChangeTab);
	}

	@Nonnull
	@Override
	public IconData getIcon() { return IconUtil.ICON_TRADELIST; }

	@Override
	public Component getTooltip() { return LCText.TOOLTIP_TRADER_EDIT_TRADES.get(); }

	TradeButtonArea tradeDisplay;
	
	EasyButton buttonAddTrade;
	EasyButton buttonRemoveTrade;

	EasyButton buttonSelectAllTrades;
	EasyButton buttonOpenMultiEdit;
	
	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {

		this.screen.getRightEdgePositioner().removeWidgets(this.buttonSelectAllTrades,this.buttonOpenMultiEdit);

		this.tradeDisplay = this.addChild(TradeButtonArea.builder()
				.position(screenArea.pos.offset(3,17))
				.size(screenArea.width - 6,111)
				.traderSource(this.menu::getTrader)
				.context(this.menu::getContext)
				.tradeFilter(this.menu.getTrader(),this.menu)
				.title(screenArea.pos.offset(4,6),screenArea.width - (this.addRemoveVisible() ? 28 : 8),true)
				.interactionHandler(this)
				.selectedState(this.commonTab::isSelected)
				.blockSearchBox()
				.extraTooltips(this::tradeSelectTooltip)
				.old(this.tradeDisplay)
				.build());
		
		this.buttonAddTrade = this.addChild(PlainButton.builder()
				.position(screenArea.pos.offset(screenArea.width - 25, 4))
				.pressAction(this::AddTrade)
				.sprite(IconAndButtonUtil.SPRITE_PLUS)
				.addon(EasyAddonHelper.visibleCheck(this::addRemoveVisible))
				.addon(EasyAddonHelper.activeCheck(this::addActive))
				.build());
		this.buttonRemoveTrade = this.addChild(PlainButton.builder()
				.position(screenArea.pos.offset(screenArea.width - 14, 4))
				.pressAction(this::RemoveTrade)
				.sprite(IconAndButtonUtil.SPRITE_MINUS)
				.addon(EasyAddonHelper.visibleCheck(this::addRemoveVisible))
				.addon(EasyAddonHelper.activeCheck(this::removeActive))
				.build());

		this.buttonSelectAllTrades = this.addChild(IconButton.builder()
				.position(screenArea.pos.offset(screenArea.width,20))
				.pressAction(this.commonTab::SelectAllTrades)
				.icon(this::selectAllIcon)
				.addon(EasyAddonHelper.visibleCheck(this.commonTab::allowTradeSelection))
				.addon(EasyAddonHelper.tooltip(this::selectAllTooltip))
				.build());

		this.buttonOpenMultiEdit = this.addChild(IconButton.builder()
				.position(screenArea.pos.offset(screenArea.width,40))
				.pressAction(this.commonTab::OpenMultiEditTab)
				.icon(IconUtil.ICON_TRADER_ALT)
				.addon(EasyAddonHelper.activeCheck(this.commonTab::canOpenMultiEdit))
				.addon(EasyAddonHelper.visibleCheck(this.commonTab::allowTradeSelection))
				.addon(EasyAddonHelper.tooltips(this::multiEditTooltip, TooltipHelper.DEFAULT_TOOLTIP_WIDTH))
				.build());

		this.screen.getRightEdgePositioner().addWidgets(this.buttonSelectAllTrades,this.buttonOpenMultiEdit);
		
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) { }

	@Override
	protected void closeAction() {
		this.screen.getRightEdgePositioner().removeWidgets(this.buttonSelectAllTrades,this.buttonOpenMultiEdit);
	}

	private boolean addRemoveVisible()
	{
		TraderData trader = this.menu.getTrader();
		return trader != null && trader.canEditTradeCount();
	}

	private boolean addActive()
	{
		TraderData trader = this.menu.getTrader();
		return trader != null && trader.getTradeCount() < trader.getMaxTradeCount();
	}

	private boolean removeActive()
	{
		TraderData trader = this.menu.getTrader();
		return trader != null && trader.getTradeCount() > 1;
	}

	private List<Component> tradeSelectTooltip()
	{
		if(this.commonTab.allowTradeSelection())
			return Lists.newArrayList(LCText.TOOLTIP_TRADE_SELECT.getWithStyle(ChatFormatting.YELLOW));
		return null;
	}

	private IconData selectAllIcon() { return this.commonTab.allTradesSelected() ? IconUtil.ICON_MINUS : IconUtil.ICON_PLUS; }
	private Component selectAllTooltip() { return this.commonTab.allTradesSelected() ? LCText.TOOLTIP_TRADER_DESELECT_ALL_TRADES.get() : LCText.TOOLTIP_TRADER_SELECT_ALL_TRADES.get(); }

	private List<Component> multiEditTooltip() { return Lists.newArrayList(LCText.TOOLTIP_TRADER_OPEN_MULTI_EDIT_SELECTED.get(this.commonTab.selectedCount())); }

	@Override
	public void HandleTradeInputInteraction(@Nonnull TraderData trader, @Nonnull TradeData trade, @Nonnull TradeInteractionData data, int index) {
		if(trader.hasPermission(this.menu.getPlayer(), Permissions.EDIT_TRADES))
		{
			if(data.ctrlHeld())
				this.commonTab.ToggleTradeSelection(trader.indexOfTrade(trade));
			else
				trade.OnInputDisplayInteraction(this.commonTab, index, data, this.menu.getHeldItem());
		}
		else
			Permissions.PermissionWarning(this.menu.getPlayer(), "edit trade", Permissions.EDIT_TRADES);
	}

	@Override
	public void HandleTradeOutputInteraction(@Nonnull TraderData trader, @Nonnull TradeData trade, @Nonnull TradeInteractionData data, int index) {
		if(trader.hasPermission(this.menu.getPlayer(), Permissions.EDIT_TRADES))
		{
			if(data.ctrlHeld())
				this.commonTab.ToggleTradeSelection(trader.indexOfTrade(trade));
			else
				trade.OnOutputDisplayInteraction(this.commonTab, index, data, this.menu.getHeldItem());
		}
		else
			Permissions.PermissionWarning(this.menu.getPlayer(), "edit trade", Permissions.EDIT_TRADES);
	}

	@Override
	public void HandleOtherTradeInteraction(@Nonnull TraderData trader, @Nonnull TradeData trade, @Nonnull TradeInteractionData data) {
		if(trader.hasPermission(this.menu.getPlayer(), Permissions.EDIT_TRADES))
		{
			if(data.ctrlHeld())
				this.commonTab.ToggleTradeSelection(trader.indexOfTrade(trade));
			else
				trade.OnInteraction(this.commonTab, data, this.menu.getHeldItem());
		}

		else
			Permissions.PermissionWarning(this.menu.getPlayer(), "edit trade", Permissions.EDIT_TRADES);
	}
	
	private void AddTrade() { this.commonTab.addTrade(); }
	
	private void RemoveTrade() { this.commonTab.removeTrade(); }

}
