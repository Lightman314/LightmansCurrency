package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage;

import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea.InteractionConsumer;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.trades_basic.BasicTradeEditTab;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class BasicTradeEditClientTab<T extends BasicTradeEditTab> extends TraderStorageClientTab<T> implements InteractionConsumer{

	public BasicTradeEditClientTab(Object screen, T commonTab) { super(screen, commonTab); this.commonTab.setClientHandler(((TraderStorageScreen)screen)::selfMessage); }

	@Nonnull
	@Override
	public IconData getIcon() { return IconAndButtonUtil.ICON_TRADELIST; }

	@Override
	public Component getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.trader.edit_trades"); }
	
	@Override
	public boolean blockInventoryClosing() { return false; }

	TradeButtonArea tradeDisplay;
	
	EasyButton buttonAddTrade;
	EasyButton buttonRemoveTrade;
	
	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		
		this.tradeDisplay = this.addChild(new TradeButtonArea(this.menu.traderSource, t -> this.menu.getContext(), screenArea.x + 3, screenArea.y + 17, screenArea.width - 6, 100, (t1,t2) -> {}, this.menu.getTrader() == null ? TradeButtonArea.FILTER_ANY : this.menu.getTrader().getStorageDisplayFilter(this.menu)));
		this.tradeDisplay.setInteractionConsumer(this);
		this.tradeDisplay.withTitle(screenArea.pos.offset(6, 6), screenArea.width - (this.renderAddRemoveButtons() ? 32 : 16), true);
		
		this.buttonAddTrade = this.addChild(IconAndButtonUtil.plusButton(screenArea.pos.offset(screenArea.width- 25, 4), this::AddTrade));
		this.buttonRemoveTrade = this.addChild(IconAndButtonUtil.minusButton(screenArea.pos.offset(screenArea.width- 14, 4), this::RemoveTrade));
		
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
		if(trader.hasPermission(this.menu.player, Permissions.EDIT_TRADES))
			trade.onInputDisplayInteraction(this.commonTab, this.screen::selfMessage, index, mouseButton, this.menu.getCarried());
		else
			Permissions.PermissionWarning(this.menu.player, "edit trade", Permissions.EDIT_TRADES);
	}

	@Override
	public void onTradeButtonOutputInteraction(TraderData trader, TradeData trade, int index, int mouseButton) {
		if(trader.hasPermission(this.menu.player, Permissions.EDIT_TRADES))
			trade.onOutputDisplayInteraction(this.commonTab, this.screen::selfMessage, index, mouseButton, this.menu.getCarried());
		else
			Permissions.PermissionWarning(this.menu.player, "edit trade", Permissions.EDIT_TRADES);
	}
	
	@Override
	public void onTradeButtonInteraction(TraderData trader, TradeData trade, int localMouseX, int localMouseY, int mouseButton) {
		if(trader.hasPermission(this.menu.player, Permissions.EDIT_TRADES))
			trade.onInteraction(this.commonTab, this.screen::selfMessage, localMouseX, localMouseY, mouseButton, this.menu.getCarried());
		else
			Permissions.PermissionWarning(this.menu.player, "edit trade", Permissions.EDIT_TRADES);
	}
	
	private void AddTrade(EasyButton button) { this.commonTab.addTrade(); }
	
	private void RemoveTrade(EasyButton button) { this.commonTab.removeTrade(); }

}
