package io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.TradeSelectClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceTab;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class TradeSelectTab extends TraderInterfaceTab {

	public TradeSelectTab(TraderInterfaceMenu menu) { super(menu); }

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(@Nonnull Object screen) { return new TradeSelectClientTab(screen, this); }

	@Override
	public boolean canOpen(Player player) {
		return this.menu.getBE().getInteractionType().trades && this.menu.getBE().getTrader() != null;
	}

	public void setTradeIndex(int tradeIndex) {
		//LightmansCurrency.LogInfo("Setting trade index to " + tradeIndex + " on the " + DebugUtil.getSideText(this.menu.player));
		if(this.menu.getBE().canAccess(this.menu.player))
		{
			this.menu.getBE().setTradeIndex(tradeIndex);
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setInt("NewTradeIndex", tradeIndex));
		}
	}

	@Override
	public void handleMessage(@Nonnull LazyPacketData message) {
		if(message.contains("NewTradeIndex"))
			this.setTradeIndex(message.getInt("NewTradeIndex"));
	}

}