package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.trades_basic.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

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
	
	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		
		this.tradeDisplay = this.addChild(TradeButtonArea.builder()
				.position(screenArea.pos.offset(3,17))
				.size(screenArea.width - 6,100)
				.traderSource(this.menu::getTrader)
				.context(this.menu::getContext)
				.tradeFilter(this.menu.getTrader(),this.menu)
				.title(screenArea.pos.offset(4,6),screenArea.width - (this.renderAddRemoveButtons() ? 28 : 8),true)
				.interactionHandler(this)
				.blockSearchBox()
				.build());
		
		this.buttonAddTrade = this.addChild(PlainButton.builder()
				.position(screenArea.pos.offset(screenArea.width - 25, 4))
				.pressAction(this::AddTrade)
				.sprite(IconAndButtonUtil.SPRITE_PLUS)
				.build());
		this.buttonRemoveTrade = this.addChild(PlainButton.builder()
				.position(screenArea.pos.offset(screenArea.width - 14, 4))
				.pressAction(this::RemoveTrade)
				.sprite(IconAndButtonUtil.SPRITE_MINUS)
				.build());
		
		this.tick();
		
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) { }

	private boolean renderAddRemoveButtons() {
		if(this.menu.getTrader() != null)
			return this.menu.getTrader().canEditTradeCount();
		return false;
	}

	@Override
	public void tick() {
		
		TraderData trader = this.menu.getTrader();
		if(trader != null)
		{
			this.buttonAddTrade.visible = this.buttonRemoveTrade.visible = trader.canEditTradeCount();
			this.buttonAddTrade.active = trader.getTradeCount() < trader.getMaxTradeCount();
			this.buttonRemoveTrade.active = trader.getTradeCount() > 1;
		}
		else
			this.buttonAddTrade.visible = this.buttonRemoveTrade.visible = false;
	}

	@Override
	public void HandleTradeInputInteraction(@Nonnull TraderData trader, @Nonnull TradeData trade, @Nonnull TradeInteractionData data, int index) {
		if(trader.hasPermission(this.menu.getPlayer(), Permissions.EDIT_TRADES))
			trade.OnInputDisplayInteraction(this.commonTab, index, data, this.menu.getHeldItem());
		else
			Permissions.PermissionWarning(this.menu.getPlayer(), "edit trade", Permissions.EDIT_TRADES);
	}

	@Override
	public void HandleTradeOutputInteraction(@Nonnull TraderData trader, @Nonnull TradeData trade, @Nonnull TradeInteractionData data, int index) {
		if(trader.hasPermission(this.menu.getPlayer(), Permissions.EDIT_TRADES))
			trade.OnOutputDisplayInteraction(this.commonTab, index, data, this.menu.getHeldItem());
		else
			Permissions.PermissionWarning(this.menu.getPlayer(), "edit trade", Permissions.EDIT_TRADES);
	}

	@Override
	public void HandleOtherTradeInteraction(@Nonnull TraderData trader, @Nonnull TradeData trade, @Nonnull TradeInteractionData data) {
		if(trader.hasPermission(this.menu.getPlayer(), Permissions.EDIT_TRADES))
			trade.OnInteraction(this.commonTab, data, this.menu.getHeldItem());
		else
			Permissions.PermissionWarning(this.menu.getPlayer(), "edit trade", Permissions.EDIT_TRADES);
	}
	
	private void AddTrade(EasyButton button) { this.commonTab.addTrade(); }
	
	private void RemoveTrade(EasyButton button) { this.commonTab.removeTrade(); }

}
