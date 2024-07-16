package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea.InteractionConsumer;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.trades_basic.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class BasicTradeEditClientTab<T extends BasicTradeEditTab> extends TraderStorageClientTab<T> implements InteractionConsumer {

	public BasicTradeEditClientTab(Object screen, T commonTab) {
		super(screen, commonTab);
		this.commonTab.setClient(((TraderStorageScreen)screen)::selfMessage);
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
		
		this.tradeDisplay = this.addChild(new TradeButtonArea(this.menu::getTrader, t -> this.menu.getContext(), screenArea.x + 3, screenArea.y + 17, screenArea.width - 6, 100, (t1,t2) -> {}, this.menu.getTrader() == null ? TradeButtonArea.FILTER_ANY : this.menu.getTrader().getStorageDisplayFilter(this.menu))
				.withTitle(screenArea.pos.offset(4, 6), screenArea.width - (this.renderAddRemoveButtons() ? 28 : 8), true)
				.blockSearchBox());
		this.tradeDisplay.setInteractionConsumer(this);
		
		this.buttonAddTrade = this.addChild(IconAndButtonUtil.plusButton(screenArea.pos.offset(screenArea.width - 25, 4), this::AddTrade));
		this.buttonRemoveTrade = this.addChild(IconAndButtonUtil.minusButton(screenArea.pos.offset(screenArea.width - 14, 4), this::RemoveTrade));
		
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
	public void onTradeButtonInputInteraction(TraderData trader, TradeData trade, int index, int mouseButton) {
		if(trader.hasPermission(this.menu.getPlayer(), Permissions.EDIT_TRADES))
			trade.OnInputDisplayInteraction(this.commonTab, this.screen::selfMessage, index, mouseButton, this.menu.getHeldItem());
		else
			Permissions.PermissionWarning(this.menu.getPlayer(), "edit trade", Permissions.EDIT_TRADES);
	}

	@Override
	public void onTradeButtonOutputInteraction(TraderData trader, TradeData trade, int index, int mouseButton) {
		if(trader.hasPermission(this.menu.getPlayer(), Permissions.EDIT_TRADES))
			trade.OnOutputDisplayInteraction(this.commonTab, this.screen::selfMessage, index, mouseButton, this.menu.getHeldItem());
	}
	
	@Override
	public void onTradeButtonInteraction(TraderData trader, TradeData trade, int localMouseX, int localMouseY, int mouseButton) {
		if(trader.hasPermission(this.menu.getPlayer(), Permissions.EDIT_TRADES))
			trade.OnInteraction(this.commonTab, this.screen::selfMessage, localMouseX, localMouseY, mouseButton, this.menu.getHeldItem());
		else
			Permissions.PermissionWarning(this.menu.getPlayer(), "edit trade", Permissions.EDIT_TRADES);
	}
	
	private void AddTrade(EasyButton button) { this.commonTab.addTrade(); }
	
	private void RemoveTrade(EasyButton button) { this.commonTab.removeTrade(); }

}
