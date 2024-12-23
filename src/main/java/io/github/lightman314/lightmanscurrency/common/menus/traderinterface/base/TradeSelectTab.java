package io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.TradeSelectClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceTab;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class TradeSelectTab extends TraderInterfaceTab {

	public TradeSelectTab(TraderInterfaceMenu menu) { super(menu); }

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(@Nonnull Object screen) { return new TradeSelectClientTab(screen, this); }

	@Override
	public boolean canOpen(Player player) {
		return this.menu.getBE().getInteractionType().trades() && this.menu.getBE().targets.getTrader() != null;
	}
	
	public void toggleTradeIndex(int tradeIndex) {
		//LightmansCurrency.LogInfo("Setting trade index to " + tradeIndex + " on the " + DebugUtil.getSideText(this.menu.player));
		if(this.menu.getBE().canAccess(this.menu.player))
		{
			this.menu.getBE().toggleTradeIndex(tradeIndex);
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setInt("ToggleTradeIndex", tradeIndex));
		}
	}
	
	@Override
	public void handleMessage(@Nonnull LazyPacketData message) {
		if(message.contains("ToggleTradeIndex"))
			this.toggleTradeIndex(message.getInt("ToggleTradeIndex"));
	}
	
}
